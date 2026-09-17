package com.sinthspark.mech.client.screen;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.screen.PressMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

public class PressScreen extends AbstractContainerScreen<PressMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MECH.MOD_ID, "textures/gui/kinetic/press_gui.png");

    public PressScreen(PressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.blockEntity.progress > 0) {
            int progressWidth = (int) ((menu.blockEntity.progress / (float) menu.blockEntity.maxProgress) * 25);
            graphics.blit(TEXTURE, x + 79, y + 35, 176, 14, progressWidth, 16);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        // В renderLabels добавим надпись о диапазоне мощности
        if (menu.blockEntity.inputPower > 0) {
            String powerText = String.format("Power: %.1fW [%d-%dW]",
                    menu.blockEntity.inputPower,
                    (int)menu.blockEntity.minPower,
                    (int)menu.blockEntity.maxPower);
            graphics.drawString(font, powerText, 8, 60, 0xFFFFFF);
        }
    }
}