package com.sinthspark.mech.energy;

import com.sinthspark.mech.blockentity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class KineticNetwork {
    private final Set<BlockPos> components = new HashSet<>();
    private final Set<BlockPos> sources = new HashSet<>();
    private final Set<BlockPos> consumers = new HashSet<>();
    private final Set<BlockPos> transmitters = new HashSet<>();
    private float cachedTotalPower = 0;
    private float cachedSpeed = 0;
    private boolean dirty = true;
    private boolean needsRecalculation = false;
    private int networkId = 0;
    private static int nextNetworkId = 0;

    public KineticNetwork() { this.networkId = nextNetworkId++; }
    public void addComponent(BlockPos pos) { if (components.add(pos)) dirty = true; }
    public void removeComponent(BlockPos pos) { if (components.remove(pos)) dirty = true; }
    public void markDirty() { dirty = true; needsRecalculation = true; }
    public void markNeedsRecalculation() { needsRecalculation = true; }
    public boolean needsRecalculation() { return needsRecalculation; }

    public void redistribute(Level level) {
        if (!needsRecalculation) return;
        updateCache(level);
        if (cachedTotalPower <= 0) {
            deactivateAll(level);
            needsRecalculation = false;
            return;
        }

        float powerPerConsumer = cachedTotalPower / Math.max(1, consumers.size());
        // ⚔️ Арка X: скорость от РЕАЛЬНОЙ доли — кламп тугим потребителем.
        // Кузнец поймал ошибку компиляции: потребители — BlockPos разных
        // типов (Press/OilPress/Quern/Sawmill), а не список прессов~
        float clampedPower = powerPerConsumer;
        for (BlockPos consumerPos0 : consumers) {
            var be0 = level.getBlockEntity(consumerPos0);
            if (be0 instanceof PressTile p0) clampedPower = Math.min(clampedPower, p0.maxPower);
            else if (be0 instanceof OilPressTile op0) clampedPower = Math.min(clampedPower, op0.maxPower);
        }
        cachedSpeed = calculateSpeed(clampedPower);
        float perTick = cachedSpeed / 20.0f;

        for (BlockPos consumerPos : consumers) {
            var be = level.getBlockEntity(consumerPos);
            if (be instanceof PressTile press) {
                float deliveredPower = Math.min(powerPerConsumer, press.maxPower);
                press.inputPower = deliveredPower;
                press.perTick = calculateSpeed(deliveredPower) / 20.0f;
                press.leftActive = deliveredPower >= press.minPower;
                press.rightActive = deliveredPower >= press.minPower;
            } else if (be instanceof OilPressTile oilPress) {
                float deliveredPower = Math.min(powerPerConsumer, oilPress.maxPower);
                oilPress.inputPower = deliveredPower;
                oilPress.perTick = calculateSpeed(deliveredPower) / 20.0f;
            } else if (be instanceof QuernTile quern) {
                quern.inputPower = powerPerConsumer;
                quern.perTick = perTick;
            } else if (be instanceof SawmillTile sawmill) {
                sawmill.inputPower = powerPerConsumer;
                sawmill.perTick = perTick;
            }
            // ⚔️ Арка XIII: потребители тоже шлют весточку клиенту
            syncToClient(level, consumerPos);
        }

        for (BlockPos transmitterPos : transmitters) {
            var be = level.getBlockEntity(transmitterPos);
            if (be instanceof VulcaniteGearTile gear) {
                gear.inputPower = cachedTotalPower;
                gear.perTick = perTick;
                gear.isActive = true;
            } else if (be instanceof VulcaniteBearingTile bearing) {
                bearing.inputPower = cachedTotalPower;
                bearing.outputPower = cachedTotalPower * VulcaniteBearingTile.BEARING_EFFICIENCY;
                bearing.perTick = perTick;
                bearing.isActive = true;
            }
            // ⚔️ Арка XIII: шлём весточку клиенту — без неё шестерня
            // не крутится визуально, хотя на сервере жива
            syncToClient(level, transmitterPos);
        }
        needsRecalculation = false;
    }

    private void updateCache(Level level) {
        if (!dirty) return;
        sources.clear();
        consumers.clear();
        transmitters.clear();
        cachedTotalPower = 0;
        for (BlockPos pos : components) {
            var be = level.getBlockEntity(pos);
            if (be == null) continue;
            if (be instanceof WaterWheelTile wheel) {
                if (wheel.isActive) { sources.add(pos); cachedTotalPower += wheel.power; }
            } else if (be instanceof ManualKineticGeneratorTile gen) {
                if (gen.isActive) { sources.add(pos); cachedTotalPower += gen.getPower(); }
            } else if (be instanceof PressTile) consumers.add(pos);
            else if (be instanceof OilPressTile) consumers.add(pos);
            else if (be instanceof QuernTile) consumers.add(pos);
            else if (be instanceof SawmillTile) consumers.add(pos);
            else if (be instanceof VulcaniteGearTile) transmitters.add(pos);
            else if (be instanceof VulcaniteBearingTile) transmitters.add(pos);
        }
        dirty = false;
    }


    // ⚔️ Арка XIII: «Забытое послание» — БАГФИКС.
    // Сеть будила шестерни и потребителей на сервере, но не слала пакет
    // клиенту — tile.isActive оставался false на клиенте, и шестерня
    // визуально не крутилась (генератор синкал сам, поэтому крутился один).
    private void syncToClient(Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (be != null) be.setChanged();
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private float calculateSpeed(float power) {
        if (power <= 0) return 0;
        return Math.min(60, Math.max(5, power / 10));
    }

    private void deactivateAll(Level level) {
        for (BlockPos pos : components) {
            // ⚔️ Арка XIII: гасим — и СРАЗУ шлём весточку клиенту
            var be = level.getBlockEntity(pos);
            if (be instanceof VulcaniteGearTile gear) {
                gear.isActive = false; gear.inputPower = 0; gear.perTick = 0;
            } else if (be instanceof VulcaniteBearingTile bearing) {
                bearing.isActive = false; bearing.inputPower = 0; bearing.outputPower = 0; bearing.perTick = 0;
            } else if (be instanceof PressTile press) {
                press.inputPower = 0; press.perTick = 0; press.leftActive = false; press.rightActive = false;
            } else if (be instanceof OilPressTile oilPress) {
                oilPress.inputPower = 0; oilPress.perTick = 0;
            } else if (be instanceof QuernTile quern) {
                quern.inputPower = 0; quern.perTick = 0;
            } else if (be instanceof SawmillTile sawmill) {
                sawmill.inputPower = 0; sawmill.perTick = 0;
            }
        }
        for (BlockPos pos : components) syncToClient(level, pos);
    }

    public void tick(Level level) { if (needsRecalculation) redistribute(level); }
    public Set<BlockPos> getComponents() { return Collections.unmodifiableSet(components); }
}
