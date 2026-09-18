package com.sinthspark.mech.energy;

import com.sinthspark.mech.blockentity.*;
import net.minecraft.core.BlockPos;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class KineticNetwork {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
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

        // Единый источник правды: deliveredPower каждого потребителя считается
        // ОДИН раз. Скорость сети определяется самым "тугим" потребителем,
        // и все вращаются с одной скоростью — один вал, один RPM.
        Map<BlockPos, Float> delivered = new HashMap<>();
        float networkPower = powerPerConsumer;
        for (BlockPos consumerPos : consumers) {
            var be = level.getBlockEntity(consumerPos);
            float d = powerPerConsumer;
            if (be instanceof PressTile press) d = Math.min(d, press.maxPower);
            else if (be instanceof OilPressTile oilPress) d = Math.min(d, oilPress.maxPower);
            delivered.put(consumerPos, d);
            networkPower = Math.min(networkPower, d);
        }
        cachedSpeed = calculateSpeed(networkPower);
        float perTick = cachedSpeed / 20.0f;

        for (BlockPos consumerPos : consumers) {
            var be = level.getBlockEntity(consumerPos);
            float d = delivered.get(consumerPos);
            if (be instanceof PressTile press) {
                press.inputPower = d;
                press.perTick = perTick;
                press.leftActive = d >= press.minPower;
                press.rightActive = d >= press.minPower;
            } else if (be instanceof OilPressTile oilPress) {
                oilPress.inputPower = d;
                oilPress.perTick = perTick;
            } else if (be instanceof QuernTile quern) {
                quern.inputPower = d;
                quern.perTick = perTick;
            } else if (be instanceof SawmillTile sawmill) {
                sawmill.inputPower = d;
                sawmill.perTick = perTick;
            }
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
        LOGGER.info("[KNet] redistribute: power=" + cachedTotalPower
                + " speed=" + cachedSpeed + " perTick=" + perTick
                + " transmitters=" + transmitters.size());
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
        LOGGER.info("[KNet] updateCache: sources=" + sources.size()
                + " transmitters=" + transmitters.size() + " consumers=" + consumers.size()
                + " totalPower=" + cachedTotalPower);
    }


    // Синхронизация сервер -> клиент.
    // Работает при ОДИНАКОВОМ BlockState, потому что в Forge 1.20.1 пакет
    // данных BlockEntity уходит через BlockEntity.getUpdatePacket()
    // независимо от равенства стейтов. Все тайлы сети реализуют цепочку:
    // getUpdatePacket -> getUpdateTag (isActive, perTick) -> handleUpdateTag.
    // Если какой-то тайл потеряет эту цепочку — синк молча сломается.
    private void syncToClient(Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        LOGGER.info("[KNet] syncToClient " + pos.toShortString()
                + " be=" + (be == null ? "NULL!" : be.getClass().getSimpleName()));
        if (be == null) return;
        be.setChanged();
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
        // Страховка: прямая рассылка пакета всем игрокам измерения.
        // Если sendBlockUpdated с одинаковым стейтом проглатывается — этот путь довезёт.
        if (level instanceof ServerLevel serverLevel) {
            var packet = be.getUpdatePacket();
            String status = (be instanceof com.sinthspark.mech.blockentity.VulcaniteGearTile gear)
                    ? ("isActive=" + gear.isActive + " perTick=" + gear.perTick)
                    : be.getClass().getSimpleName();
            for (ServerPlayer player : serverLevel.players()) {
                if (packet != null) {
                    player.connection.send(packet);
                }
                // Диагностика прямо в игру: action bar над хотбаром.
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "[KNet] синк " + pos.toShortString() + " " + status), true);
            }
        }
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
        // Синкаем только то, что реально меняли: передатчики и потребители.
        // Источники (колесо, генератор) в этом методе не трогаем.
        for (BlockPos pos : transmitters) syncToClient(level, pos);
        for (BlockPos pos : consumers) syncToClient(level, pos);
    }

    public void tick(Level level) { if (needsRecalculation) redistribute(level); }
    public Set<BlockPos> getComponents() { return Collections.unmodifiableSet(components); }
}
