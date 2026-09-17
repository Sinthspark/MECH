package com.sinthspark.mech.fluid;

import com.sinthspark.mech.blockentity.PipeTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.*;

/** Cached connected component of pipes. Fluid transfer runs every server tick. */
public class FluidNetwork {
    private final Set<BlockPos> pipes = new HashSet<>();
    private final Set<IFluidHandler> sources = new HashSet<>();
    private final Set<IFluidHandler> sinks = new HashSet<>();

    private List<IFluidHandler> cachedSources = List.of();
    private List<IFluidHandler> cachedSinks = List.of();
    private List<IFluidHandler> cachedPipeHandlers = List.of();
    private int cachedTotalLoss;
    private int cachedMaxTransferRate;
    private boolean dirty = true;

    public void addPipe(BlockPos pos) { if (pipes.add(pos)) dirty = true; }
    public void removePipe(BlockPos pos) { if (pipes.remove(pos)) dirty = true; }
    public void addSource(IFluidHandler source) { if (source != null) sources.add(source); }
    public void addSink(IFluidHandler sink) { if (sink != null) sinks.add(sink); }

    public void replaceEndpoints(Collection<IFluidHandler> newSources, Collection<IFluidHandler> newSinks) {
        sources.clear();
        sinks.clear();
        sources.addAll(newSources);
        sinks.addAll(newSinks);
        dirty = true;
    }

    public void clearSources() { sources.clear(); dirty = true; }
    public void clearSinks() { sinks.clear(); dirty = true; }
    public Set<BlockPos> getPipes() { return Collections.unmodifiableSet(pipes); }
    public Set<IFluidHandler> getSources() { return Collections.unmodifiableSet(sources); }
    public Set<IFluidHandler> getSinks() { return Collections.unmodifiableSet(sinks); }

    public void merge(FluidNetwork other) {
        if (other == this) return;
        pipes.addAll(other.pipes);
        sources.addAll(other.sources);
        sinks.addAll(other.sinks);
        dirty = true;
    }

    private void updateCache(Level level) {
        if (!dirty) return;
        cachedSources = sources.stream().filter(Objects::nonNull).toList();
        cachedSinks = sinks.stream().filter(Objects::nonNull).toList();
        cachedTotalLoss = 0;
        cachedMaxTransferRate = Integer.MAX_VALUE;

        List<IFluidHandler> pipeHandlers = new ArrayList<>();
        for (BlockPos pos : pipes) {
            if (level.getBlockEntity(pos) instanceof PipeTile pipe) {
                cachedTotalLoss = safeAdd(cachedTotalLoss, 1);
                cachedMaxTransferRate = Math.min(cachedMaxTransferRate, Math.max(0, pipe.getTransferRate()));
                pipeHandlers.add(pipe);
            }
        }
        cachedPipeHandlers = pipeHandlers;
        if (cachedMaxTransferRate == Integer.MAX_VALUE) cachedMaxTransferRate = 0;
        dirty = false;
    }

    public void tick(Level level) {
        updateCache(level);

        // ⚔️ Арка XI: раньше тут весь клан труб глушил флаг — даже те,
        // в ком жидкость оставалась. Теперь — честная сверка по ёмкостям.
        if (cachedSources.isEmpty() || cachedPipeHandlers.isEmpty() || cachedMaxTransferRate <= 0) {
            syncPipesVisualState(level);
            return;
        }

        FluidStack fluid = null;
        int sourceBudget = 0;
        for (IFluidHandler source : cachedSources) {
            if (source.getTanks() <= 0) continue;
            for (int tank = 0; tank < source.getTanks(); tank++) {
                FluidStack stack = source.getFluidInTank(tank);
                if (stack.isEmpty()) continue;
                if (fluid == null) fluid = stack.copy();
                if (!fluid.isFluidEqual(stack)) continue;
                int remainingBudget = cachedMaxTransferRate - sourceBudget;
                if (remainingBudget <= 0) break;
                sourceBudget = safeAdd(sourceBudget, Math.min(stack.getAmount(), remainingBudget));
            }
            if (sourceBudget >= cachedMaxTransferRate) break;
        }

        if (fluid == null || sourceBudget <= 0) {
            setAllPipesHasFluid(level, false);
            return;
        }

        int afterLoss = Math.max(0, sourceBudget - cachedTotalLoss);
        if (afterLoss <= 0) {
            setAllPipesHasFluid(level, false);
            return;
        }

        // Only real external machines are handled here. PipeTile is deliberately
        // excluded from cachedSinks; otherwise the same pipe is targeted twice and
        // the first pipe consumes the whole budget before the rest get anything.
        int[] plannedExternal = planSequential(cachedSinks, fluid.getFluid(), afterLoss);
        int acceptedExternal = sum(plannedExternal);
        int remainingForPipes = afterLoss - acceptedExternal;
        int[] plannedPipes = planFairShare(cachedPipeHandlers, fluid.getFluid(), remainingForPipes);
        int acceptedPipes = sum(plannedPipes);
        int acceptedTotal = acceptedExternal + acceptedPipes;

        if (acceptedTotal <= 0) {
            setAllPipesHasFluid(level, false);
            return;
        }

        int amountToDrain = Math.min(sourceBudget, acceptedTotal + cachedTotalLoss);
        int remainingDrain = amountToDrain;
        for (IFluidHandler source : cachedSources) {
            if (remainingDrain <= 0) break;
            FluidStack request = new FluidStack(fluid.getFluid(), remainingDrain);
            FluidStack drained = source.drain(request, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) remainingDrain -= drained.getAmount();
        }

        applyPlanned(cachedSinks, plannedExternal, fluid.getFluid());
        applyPlanned(cachedPipeHandlers, plannedPipes, fluid.getFluid());
        setAllPipesHasFluid(level, acceptedTotal > 0);
    }

