package com.sinthspark.mech.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SawingRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int cookingTime;

    public SawingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int cookingTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(@NotNull SimpleContainer container, @NotNull Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public @NotNull ItemStack assemble(@NotNull SimpleContainer container, @NotNull RegistryAccess registryAccess) {
        ItemStack input = container.getItem(0);
        ItemStack planks = getPlanksForLog(input);
        if (!planks.isEmpty()) {
            planks.setCount(6);
            return planks;
        }
        return result.copy();
    }

    private ItemStack getPlanksForLog(ItemStack log) {
        var key = ForgeRegistries.ITEMS.getKey(log.getItem());
        if (key == null) return ItemStack.EMPTY;

        String logName = key.toString();
        String plankName = logName.replace("_log", "_planks").replace("_wood", "_planks");
        var plankItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(plankName));
        if (plankItem != null) {
            return new ItemStack(plankItem, 6);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.SAWING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.SAWING_TYPE.get();
    }

    public Ingredient getIngredient() { return ingredient; }

    public static class Serializer implements RecipeSerializer<SawingRecipe> {
        @Override
        public @NotNull SawingRecipe fromJson(@NotNull ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            ItemStack result = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            int cookingTime = json.get("cookingtime").getAsInt();
            return new SawingRecipe(id, ingredient, result, cookingTime);
        }

        @Override
        public @Nullable SawingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            int cookingTime = buf.readVarInt();
            return new SawingRecipe(id, ingredient, result, cookingTime);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, SawingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.cookingTime);
        }
    }
}