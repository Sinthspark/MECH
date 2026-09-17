package com.sinthspark.mech.energy;

import com.sinthspark.mech.blockentity.WireTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.IEnergyStorage;

import java.lang.ref.WeakReference;
import java.util.*;

/** Cached connected component. Topology is rebuilt only when the manager marks it dirty. */
public class EnergyNetwork {
    private final Set<BlockPos> wires = new HashSet<>();
    private final Set<WeakReference<IEnergyStorage>> sources = new HashSet<>();
    private final Set<WeakReference<IEnergyStorage>> sinks = new HashSet<>();

    private List<IEnergyStorage> cachedSources = List.of();
    private List<IEnergyStorage> cachedSinks = List.of();
    private int cachedTotalLoss;
    private int cachedMaxTransfer;
    private boolean dirty = true;

    public void addWire(BlockPos pos) { if (wires.add(pos)) dirty = true; }
    public void removeWire(BlockPos pos) { if (wires.remove(pos)) dirty = true; }
    public void addSource(IEnergyStorage source) {
        if (source != null) {
            sources.add(new WeakReference<>(source));
            dirty = true;
        }
    }

    public void addSink(IEnergyStorage sink) {
        if (sink != null) {
            sinks.add(new WeakReference<>(sink));
            dirty = true;
        }
    }

    public void replaceEndpoints(Collection<IEnergyStorage> newSources, Collection<IEnergyStorage> newSinks) {
        sources.clear();
        sinks.clear();
        for (IEnergyStorage source : newSources) {
            if (source != null) sources.add(new WeakReference<>(source));
        }
        for (IEnergyStorage sink : newSinks) {
            if (sink != null) sinks.add(new WeakReference<>(sink));
        }
        dirty = true;
    }

    public Set<BlockPos> getWires() {
        return Collections.unmodifiableSet(wires);
    }

    public Set<IEnergyStorage> getSources() {
        Set<IEnergyStorage> alive = new HashSet<>();
        for (WeakReference<IEnergyStorage> ref : sources) {
            IEnergyStorage storage = ref.get();
            if (storage != null) {
                alive.add(storage);
            }
        }
        return Collections.unmodifiableSet(alive);
    }

    public Set<IEnergyStorage> getSinks() {
        Set<IEnergyStorage> alive = new HashSet<>();
        for (WeakReference<IEnergyStorage> ref : sinks) {
            IEnergyStorage storage = ref.get();
            if (storage != null) {
                alive.add(storage);
            }
        }
        return Collections.unmodifiableSet(alive);
    }

    public void merge(EnergyNetwork other) {
        if (other == this) return;
        wires.addAll(other.wires);
        sources.addAll(other.sources);
        sinks.addAll(other.sinks);
        dirty = true;
    }

    private void updateCache(Level level) {
        if (!dirty) return;

        sources.removeIf(ref -> ref.get() == null);
        sinks.removeIf(ref -> ref.get() == null);

        cachedSources = sources.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .toList();
        cachedSinks = sinks.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .toList();
        cachedTotalLoss = 0;
        cachedMaxTransfer = Integer.MAX_VALUE;

        for (BlockPos pos : wires) {
            if (level.getBlockEntity(pos) instanceof WireTile tile) {
                cachedTotalLoss = safeAdd(cachedTotalLoss, Math.max(0, tile.getLossPerBlock()));
                cachedMaxTransfer = Math.min(cachedMaxTransfer, Math.max(0, tile.getMaxTransfer()));
            }
        }
        if (cachedMaxTransfer == Integer.MAX_VALUE) cachedMaxTransfer = 0;
        dirty = false;
    }

    /** Transfers energy every server tick; only topology/cache work is deferred. */
    public void tick(Level level) {
        updateCache(level);
        if (cachedSources.isEmpty() || cachedSinks.isEmpty() || cachedMaxTransfer <= 0) {
            setAllWiresEnergized(level, false);
            return;
        }

        int sourceBudget = 0;
        for (IEnergyStorage source : cachedSources) {
            int remaining = cachedMaxTransfer - sourceBudget;
            if (remaining <= 0) break;
            sourceBudget += Math.max(0, source.extractEnergy(remaining, true));
        }
        if (sourceBudget <= 0) {
            setAllWiresEnergized(level, false);
            return;
        }

        int afterLoss = Math.max(0, sourceBudget - cachedTotalLoss);
        if (afterLoss <= 0) {
            setAllWiresEnergized(level, false);
            return;
        }

        int remainingToSend = afterLoss;
        int acceptedTotal = 0;
        for (IEnergyStorage sink : cachedSinks) {
            if (remainingToSend <= 0) break;
            int accepted = Math.max(0, sink.receiveEnergy(remainingToSend, true));
            accepted = Math.min(accepted, remainingToSend);
            acceptedTotal += accepted;
            remainingToSend -= accepted;
        }

        if (acceptedTotal <= 0) {
            setAllWiresEnergized(level, false);
            return;
        }

        int energyToExtract = Math.min(sourceBudget, acceptedTotal + cachedTotalLoss);
        int remainingToExtract = energyToExtract;
        for (IEnergyStorage source : cachedSources) {
            if (remainingToExtract <= 0) break;
            int extracted = Math.min(remainingToExtract, Math.max(0, source.extractEnergy(remainingToExtract, false)));
            remainingToExtract -= extracted;
        }

        int remainingAccepted = acceptedTotal;
        for (IEnergyStorage sink : cachedSinks) {
            if (remainingAccepted <= 0) break;
            int accepted = Math.min(remainingAccepted, Math.max(0, sink.receiveEnergy(remainingAccepted, false)));
            remainingAccepted -= accepted;
        }

        setAllWiresEnergized(level, acceptedTotal > 0);
    }

    private void setAllWiresEnergized(Level level, boolean energized) {
        for (BlockPos pos : wires) {
            if (level.getBlockEntity(pos) instanceof WireTile tile) tile.setEnergized(energized);
        }
    }

    private static int safeAdd(int a, int b) {
        long value = (long) a + b;
        return (int) Math.min(Integer.MAX_VALUE, value);
    }
}