package com.sinthspark.mech;

import com.sinthspark.mech.registration.ItemRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class ModTiers {
    // Уровни: 0=wood, 1=stone, 2=iron, 3=diamond, 4=netherite

    public static final Tier TIN = new ForgeTier(
            1, // уровень (как камень)
            70, // прочность
            5.0F, // скорость
            1.0F, // урон
            10, // enchantability
            BlockTags.NEEDS_STONE_TOOL,
            () -> Ingredient.of(ItemRegistry.TIN_INGOT.get())
    );

    public static final Tier COPPER = new ForgeTier(
            2, // уровень (как железо)
            180, // прочность
            6.0F, // скорость
            2.0F, // урон
            12, // enchantability
            BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.of(Items.COPPER_INGOT)
    );

    public static final Tier BRONZE = new ForgeTier(
            2, // уровень (как железо)
            280, // прочность
            7.0F, // скорость
            2.0F, // урон
            14, // enchantability
            BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.of(ItemRegistry.BRONZE_INGOT.get())
    );
}
