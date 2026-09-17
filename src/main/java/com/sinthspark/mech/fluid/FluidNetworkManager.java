package com.sinthspark.mech.fluid;

import com.sinthspark.mech.blockentity.AdvancedBoilerTankTile;
import com.sinthspark.mech.blockentity.PipeTile;
import com.sinthspark.mech.blocks.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

public final class FluidNetworkManager {
    private static final Map<String, FluidNetwork> pipeToNetwork = new HashMap<>();
    private static final Map<Level, Set<FluidNetwork>> networksByLevel = new IdentityHashMap<>();
    private static final Map<Level, Set<BlockPos>> pendingUpdates = new IdentityHashMap<>();

    private FluidNetworkManager() {}

    private static Set<FluidNetwork> networks(Level level) {
        return networksByLevel.computeIfAbsent(level, ignored -> new HashSet<>());
    }

    public static void scheduleNetworkUpdate(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) return;
        pendingUpdates.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.immutable());
    }

    public static void processPendingUpdates() {
        if (pendingUpdates.isEmpty()) return;
        Map<Level, Set<BlockPos>> updates = new IdentityHashMap<>(pendingUpdates);
        pendingUpdates.clear();
        for (Map.Entry<Level, Set<BlockPos>> entry : updates.entrySet()) processUpdates(entry.getKey(), entry.getValue());
    }

    private static void processUpdates(Level level, Set<BlockPos> positions) {
        for (BlockPos pos : positions) {
            if (isPipe(level, pos)) rebuildNetworkAt(level, pos);
            else onPipeRemoved(level, pos);
        }
    }

    private static void rebuildNetworkAt(Level level, BlockPos pos) {
        if (!isPipe(level, pos)) {
            onPipeRemoved(level, pos);
            return;
        }
        Set<FluidNetwork> affected = getNeighborNetworks(level, pos);
        FluidNetwork old = pipeToNetwork.get(getKey(level, pos));
        if (old != null) affected.add(old);

        Set<BlockPos> seeds = new HashSet<>();
        for (FluidNetwork network : affected) {
            seeds.addAll(network.getPipes());
            removeNetworkMappings(network, level);
            networks(level).remove(network);
        }
        seeds.add(pos);

        // ВАЖНО: если изменение соседа физически разорвало сеть на несколько кусков,
        // один BFS-проход от случайной точки соберёт только один из них.
        // Крутим, пока не разберём все seed-позиции — аналогично onPipeRemoved.
        while (!seeds.isEmpty()) {
            buildNetworkFrom(level, seeds.iterator().next(), seeds, true);
        }
    }

    public static void onPipePlaced(Level level, BlockPos pipePos) {
        if (!isPipe(level, pipePos)) return;

        Set<FluidNetwork> neighbors = getNeighborNetworks(level, pipePos);
        FluidNetwork network = new FluidNetwork();

        for (FluidNetwork neighbor : neighbors) {
            network.merge(neighbor);
            removeNetworkMappings(neighbor, level);
            networks(level).remove(neighbor);
        }

        networks(level).add(network);
        for (BlockPos pos : network.getPipes()) {
            pipeToNetwork.put(getKey(level, pos), network);
        }

        network.addPipe(pipePos);
        pipeToNetwork.put(getKey(level, pipePos), network);

        // ВАЖНО: сканируем ВСЕ трубы сети, а не только новую
        rescanSourcesAndSinks(level, network);

        // НЕМЕДЛЕННЫЙ пересчёт
        network.tick(level);
    }

    public static void onPipeRemoved(Level level, BlockPos pipePos) {
        if (level == null || level.isClientSide) return;

        FluidNetwork old = pipeToNetwork.get(getKey(level, pipePos));
        if (old == null) return;

        removeNetworkMappings(old, level);
        networks(level).remove(old);

        // ВАЖНО: сначала удаляем трубу, потом собираем остатки
        old.removePipe(pipePos);

        Set<BlockPos> unvisited = new HashSet<>(old.getPipes());
        while (!unvisited.isEmpty()) {
            buildNetworkFrom(level, unvisited.iterator().next(), unvisited, true);
        }
    }

    public static void onPipeNeighborChanged(Level level, BlockPos pipePos) {
        scheduleNetworkUpdate(level, pipePos);
    }

    private static FluidNetwork buildNetworkFrom(Level level, BlockPos start, Set<BlockPos> allowed, boolean addToManager) {
        if (!isPipe(level, start)) {
            if (allowed != null) allowed.remove(start);
            return null;
        }
        FluidNetwork network = new FluidNetwork();
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove();
            network.addPipe(pos);
            pipeToNetwork.put(getKey(level, pos), network);
            if (allowed != null) allowed.remove(pos);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (visited.contains(neighbor) || (allowed != null && !allowed.contains(neighbor))) continue;
                if (!isConnected(level, pos, direction)) continue;
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
        rescanSourcesAndSinks(level, network);
        if (addToManager) networks(level).add(network);
        return network;
    }

    private static void rescanSourcesAndSinks(Level level, FluidNetwork network) {
        Set<IFluidHandler> sources = new HashSet<>();
        Set<IFluidHandler> sinks = new HashSet<>();

        for (BlockPos pipePos : network.getPipes()) {
            for (Direction direction : Direction.values()) {
                if (!isConnected(level, pipePos, direction)) continue;

                var be = level.getBlockEntity(pipePos.relative(direction));
                if (be == null) continue;

                be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite())
                        .resolve()
                        .ifPresent(handler -> {
                            // Бойлер ВСЕГДА источник
                            if (be instanceof AdvancedBoilerTankTile) {
                                sources.add(handler);
                                System.out.println("[Fluid] Бойлер добавлен как источник");
                                return;
                            }

                            // Трубы — приёмники (они хранят пар)
                            if (be instanceof PipeTile) {
                                sinks.add(handler);
                                System.out.println("[Fluid] Труба добавлена как приёмник");
                                return;
                            }

                            // Обычная проверка
                            boolean canExtract = false;
                            for (int tank = 0; tank < handler.getTanks(); tank++) {
                                FluidStack stack = handler.getFluidInTank(tank);
                                if (!stack.isEmpty()) {
                                    canExtract = true;
                                    break;
                                }
                            }

                            boolean canReceive = handler.fill(
                                    new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1),
                                    IFluidHandler.FluidAction.SIMULATE
                            ) > 0;

                            if (canExtract) sources.add(handler);
                            if (canReceive) sinks.add(handler);
                        });
            }
        }

        System.out.println("[Fluid] Итог: Источники=" + sources.size() + ", Приёмники=" + sinks.size());
        network.replaceEndpoints(sources, sinks);
    }

    // Вспомогательный метод — найти жидкость в сети
    private static FluidStack getNetworkFluid(FluidNetwork network, Level level) {
        for (BlockPos pos : network.getPipes()) {
            if (level.getBlockEntity(pos) instanceof PipeTile pipe) {
                FluidStack fluid = pipe.getFluidInTank(0);
                if (!fluid.isEmpty()) return fluid;
            }
        }
        // Если трубы пусты — ищем в источниках
        for (IFluidHandler source : network.getSources()) {
            for (int tank = 0; tank < source.getTanks(); tank++) {
                FluidStack fluid = source.getFluidInTank(tank);
                if (!fluid.isEmpty()) return fluid;
            }
        }
        return null;
    }

    private static Set<FluidNetwork> getNeighborNetworks(Level level, BlockPos pipePos) {
        Set<FluidNetwork> result = new HashSet<>();
        for (Direction direction : Direction.values()) {
            if (!isConnected(level, pipePos, direction)) continue;
            FluidNetwork network = pipeToNetwork.get(getKey(level, pipePos.relative(direction)));
            if (network != null) result.add(network);
        }
        return result;
    }

    private static boolean isPipe(Level level, BlockPos pos) {
        return level != null && !level.isClientSide && level.getBlockEntity(pos) instanceof PipeTile;
    }

    private static boolean isConnected(Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockState(pos).getBlock() instanceof PipeBlock block)) {
            System.out.println("[Fluid] " + pos + " не труба");
            return false;
        }

        boolean fromConnected = level.getBlockState(pos).getValue(block.getConnectionProperty(direction));
        System.out.println("[Fluid] " + pos + " -> " + direction + ": " + fromConnected);

        if (!fromConnected) return false;

        BlockPos neighbor = pos.relative(direction);
        if (!(level.getBlockState(neighbor).getBlock() instanceof PipeBlock neighborBlock)) {
            System.out.println("[Fluid] " + neighbor + " не труба, возвращаю true");
            return true;
        }

        boolean toConnected = level.getBlockState(neighbor).getValue(
                neighborBlock.getConnectionProperty(direction.getOpposite())
        );
        System.out.println("[Fluid] " + neighbor + " <- " + direction.getOpposite() + ": " + toConnected);

        return toConnected;
    }

    private static String getKey(Level level, BlockPos pos) {
        return level.dimension().location() + ":" + pos.asLong();
    }

    private static void removeNetworkMappings(FluidNetwork network, Level level) {
        for (BlockPos pos : network.getPipes()) pipeToNetwork.remove(getKey(level, pos), network);
    }

    public static void tickAllNetworks(Level level) {
        if (level == null || level.isClientSide) return;
        processPendingUpdates();
        for (FluidNetwork network : new ArrayList<>(networks(level))) network.tick(level);
    }

    public static void clear() {
        pipeToNetwork.clear();
        networksByLevel.clear();
        pendingUpdates.clear();
    }
}
