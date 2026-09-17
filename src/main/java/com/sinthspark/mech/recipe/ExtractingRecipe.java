package com.sinthspark.mech.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtractingRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final FluidStack fluidResult;
    private final int steamCost;
    private final int processTime;

    public ExtractingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result,
                            FluidStack fluidResult, int steamCost, int processTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.fluidResult = fluidResult;
        this.steamCost = steamCost;
        this.processTime = processTime;
    }

    @Override
    public boolean matches(@NotNull SimpleContainer container, @NotNull Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SimpleContainer container, @NotNull RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.EXTRACTING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.EXTRACTING_TYPE.get();
    }

    public Ingredient getIngredient() { return ingredient; }
    public ItemStack getResult() { return result; }
    public FluidStack getFluidResult() { return fluidResult; }
    public int getSteamCost() { return steamCost; }
    public int getProcessTime() { return processTime; }

    public static class Serializer implements RecipeSerializer<ExtractingRecipe> {
        @Override
        public @NotNull ExtractingRecipe fromJson(@NotNull ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));

            ItemStack result = ItemStack.EMPTY;
            if (json.has("result")) {
                result = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            }

            FluidStack fluidResult = FluidStack.EMPTY;
            if (json.has("fluid")) {
                String fluidName = json.get("fluid").getAsString();
                int fluidAmount = json.get("fluidAmount").getAsInt();
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
                if (fluid != null) {
                    fluidResult = new FluidStack(fluid, fluidAmount);
                }
            }

            int steamCost = json.get("steam").getAsInt();
            int processTime = json.get("time").getAsInt();

            return new ExtractingRecipe(id, ingredient, result, fluidResult, steamCost, processTime);
        }

        @Override
        public @Nullable ExtractingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            FluidStack fluidResult = FluidStack.readFromPacket(buf);
            int steamCost = buf.readVarInt();
            int processTime = buf.readVarInt();
            return new ExtractingRecipe(id, ingredient, result, fluidResult, steamCost, processTime);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, ExtractingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            recipe.fluidResult.writeToPacket(buf);
            buf.writeVarInt(recipe.steamCost);
            buf.writeVarInt(recipe.processTime);
        }
    }
}
