package com.sinthspark.mech.recipe;

import com.google.gson.JsonArray;
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

import java.util.ArrayList;
import java.util.List;

public class AlloyingRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final List<CountedIngredient> countedIngredients;
    private final ItemStack result;
    private final int steamCost;
    private final int processTime;

    public AlloyingRecipe(ResourceLocation id, List<CountedIngredient> countedIngredients,
                          ItemStack result, int steamCost, int processTime) {
        this.id = id;
        this.countedIngredients = countedIngredients;
        this.result = result;
        this.steamCost = steamCost;
        this.processTime = processTime;
    }

    @Override
    public boolean matches(@NotNull SimpleContainer container, @NotNull Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack.copy());
            }
        }

        for (CountedIngredient ci : countedIngredients) {
            int required = ci.count;
            boolean found = false;
            for (int i = 0; i < inputs.size(); i++) {
                ItemStack input = inputs.get(i);
                if (ci.ingredient.test(input) && input.getCount() >= required) {
                    input.shrink(required);
                    if (input.isEmpty()) {
                        inputs.remove(i);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
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
        return ModRecipes.ALLOYING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.ALLOYING_TYPE.get();
    }

    public List<CountedIngredient> getCountedIngredients() {
        return countedIngredients;
    }

    public int getSteamCost() {
        return steamCost;
    }

    public int getProcessTime() {
        return processTime;
    }

    public int getSteamPerTick() {
        return Math.max(1, steamCost / processTime);
    }

    // === CountedIngredient ===

    public record CountedIngredient(Ingredient ingredient, int count) {}

    // === Serializer ===

    public static class Serializer implements RecipeSerializer<AlloyingRecipe> {
        @Override
        public @NotNull AlloyingRecipe fromJson(@NotNull ResourceLocation id, JsonObject json) {
            JsonArray ingredientsArray = json.getAsJsonArray("ingredients");
            List<CountedIngredient> list = new ArrayList<>();
            for (int i = 0; i < ingredientsArray.size(); i++) {
                JsonObject obj = ingredientsArray.get(i).getAsJsonObject();
                Ingredient ingredient = Ingredient.fromJson(obj);
                int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
                list.add(new CountedIngredient(ingredient, count));
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            int steamCost = json.get("steam").getAsInt();
            int processTime = json.get("time").getAsInt();

            return new AlloyingRecipe(id, list, result, steamCost, processTime);
        }

        @Override
        public @Nullable AlloyingRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<CountedIngredient> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(new CountedIngredient(Ingredient.fromNetwork(buf), buf.readVarInt()));
            }
            ItemStack result = buf.readItem();
            int steamCost = buf.readVarInt();
            int processTime = buf.readVarInt();
            return new AlloyingRecipe(id, list, result, steamCost, processTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AlloyingRecipe recipe) {
            buf.writeVarInt(recipe.countedIngredients.size());
            for (CountedIngredient ci : recipe.countedIngredients) {
                ci.ingredient.toNetwork(buf);
                buf.writeVarInt(ci.count);
            }
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.steamCost);
            buf.writeVarInt(recipe.processTime);
        }
    }
}