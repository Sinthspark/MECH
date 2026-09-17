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

public class CompressingRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final int inputCount;
    private final ItemStack result;
    private final int outputCount;
    private final int steamCost;
    private final int processTime;

    public CompressingRecipe(ResourceLocation id, Ingredient ingredient, int inputCount,
                             ItemStack result, int outputCount, int steamCost, int processTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.inputCount = inputCount;
        this.result = result;
        this.outputCount = outputCount;
        this.steamCost = steamCost;
        this.processTime = processTime;
    }

    public int getInputCount() {
        return inputCount;
    }

    @Override
    public boolean matches(@NotNull SimpleContainer container, @NotNull Level level) {
        ItemStack input = container.getItem(0);
        return ingredient.test(input) && input.getCount() >= inputCount;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SimpleContainer container, @NotNull RegistryAccess registryAccess) {
        ItemStack output = result.copy();
        output.setCount(outputCount);
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        ItemStack output = result.copy();
        output.setCount(outputCount);
        return output;
    }

    @Override
    public @NotNull ResourceLocation getId() { return id; }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return ModRecipes.COMPRESSING_SERIALIZER.get(); }

    @Override
    public @NotNull RecipeType<?> getType() { return ModRecipes.COMPRESSING_TYPE.get(); }

    public int getSteamCost() { return steamCost; }
    public int getProcessTime() { return processTime; }

    public static class Serializer implements RecipeSerializer<CompressingRecipe> {
        @Override
        public @NotNull CompressingRecipe fromJson(@NotNull ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            JsonObject ingObj = json.getAsJsonObject("ingredient");
            int inputCount = ingObj.has("count") ? ingObj.get("count").getAsInt() : 1;
            ItemStack result = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            int outputCount = json.has("output_count") ? json.get("output_count").getAsInt() : 1;
            int steamCost = json.get("steam").getAsInt();
            int processTime = json.get("time").getAsInt();
            return new CompressingRecipe(id, ingredient, inputCount, result, outputCount, steamCost, processTime);
        }

        @Override
        public @Nullable CompressingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            int inputCount = buf.readVarInt();
            ItemStack result = buf.readItem();
            int outputCount = buf.readVarInt();
            int steamCost = buf.readVarInt();
            int processTime = buf.readVarInt();
            return new CompressingRecipe(id, ingredient, inputCount, result, outputCount, steamCost, processTime);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, CompressingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeVarInt(recipe.inputCount);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.outputCount);
            buf.writeVarInt(recipe.steamCost);
            buf.writeVarInt(recipe.processTime);
        }
    }
}