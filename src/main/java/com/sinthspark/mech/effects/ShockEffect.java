package com.sinthspark.mech.effects;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class ShockEffect extends MobEffect {

    public ShockEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFF00);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            dropBothHands(player);
        } else if (entity instanceof EnderMan enderman) {
            enderman.setCarriedBlock(Blocks.AIR.defaultBlockState());
        } else if (entity instanceof Allay allay) {
            allay.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else if (entity instanceof Fox fox) {
            fox.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else if (entity instanceof Panda panda) {
            panda.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else if (entity instanceof PiglinBrute
                || entity instanceof Vindicator
                || entity instanceof Pillager) {
            dropMainHand(entity);
        } else if (entity instanceof Zombie
                || entity instanceof ZombifiedPiglin
                || entity instanceof Piglin) {
            // Покрывает Drowned, Husk, ZombieVillager — всех наследников Zombie
            dropMainHand(entity);
        } else if (entity instanceof Skeleton
                || entity instanceof WitherSkeleton
                || entity instanceof Stray) {
            dropMainHand(entity);
        }
        // Villager, WanderingTrader, Witch — без выброса
    }

    private void dropMainHand(@NotNull LivingEntity entity) {
        ItemStack held = entity.getMainHandItem();
        if (!held.isEmpty()) {
            entity.spawnAtLocation(held.copy());
            entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    private void dropBothHands(@NotNull Player player) {
        dropHand(player, InteractionHand.MAIN_HAND);
        dropHand(player, InteractionHand.OFF_HAND);
    }

    private void dropHand(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            player.drop(held.copy(), true);
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = Math.max(20 / (amplifier + 1), 1);
        return duration % interval == 0;
    }
}