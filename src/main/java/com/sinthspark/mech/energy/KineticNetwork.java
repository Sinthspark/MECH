package com.sinthspark.mech.energy;

import com.sinthspark.mech.blockentity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class KineticNetwork {
    private static final Logger LOGGER = LoggerFactory.getLogger("MECH/KineticNetwork");

    private final Set<BlockPos> components = new HashSet<>();
    private final Set<BlockPos> sources = new HashSet<>();
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

    /*
     * Обход графа от источника. На каждом узле мощность идёт дальше по
     * реальным связям (те же правила, что строят саму сеть — see
     * KineticNetworkManager.isConnected). На развилке мощность делится
     * поровну на количество непосещённых соседей (аналог "div a" из доки,
     * только float-делением — без молчаливой потери на округлении).
     * Затем к доле применяется СОБСТВЕННЫЙ коэффициент узла, с которого
     * мощность УХОДИТ дальше (k/F_тр) — ровно то же правило, что делает
     * серию P_ш_m = P_г×(k_г/F_тр_г)×(k_ш/F_тр_ш)^(m-1) и параллель
     * P_ш_(m+1) = (P_ш_m div a)×(k_ш/F_тр_ш) частными случаями одной
     * и той же рекурсии.
     *
     * ВАЖНО: это однопроходный BFS "первый пришёл — застолбил узел".
     * Если в одной сети несколько активных источников сходятся в одной
     * точке (два колеса на одну шестерню), мощность второго источника
     * в этой точке потеряется — второй BFS не тронет уже посещённый узел.
     * Для одного источника на сеть (текущий геймплей) это не проявляется.
     * Если понадобится честный мердж нескольких источников — нужно
     * накапливать входящую мощность в узле ДО его обработки, а не
     * забирать первым пришедшим; это отдельная задача.
     */
    public void redistribute(Level level) {
        if (!needsRecalculation) return;
        updateCache(level);

        Map<BlockPos, Float> outgoing = new HashMap<>();
        Map<BlockPos, Float> direction = new HashMap<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        for (BlockPos pos : sources) {
            var be = level.getBlockEntity(pos);
            float leaving = 0;
            if (be instanceof WaterWheelTile wheel && wheel.isActive) {
                // P_к у колеса уже содержит k_к/F_тр_к внутри своей формулы —
                // повторно коэффициент не накладываем, иначе потери удвоятся.
                leaving = wheel.getPower();
            } else if (be instanceof ManualKineticGeneratorTile gen && gen.isActive) {
                float raw = gen.getPower();
                leaving = applyOwnFactor(raw, ManualKineticGeneratorTile.EFFICIENCY,
                        ManualKineticGeneratorTile.FRICTION, "ManualKineticGenerator");
            }
            if (leaving > 0) {
                outgoing.put(pos, leaving);
                direction.put(pos, 1.0f); // базовое направление источника, дальше только флипается
                visited.add(pos);
                queue.add(pos);
            }
        }

        if (queue.isEmpty()) {
            deactivateAll(level);
            needsRecalculation = false;
            return;
        }

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            float incoming = outgoing.get(pos);
            float dirHere = direction.getOrDefault(pos, 1.0f);
            var posBe = level.getBlockEntity(pos);

            List<BlockPos> next = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.contains(neighbor)) continue;
                if (!components.contains(neighbor)) continue;
                if (!KineticNetworkManager.isConnected(level, pos, neighbor, dir)) continue;
                next.add(neighbor);
            }

            int branches = Math.max(1, next.size());
            float share = incoming / branches;

            for (BlockPos n : next) {
                visited.add(n);
                var nbe = level.getBlockEntity(n);

                // Флип направления только на зацеплении шестерня-шестерня —
                // это единственное реальное меш-соединение в системе.
                // Подшипник — вал, а не зубчатая пара, направление сквозь
                // него передаётся без изменений.
                boolean isGearMesh = posBe instanceof VulcaniteGearTile && nbe instanceof VulcaniteGearTile;
                float nextDir = isGearMesh ? -dirHere : dirHere;
                direction.put(n, nextDir);

                if (nbe instanceof VulcaniteGearTile gear) {
                    gear.inputPower = share;
                    gear.isActive = true;
                    gear.rotationDirection = nextDir;
                    gear.perTick = gear.getSpeed() / 20.0f;
                    float leaving = applyOwnFactor(share, VulcaniteGearTile.EFFICIENCY,
                            gear.getFriction(), "VulcaniteGear @ " + n);
                    outgoing.put(n, leaving);
                    queue.add(n);
                    syncToClient(level, n);

                } else if (nbe instanceof VulcaniteBearingTile bearing) {
                    bearing.inputPower = share;
                    bearing.isActive = true;
                    bearing.perTick = bearing.getSpeed() / 20.0f;
                    // getPower() у подшипника уже своя (вычитающая) модель потерь —
                    // не накладываем k/F поверх неё, как и с колесом.
                    outgoing.put(n, bearing.getPower());
                    queue.add(n);
                    syncToClient(level, n);

                } else if (nbe instanceof PressTile press) {
                    float delivered = applyOwnFactor(share, PressTile.PRESS_EFFICIENCY,
                            press.getFriction(), "Press @ " + n);
                    press.inputPower = Math.min(delivered, press.maxPower);
                    press.leftActive = press.inputPower >= press.minPower;
                    press.rightActive = press.inputPower >= press.minPower;
                    syncToClient(level, n);

                } else if (nbe instanceof OilPressTile oilPress) {
                    float delivered = applyOwnFactor(share, OilPressTile.OIL_PRESS_EFFICIENCY,
                            oilPress.getFriction(), "OilPress @ " + n);
                    oilPress.inputPower = Math.min(delivered, oilPress.maxPower);
                    syncToClient(level, n);

                } else if (nbe instanceof QuernTile quern) {
                    quern.inputPower = applyOwnFactor(share, QuernTile.QUERN_EFFICIENCY,
                            quern.getFriction(), "Quern @ " + n);
                    syncToClient(level, n);

                } else if (nbe instanceof SawmillTile sawmill) {
                    sawmill.inputPower = applyOwnFactor(share, SawmillTile.SAWMILL_EFFICIENCY,
                            sawmill.getFriction(), "Sawmill @ " + n);
                    syncToClient(level, n);
                }
            }
        }

        needsRecalculation = false;
    }

    /**
     * leaving = incoming × (k / F_тр). Если k ≥ F_тр, узел не теряет
     * мощность при передаче, а усиливает её — при повторении по цепочке
     * это perpetuum mobile. Логируем один раз и на всякий случай режем
     * коэффициент до 1, чтобы конфиг с опечаткой не превратился в
     * бесконечный источник энергии в мультиплеере.
     */
    private float applyOwnFactor(float incoming, float k, float frictionTr, String where) {
        if (frictionTr <= 0) return 0;
        float ratio = k / frictionTr;
        if (ratio >= 1.0f) {
            LOGGER.warn("[MECH] {} имеет k/F_тр = {} >= 1 — мощность будет усиливаться, а не теряться! " +
                    "Коэффициент временно обрезан до 1.0, поправь баланс в конфиге.", where, ratio);
            ratio = 1.0f;
        }
        return incoming * ratio;
    }

    private void updateCache(Level level) {
        if (!dirty) return;
        sources.clear();
        for (BlockPos pos : components) {
            var be = level.getBlockEntity(pos);
            if (be == null) continue;
            if (be instanceof WaterWheelTile wheel) {
                if (wheel.isActive) sources.add(pos);
            } else if (be instanceof ManualKineticGeneratorTile gen) {
                if (gen.isActive) sources.add(pos);
            }
        }
        dirty = false;
    }

    private void syncToClient(Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (be != null) be.setChanged();
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private void deactivateAll(Level level) {
        for (BlockPos pos : components) {
            var be = level.getBlockEntity(pos);
            boolean wasRelevant = false;
            if (be instanceof VulcaniteGearTile gear) {
                gear.isActive = false; gear.inputPower = 0; gear.perTick = 0;
                wasRelevant = true;
            } else if (be instanceof VulcaniteBearingTile bearing) {
                bearing.isActive = false; bearing.inputPower = 0; bearing.outputPower = 0; bearing.perTick = 0;
                wasRelevant = true;
            } else if (be instanceof PressTile press) {
                press.inputPower = 0; press.perTick = 0; press.leftActive = false; press.rightActive = false;
                wasRelevant = true;
            } else if (be instanceof OilPressTile oilPress) {
                oilPress.inputPower = 0; oilPress.perTick = 0;
                wasRelevant = true;
            } else if (be instanceof QuernTile quern) {
                quern.inputPower = 0; quern.perTick = 0;
                wasRelevant = true;
            } else if (be instanceof SawmillTile sawmill) {
                sawmill.inputPower = 0; sawmill.perTick = 0;
                wasRelevant = true;
            }
            if (wasRelevant) syncToClient(level, pos);
        }
    }

    public void tick(Level level) { if (needsRecalculation) redistribute(level); }
    public Set<BlockPos> getComponents() { return Collections.unmodifiableSet(components); }
}
