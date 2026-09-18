package com.sinthspark.mech.energy;

import com.sinthspark.mech.blockentity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.*;

/*
 * ⚔️ Арка IX: «Терпеливый страж валов»
 *
 * Прежде страж кинетики звал перестройку сети В ТОТ ЖАСЕ МИГ, когда блок
 * вставали в мир — каскад мгновенных перестроек давил TPS при расстановке
 * длинных линий. Теперь он поступает как его братья (энергия, жидкость):
 * складывает просьбы в очередь и исполняет их одним движением на тике.
 * А ещё сети больше не скитерются между измерениями в общем мешке —
 * у каждого мира своя полка. (◠‿◠✿)
 */
public class KineticNetworkManager {

    private static final Map<String, KineticNetwork> posToNetwork = new HashMap<>();
    // ⚔️ Арка IX: per-level хранилище — сети одного мира не тикают в другом
    private static final Map<Level, Set<KineticNetwork>> networksByLevel = new IdentityHashMap<>();
    private static final Map<Level, Set<BlockPos>> pendingUpdates = new IdentityHashMap<>();

    public static void scheduleNetworkUpdate(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return;
        }

        // ⚔️ Арка IX: терпение вместо импульса — просьба ложится в очередь
        pendingUpdates.computeIfAbsent(level, k -> new HashSet<>()).add(pos.immutable());
    }

    public static void processPendingUpdates() {
        if (pendingUpdates.isEmpty()) return;

        Map<Level, Set<BlockPos>> updates = new IdentityHashMap<>(pendingUpdates);
        pendingUpdates.clear();

        for (Map.Entry<Level, Set<BlockPos>> entry : updates.entrySet()) {
            for (BlockPos pos : entry.getValue()) {
                rebuildNetworkAt(entry.getKey(), pos);
            }
        }
    }

    private static void rebuildNetworkAt(Level level, BlockPos pos) {

        Set<KineticNetwork> affected = new HashSet<>();

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            String key = getKey(level, neighbor);
            KineticNetwork network = posToNetwork.get(key);
            if (network != null) {
                affected.add(network);
            }
        }

        String key = getKey(level, pos);
        KineticNetwork oldNetwork = posToNetwork.get(key);
        if (oldNetwork != null) {
            affected.add(oldNetwork);
        }

        System.out.println("[KNetMgr] rebuildNetworkAt " + pos.toShortString());
        // Удаляем все затронутые сети
        Set<BlockPos> allComponents = new HashSet<>();
        for (KineticNetwork network : affected) {
            allComponents.addAll(network.getComponents());
            // ⚔️ Арка IX: забираем сеть с полки своего мира
            Set<KineticNetwork> levelNetworks = networksByLevel.get(level);
            if (levelNetworks != null) {
                levelNetworks.remove(network);
            }
            for (BlockPos compPos : network.getComponents()) {
                posToNetwork.remove(getKey(level, compPos), network);
            }
        }

        if (isKineticComponent(level, pos)) {
            allComponents.add(pos);
        }

        // Перестраиваем сети из всех компонентов
        Set<BlockPos> unvisited = new HashSet<>(allComponents);
        while (!unvisited.isEmpty()) {
            BlockPos start = unvisited.iterator().next();
            KineticNetwork network = buildNetworkFrom(level, start, unvisited);
            if (network != null) {
                network.redistribute(level);
            }
        }
    }

    private static KineticNetwork buildNetworkFrom(Level level, BlockPos start, Set<BlockPos> allowed) {

        if (!isKineticComponent(level, start)) {
            allowed.remove(start);
            return null;
        }

        KineticNetwork network = new KineticNetwork();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            network.addComponent(pos);
            posToNetwork.put(getKey(level, pos), network);
            allowed.remove(pos);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.contains(neighbor)) continue;
                if (!allowed.contains(neighbor)) continue;
                if (!isKineticComponent(level, neighbor)) continue;
                if (!isConnected(level, pos, neighbor, dir)) {
                    continue;
                }

                visited.add(neighbor);
                queue.add(neighbor);
            }
        }

        // ⚔️ Арка IX: сеть встаёт на полку своего мира
        networksByLevel.computeIfAbsent(level, k -> new HashSet<>()).add(network);
        network.markNeedsRecalculation();

        return network;
    }

    private static boolean isKineticComponent(Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        return be instanceof WaterWheelTile ||
                be instanceof VulcaniteGearTile ||
                be instanceof VulcaniteBearingTile ||
                be instanceof ManualKineticGeneratorTile ||
                be instanceof PressTile ||
                be instanceof OilPressTile ||
                be instanceof QuernTile ||
                be instanceof SawmillTile;
    }

    private static boolean isConnected(Level level, BlockPos from, BlockPos to, Direction dir) {
        var fromBe = level.getBlockEntity(from);
        var toBe = level.getBlockEntity(to);

        if (fromBe == null || toBe == null) return false;

        // Водяное колесо соединяется с подшипником
        if (fromBe instanceof WaterWheelTile && toBe instanceof VulcaniteBearingTile) return true;
        if (fromBe instanceof VulcaniteBearingTile && toBe instanceof WaterWheelTile) return true;

        // ⚔️ Арка XII: откат — мастер уточнил: шестерня стоит ПОД генератором,
        // значит DOWN от генератора к шестерне — правильный закон ворот.
        if (fromBe instanceof ManualKineticGeneratorTile && toBe instanceof VulcaniteGearTile) {
            return dir == Direction.DOWN;
        }
        if (fromBe instanceof VulcaniteGearTile && toBe instanceof ManualKineticGeneratorTile) {
            return dir == Direction.UP;
        }

        // Шестерни соединяются друг с другом (горизонтально)
        if (fromBe instanceof VulcaniteGearTile && toBe instanceof VulcaniteGearTile) {
            return dir.getAxis() != Direction.Axis.Y;
        }

        // Шестерни соединяются с подшипниками
        if (fromBe instanceof VulcaniteGearTile && toBe instanceof VulcaniteBearingTile) return true;
        if (fromBe instanceof VulcaniteBearingTile && toBe instanceof VulcaniteGearTile) return true;

        // Подшипники соединяются друг с другом
        if (fromBe instanceof VulcaniteBearingTile && toBe instanceof VulcaniteBearingTile) return true;

        // Механизмы соединяются с шестернями и подшипниками (горизонтально)
        if (isConsumer(fromBe) && (toBe instanceof VulcaniteGearTile || toBe instanceof VulcaniteBearingTile)) {
            return dir.getAxis() != Direction.Axis.Y;
        }
        if (isConsumer(toBe) && (fromBe instanceof VulcaniteGearTile || fromBe instanceof VulcaniteBearingTile)) {
            return dir.getAxis() != Direction.Axis.Y;
        }

        return false;
    }

    private static boolean isConsumer(Object be) {
        return be instanceof PressTile ||
                be instanceof OilPressTile ||
                be instanceof QuernTile ||
                be instanceof SawmillTile;
    }

    private static String getKey(Level level, BlockPos pos) {
        return level.dimension().location() + ":" + pos.asLong();
    }

    public static void tickAllNetworks(Level level) {
        if (level == null || level.isClientSide) {
            return;
        }

        processPendingUpdates();

        // ⚔️ Арка IX: тикают только сети этого мира — двойного тика больше нет
        Set<KineticNetwork> levelNetworks = networksByLevel.get(level);
        if (levelNetworks == null) return;

        System.out.println("[KNetMgr] tick: networks=" + levelNetworks.size());
        for (KineticNetwork network : new ArrayList<>(levelNetworks)) {
            network.tick(level);
        }
    }

    public static void clear() {
        posToNetwork.clear();
        networksByLevel.clear();
        pendingUpdates.clear();
    }
}
