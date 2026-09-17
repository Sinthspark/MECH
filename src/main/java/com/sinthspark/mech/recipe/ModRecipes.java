package com.sinthspark.mech.recipe;

import com.sinthspark.mech.MECH;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MECH.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MECH.MOD_ID);

    public static final RegistryObject<RecipeType<PressingRecipe>> PRESSING_TYPE =
            RECIPE_TYPES.register("pressing", () -> RecipeType.simple(new ResourceLocation(MECH.MOD_ID, "pressing")));

    public static final RegistryObject<RecipeSerializer<PressingRecipe>> PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("pressing", PressingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<SawingRecipe>> SAWING_TYPE =
            RECIPE_TYPES.register("sawing", () -> RecipeType.simple(new ResourceLocation(MECH.MOD_ID, "sawing")));

    public static final RegistryObject<RecipeSerializer<SawingRecipe>> SAWING_SERIALIZER =
            RECIPE_SERIALIZERS.register("sawing", SawingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<MillingRecipe>> MILLING_TYPE =
            RECIPE_TYPES.register("milling", () -> RecipeType.simple(new ResourceLocation(MECH.MOD_ID, "milling")));

    public static final RegistryObject<RecipeSerializer<MillingRecipe>> MILLING_SERIALIZER =
            RECIPE_SERIALIZERS.register("milling", MillingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<OilPressingRecipe>> OIL_PRESSING_TYPE =
            RECIPE_TYPES.register("oil_pressing", () -> RecipeType.simple(new ResourceLocation(MECH.MOD_ID, "oil_pressing")));

    public static final RegistryObject<RecipeSerializer<OilPressingRecipe>> OIL_PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("oil_pressing", OilPressingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<AlloyingRecipe>> ALLOYING_TYPE =
            RECIPE_TYPES.register("alloying", () -> new RecipeType<>() {});

    public static final RegistryObject<RecipeSerializer<AlloyingRecipe>> ALLOYING_SERIALIZER =
            RECIPE_SERIALIZERS.register("alloying", AlloyingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<GrindingRecipe>> GRINDING_TYPE =
            RECIPE_TYPES.register("grinding", () -> new RecipeType<>() {});

    public static final RegistryObject<RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", GrindingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<CompressingRecipe>> COMPRESSING_TYPE =
            RECIPE_TYPES.register("compressing", () -> new RecipeType<>() {});

    public static final RegistryObject<RecipeSerializer<CompressingRecipe>> COMPRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("compressing", CompressingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<ExtractingRecipe>> EXTRACTING_TYPE =
            RECIPE_TYPES.register("extracting", () -> new RecipeType<>() {});

    public static final RegistryObject<RecipeSerializer<ExtractingRecipe>> EXTRACTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("extracting", ExtractingRecipe.Serializer::new);
}
