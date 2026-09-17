package com.sinthspark.mech.energy;

import com.sinthspark.mech.blockentity.WireTile;
import com.sinthspark.mech.blocks.WireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.*;

public final class EnergyNetworkManager {
    private static final Map<String, EnergyNetwork> wireToNetwork = new HashMap<>();
    private static final Map<Level, Set<EnergyNetwork>> networksByLevel = new IdentityHashMap<>();
    private static final Map<Level, Set<BlockPos>> pendingUpdates = new IdentityHashMap<>();

    private EnergyNetworkManager() {}

    private static Set<EnergyNetwork> networks(Level level) {
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
            if (isWire(level, pos)) rebuildNetworkAt(level, pos);
            else onWireRemoved(level, pos);
        }
    }

    private static void rebuildNetworkAt(Level level, BlockPos pos) {
        if (!isWire(level, pos)) {
            onWireRemoved(level, pos);
            return;
        }

        Set<BlockPos> allWires = new HashSet<>();
        Set<EnergyNetwork> affected = getNeighborNetworks(level, pos);
        EnergyNetwork old = wireToNetwork.get(getKey(level, pos));
        if (old != null) affected.add(old);

        for (EnergyNetwork network : affected) {
            allWires.addAll(network.getWires());
            removeNetworkMappings(network, level);
            networks(level).remove(network);
        }
        allWires.add(pos);

        Set<BlockPos> unvisited = new HashSet<>(allWires);
        while (!unvisited.isEmpty()) {
            BlockPos seed = unvisited.iterator().next();
            EnergyNetwork network = buildNetworkFrom(level, seed, unvisited, true);
            if (network != null) {
                networks(level).add(network);
            }
        }
    }

    public static void onWirePlaced(Level level, BlockPos wirePos) {
        if (!isWire(level, wirePos)) return;
        Set<EnergyNetwork> neighbors = getNeighborNetworks(level, wirePos);
        EnergyNetwork network = new EnergyNetwork();
        for (EnergyNetwork neighbor : neighbors) {
            network.merge(neighbor);
            removeNetworkMappings(neighbor, level);
            networks(level).remove(neighbor);
        }
        networks(level).add(network);
        for (BlockPos pos : network.getWires()) wireToNetwork.put(getKey(level, pos), network);
        network.addWire(wirePos);
        wireToNetwork.put(getKey(level, wirePos), network);
        rescanSourcesAndSinks(level, network);
    }

    public static void onWireRemoved(Level level, BlockPos wirePos) {
        if (level == null || level.isClientSide) return;
        EnergyNetwork old = wireToNetwork.get(getKey(level, wirePos));
        if (old == null) return;
        removeNetworkMappings(old, level);
        networks(level).remove(old);
        old.removeWire(wirePos);
        Set<BlockPos> unvisited = new HashSet<>(old.getWires());
        while (!unvisited.isEmpty()) buildNetworkFrom(level, unvisited.iterator().next(), unvisited, true);
    }

    public static void onWireNeighborChanged(Level level, BlockPos wirePos) {
        scheduleNetworkUpdate(level, wirePos);
    }

    private static EnergyNetwork buildNetworkFrom(Level level, BlockPos start, Set<BlockPos> allowed, boolean addToManager) {
        if (!isWire(level, start)) return null;
        EnergyNetwork network = new EnergyNetwork();
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove();
            network.addWire(pos);
            wireToNetwork.put(getKey(level, pos), network);
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

    private static void rescanSourcesAndSinks(Level level, EnergyNetwork network) {
        Set<IEnergyStorage> sources = new HashSet<>();
        Set<IEnergyStorage> sinks = new HashSet<>();
        for (BlockPos wirePos : network.getWires()) {
            for (Direction direction : Direction.values()) {
                if (!isConnected(level, wirePos, direction)) continue;
                var be = level.getBlockEntity(wirePos.relative(direction));
                if (be == null || be instanceof WireTile) continue;
                be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).resolve().ifPresent(storage -> {
                    if (storage.canExtract()) sources.add(storage);
                    if (storage.canReceive()) sinks.add(storage);
                });
            }
        }
        network.replaceEndpoints(sources, sinks);
    }

    private static Set<EnergyNetwork> getNeighborNetworks(Level level, BlockPos wirePos) {
        Set<EnergyNetwork> result = new HashSet<>();
        for (Direction direction : Direction.values()) {
            if (!isConnected(level, wirePos, direction)) continue;
            EnergyNetwork network = wireToNetwork.get(getKey(level, wirePos.relative(direction)));
            if (network != null) result.add(network);
        }
        return result;
    }

    private static boolean isWire(Level level, BlockPos pos) {
        return level != null && !level.isClientSide && level.getBlockEntity(pos) instanceof WireTile;
    }

    private static boolean isConnected(Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockState(pos).getBlock() instanceof WireBlock block)) return false;
        if (!level.getBlockState(pos).getValue(block.getConnectionProperty(direction))) return false;
        BlockPos neighbor = pos.relative(direction);
        if (!(level.getBlockState(neighbor).getBlock() instanceof WireBlock neighborBlock)) return true;
        return level.getBlockState(neighbor).getValue(neighborBlock.getConnectionProperty(direction.getOpposite()));
    }

    private static String getKey(Level level, BlockPos pos) {
        return level.dimension().location() + ":" + pos.asLong();
    }

    private static void removeNetworkMappings(EnergyNetwork network, Level level) {
        for (BlockPos pos : network.getWires()) wireToNetwork.remove(getKey(level, pos), network);
    }

    public static void tickAllNetworks(Level level) {
        if (level == null || level.isClientSide) return;
        processPendingUpdates();
        for (EnergyNetwork network : new ArrayList<>(networks(level))) network.tick(level);
    }

    public static void clear() {
        wireToNetwork.clear();
        networksByLevel.clear();
        pendingUpdates.clear();
    }
}