package com.sinthspark.mech.compat.jei;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.recipe.OilPressingRecipe;
import com.sinthspark.mech.registration.BlockRegistry;
import com.sinthspark.mech.registration.FluidRegistry;
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

public class OilPressingCategory implements IRecipeCategory<OilPressingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MECH.MOD_ID, "oil_pressing");
    public static final ResourceLocation TEXTURE = new ResourceLocation(MECH.MOD_ID, "textures/gui/jei/oil_pressing_category.png");

    public static final RecipeType<OilPressingRecipe> OIL_PRESSING_RECIPE_TYPE =
            new RecipeType<>(UID, OilPressingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final ITickTimer timer;

    public OilPressingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.OIL_PRESS.get()));
        this.timer = helper.createTickTimer(20, 120, true);
    }

    @Override
    public RecipeType<OilPressingRecipe> getRecipeType() {
        return OIL_PRESSING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Маслопресс");
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
    public void setRecipe(IRecipeLayoutBuilder builder, OilPressingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 52, 35).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 35)
                .setFluidRenderer(50, false, 16, 16) // 50 мб из 50 = полный квадрат
                .addFluidStack(FluidRegistry.RAPESEED_OIL.source.get(), 50);
    }

    @Override
    public void draw(OilPressingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        int progress = timer.getValue();
        int progressWidth = 24 - (timer.getValue() * 24) / 120; // обратная анимация
        guiGraphics.blit(TEXTURE, 79, 34, 176, 14, progressWidth, 17);
        guiGraphics.drawString(Minecraft.getInstance().font, "50 mB", 116, 55, 0xFF888888, false);
        guiGraphics.drawString(Minecraft.getInstance().font, "50-80W", 8, 60, 0xFF888888, false);
        guiGraphics.drawString(Minecraft.getInstance().font, "1200 тиков", 8, 72, 0xFF888888, false);
    }
}