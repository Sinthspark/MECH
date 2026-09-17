package com.sinthspark.mech.events;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.api.IHeatedBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MECH.MOD_ID)
public class HotBlockEvents {

    public static void checkEntityDamage(Level level, BlockEntity be, float minTemp, float damage) {
        if (be instanceof IHeatedBlock heated && heated.getTemperature() >= minTemp) {
            AABB aabb = new AABB(be.getBlockPos()).inflate(0.1);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);
            for (LivingEntity entity : entities) {
                if (entity.hurtTime == 0) {
                    entity.hurt(level.damageSources().hotFloor(), damage);
                }
            }
        }
    }
}
