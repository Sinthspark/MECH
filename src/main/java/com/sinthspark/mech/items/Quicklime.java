package com.sinthspark.mech.items;

import com.sinthspark.mech.registration.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Quicklime extends Item {

    private static final String TAG_WATER_START = "WaterStartTime";
    private static final int TICKS_TO_SLAKE = 100; // 5 секунд

    public Quicklime(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(@NotNull ItemStack stack, @NotNull ItemEntity entity) {
        @SuppressWarnings("resource")
        Level level = entity.level();
        if (level.isClientSide) return false;

        if (entity.isInWater()) {
            CompoundTag tag = entity.getPersistentData();
            long gameTime = level.getGameTime();

            if (!tag.contains(TAG_WATER_START)) {
                tag.putLong(TAG_WATER_START, gameTime);
            } else if (gameTime - tag.getLong(TAG_WATER_START) >= TICKS_TO_SLAKE) {
                int count = stack.getCount();
                entity.setItem(new ItemStack(ItemRegistry.SLAKED_LIME.get(), count));
                level.playSound(null, entity.blockPosition(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.0F);
                return true;
            }
        } else {
            entity.getPersistentData().remove(TAG_WATER_START);
        }

        return false;
    }
}