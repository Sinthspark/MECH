package com.sinthspark.mech.events;

import com.sinthspark.mech.api.IHeatedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class BoilerBreakHandler {

    private static final float EXPLOSION_TEMP_THRESHOLD = 150.0f;
    private static final float EXPLOSION_POWER = 4.5f;
    private static final float DIRECT_DAMAGE = 7.0f;

    @SubscribeEvent
    @SuppressWarnings("resource")
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isSpectator() || player.isCreative() || player.tickCount < 40) return;

        Level level = player.level();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof IHeatedBlock heated) || heated.getTemperature() < EXPLOSION_TEMP_THRESHOLD) {
            return;
        }

        event.setCanceled(true);
        serverLevel.destroyBlock(pos, false);

        serverLevel.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                EXPLOSION_POWER, true, Level.ExplosionInteraction.TNT);

        AABB area = new AABB(pos).inflate(7.0);
        List<Player> players = level.getEntitiesOfClass(Player.class, area);

        for (Player target : players) {
            double dist = target.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (dist < 49.0) {
                target.hurt(level.damageSources().explosion(null), DIRECT_DAMAGE);
            }
        }
    }
}