    private static int[] planSequential(List<IFluidHandler> targets, Fluid fluidType, int totalAmount) {
        int[] planned = new int[targets.size()];
        int remaining = totalAmount;
        for (int i = 0; i < targets.size() && remaining > 0; i++) {
            int accepted = targets.get(i).fill(new FluidStack(fluidType, remaining), IFluidHandler.FluidAction.SIMULATE);
            accepted = Math.max(0, Math.min(accepted, remaining));
            planned[i] = accepted;
            remaining -= accepted;
        }
        return planned;
    }

    private static int[] planFairShare(List<IFluidHandler> targets, Fluid fluidType, int totalAmount) {
        int n = targets.size();
        int[] planned = new int[n];
        if (n == 0 || totalAmount <= 0) return planned;

        boolean[] done = new boolean[n];
        int remaining = totalAmount;
        int activeCount = n;
        int safetyRounds = n + 8;

        while (remaining > 0 && activeCount > 0 && safetyRounds-- > 0) {
            int share = Math.max(1, remaining / activeCount);
            boolean progressed = false;
            for (int i = 0; i < n && remaining > 0; i++) {
                if (done[i]) continue;
                int request = Math.min(share, remaining);
                int accepted = targets.get(i).fill(new FluidStack(fluidType, request), IFluidHandler.FluidAction.SIMULATE);
                accepted = Math.max(0, Math.min(accepted, request));
                if (accepted <= 0) {
                    done[i] = true;
                    activeCount--;
                    continue;
                }
                planned[i] += accepted;
                remaining -= accepted;
                progressed = true;
                if (accepted < request) {
                    done[i] = true;
                    activeCount--;
                }
            }
            if (!progressed) break;
        }
        return planned;
    }

    private static void applyPlanned(List<IFluidHandler> targets, int[] planned, Fluid fluidType) {
        for (int i = 0; i < targets.size(); i++) {
            if (planned[i] <= 0) continue;
            targets.get(i).fill(new FluidStack(fluidType, planned[i]), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int value : values) total = safeAdd(total, value);
        return total;
    }

    // ⚔️ Арка XI: каждая труба отвечает за свой флаг по факту содержимого бака
    private void syncPipesVisualState(Level level) {
        for (BlockPos pos : pipes) {
            if (!(level.getBlockEntity(pos) instanceof PipeTile pipe)) continue;
            boolean has = false;
            var handler = pipe.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElse(null);
            if (handler != null) {
                for (int t = 0; t < handler.getTanks(); t++) {
                    if (!handler.getFluidInTank(t).isEmpty()) { has = true; break; }
                }
            }
            pipe.setHasFluid(has);
        }
    }

    private void setAllPipesHasFluid(Level level, boolean hasFluid) {
        for (BlockPos pos : pipes) {
            if (level.getBlockEntity(pos) instanceof PipeTile pipe) pipe.setHasFluid(hasFluid);
        }
    }

    private static int safeAdd(int a, int b) {
        long value = (long) a + b;
        return (int) Math.min(Integer.MAX_VALUE, value);
    }
}