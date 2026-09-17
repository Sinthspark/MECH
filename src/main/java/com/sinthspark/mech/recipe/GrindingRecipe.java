package com.sinthspark.mech.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrindingRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int count;
    private final int steamCost;
    private final int processTime;

    public GrindingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result,
                          int count, int steamCost, int processTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.count = count;
        this.steamCost = steamCost;
        this.processTime = processTime;
    }

    @Override
    public boolean matches(@NotNull SimpleContainer container, @NotNull Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SimpleContainer container, @NotNull RegistryAccess registryAccess) {
        ItemStack output = result.copy();
        output.setCount(count);
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        ItemStack output = result.copy();
        output.setCount(count);
        return output;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.GRINDING_TYPE.get();
    }

    public Ingredient getIngredient() { return ingredient; }
    public int getCount() { return count; }
    public int getSteamCost() { return steamCost; }
    public int getProcessTime() { return processTime; }

    public static class Serializer implements RecipeSerializer<GrindingRecipe> {
        @Override
        public @NotNull GrindingRecipe fromJson(@NotNull ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            ItemStack result = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            int count = json.has("count") ? json.get("count").getAsInt() : 2;
            int steamCost = json.get("steam").getAsInt();
            int processTime = json.get("time").getAsInt();
            return new GrindingRecipe(id, ingredient, result, count, steamCost, processTime);
        }

        @Override
        public @Nullable GrindingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            int count = buf.readVarInt();
            int steamCost = buf.readVarInt();
            int processTime = buf.readVarInt();
            return new GrindingRecipe(id, ingredient, result, count, steamCost, processTime);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, GrindingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.count);
            buf.writeVarInt(recipe.steamCost);
            buf.writeVarInt(recipe.processTime);
        }
    }
}
