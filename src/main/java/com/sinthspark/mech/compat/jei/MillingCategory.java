package com.sinthspark.mech.compat.jei;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.recipe.MillingRecipe;
import com.sinthspark.mech.registration.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MillingCategory implements IRecipeCategory<MillingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MECH.MOD_ID, "milling");
    public static final ResourceLocation TEXTURE = new ResourceLocation(MECH.MOD_ID, "textures/gui/jei/milling_category.png");

    public static final RecipeType<MillingRecipe> MILLING_RECIPE_TYPE =
            new RecipeType<>(UID, MillingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer timer;

    public MillingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.QUERN.get()));
        this.timer = helper.createTickTimer(20, 120, true);
    }

    @Override
    public RecipeType<MillingRecipe> getRecipeType() {
        return MILLING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Помол");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MillingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 52, 35).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 35).addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(MillingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        int progress = timer.getValue();
        int progressWidth = 24 - (timer.getValue() * 24) / 120; // обратная анимация
        guiGraphics.blit(TEXTURE, 79, 34, 176, 14, progressWidth, 17);
        guiGraphics.drawString(Minecraft.getInstance().font, "20-50W", 8, 60, 0xFF888888, false);
        guiGraphics.drawString(Minecraft.getInstance().font, "1800 тиков", 8, 72, 0xFF888888, false);
    }
}
