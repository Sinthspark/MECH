package com.sinthspark.mech.compat.jei;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.client.screen.*;
import com.sinthspark.mech.recipe.*;
import com.sinthspark.mech.registration.BlockRegistry;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class MechJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MECH.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new PressingCategory(registration.getJeiHelpers().getGuiHelper()),
                new SawingCategory(registration.getJeiHelpers().getGuiHelper()),
                new MillingCategory(registration.getJeiHelpers().getGuiHelper()),
                new OilPressingCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = Minecraft.getInstance().level.getRecipeManager();

        // JSON рецепты
        registration.addRecipes(PressingCategory.PRESSING_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ModRecipes.PRESSING_TYPE.get()));
        registration.addRecipes(SawingCategory.SAWING_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ModRecipes.SAWING_TYPE.get()));
        registration.addRecipes(MillingCategory.MILLING_RECIPE_TYPE,
                recipeManager.getAllRecipesFor(ModRecipes.MILLING_TYPE.get()));

        // КОДОВЫЕ рецепты лесопилки
        List<SawingRecipe> sawingRecipes = new ArrayList<>();

        // Брёвна → доски
        for (Item log : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(log);
            if (id != null && log.builtInRegistryHolder().is(ItemTags.LOGS)) {
                String plankName = id.getPath()
                        .replace("_log", "_planks").replace("_wood", "_planks")
                        .replace("_stem", "_planks").replace("_hyphae", "_planks")
                        .replace("stripped_", "");
                ResourceLocation plankId = new ResourceLocation(id.getNamespace(), plankName);
                Item plankItem = ForgeRegistries.ITEMS.getValue(plankId);
                if (plankItem != null) {
                    sawingRecipes.add(new SawingRecipe(
                            new ResourceLocation(MECH.MOD_ID, "sawing/log_to_planks_" + id.getPath()),
                            Ingredient.of(log), new ItemStack(plankItem, 6), 100));
                }
            }
        }

        // Доски → полублоки
        for (Item plank : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(plank);
            if (id != null && plank.builtInRegistryHolder().is(ItemTags.PLANKS)) {
                String slabName = id.getPath().replace("_planks", "_slab");
                ResourceLocation slabId = new ResourceLocation(id.getNamespace(), slabName);
                Item slabItem = ForgeRegistries.ITEMS.getValue(slabId);
                if (slabItem != null) {
                    sawingRecipes.add(new SawingRecipe(
                            new ResourceLocation(MECH.MOD_ID, "sawing/planks_to_slab_" + id.getPath()),
                            Ingredient.of(plank), new ItemStack(slabItem, 2), 80));
                }
            }
        }

        // Полублоки → палки
        for (Item slab : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(slab);
            if (id != null && slab.builtInRegistryHolder().is(ItemTags.WOODEN_SLABS)) {
                sawingRecipes.add(new SawingRecipe(
                        new ResourceLocation(MECH.MOD_ID, "sawing/slab_to_sticks_" + id.getPath()),
                        Ingredient.of(slab), new ItemStack(Items.STICK, 4), 60));
            }
        }

        // Палки → дуга колеса
        sawingRecipes.add(new SawingRecipe(
                new ResourceLocation(MECH.MOD_ID, "sawing/sticks_to_arc"),
                Ingredient.of(Items.STICK),
                new ItemStack(ItemRegistry.WHEEL_ARC_WITHOUT_STICK.get(), 1), 120));

        registration.addRecipes(SawingCategory.SAWING_RECIPE_TYPE, sawingRecipes);

        // КОДОВЫЕ рецепты маслопресса (с жидкостью)
        // Исправлено: получаем ведро с маслом через FluidUtil
        ItemStack rapeseedOilBucket = FluidUtil.getFilledBucket(
                new FluidStack(FluidRegistry.RAPESEED_OIL.source.get(), 1000)
        );

        if (rapeseedOilBucket.isEmpty()) {
            // Запасной вариант: ищем предмет-ведро с этой жидкостью
            Optional<Item> bucketItem = ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> item.getDescriptionId().contains("rapeseed_oil_bucket"))
                    .findFirst();

            if (bucketItem.isPresent()) {
                rapeseedOilBucket = new ItemStack(bucketItem.get());
            } else {
                // Если ведро не найдено, используем пустой ItemStack
                rapeseedOilBucket = ItemStack.EMPTY;
            }
        }

        registration.addRecipes(OilPressingCategory.OIL_PRESSING_RECIPE_TYPE, List.of(
                new OilPressingRecipe(
                        new ResourceLocation(MECH.MOD_ID, "oil_pressing/rapeseed_cleaned"),
                        Ingredient.of(ItemRegistry.RAPESEED_CLEANED.get()),
                        rapeseedOilBucket, 80)
        ));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.PRESS.get().asItem()), PressingCategory.PRESSING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.SAWMILL.get().asItem()), SawingCategory.SAWING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.QUERN.get().asItem()), MillingCategory.MILLING_RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.OIL_PRESS.get().asItem()), OilPressingCategory.OIL_PRESSING_RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PressScreen.class, 79, 35, 25, 16, PressingCategory.PRESSING_RECIPE_TYPE);
        registration.addRecipeClickArea(SawmillScreen.class, 79, 35, 25, 16, SawingCategory.SAWING_RECIPE_TYPE);
        registration.addRecipeClickArea(QuernScreen.class, 79, 35, 25, 16, MillingCategory.MILLING_RECIPE_TYPE);
        registration.addRecipeClickArea(OilPressScreen.class, 79, 35, 25, 16, OilPressingCategory.OIL_PRESSING_RECIPE_TYPE);
    }
}