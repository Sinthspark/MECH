package com.sinthspark.mech.compat.jei;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.recipe.OilPressingRecipe;
import com.sinthspark.mech.recipe.PressingRecipe;
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

public class PressingCategory implements IRecipeCategory<PressingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MECH.MOD_ID, "pressing");
    public static final ResourceLocation TEXTURE = new ResourceLocation(MECH.MOD_ID, "textures/gui/jei/pressing_category.png");

    public static final RecipeType<PressingRecipe> PRESSING_RECIPE_TYPE =
            new RecipeType<>(UID, PressingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer timer;

    public PressingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.PRESS.get()));
        this.timer = helper.createTickTimer(20, 120, true);
    }

    @Override
    public RecipeType<PressingRecipe> getRecipeType() {
        return PRESSING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Прессование");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PressingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 52, 35).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 35).addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(PressingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        int progress = timer.getValue();
        int progressWidth = 24 - (timer.getValue() * 24) / 120; // обратная анимация
        guiGraphics.blit(TEXTURE, 79, 34, 176, 14, progressWidth, 17);
        guiGraphics.drawString(Minecraft.getInstance().font, "30-60W", 8, 60, 0xFF888888, false);
        guiGraphics.drawString(Minecraft.getInstance().font, "1800 тиков", 8, 72, 0xFF888888, false);
    }
}
