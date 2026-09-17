package com.sinthspark.mech.client.screen;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.screen.OilPressMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

public class OilPressScreen extends AbstractContainerScreen<OilPressMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MECH.MOD_ID, "textures/gui/kinetic/oil_press_gui.png");

    public OilPressScreen(OilPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Всегда рисуем танк
        int tankX = x + 116;
        int tankY = y + 16;
        int tankWidth = 16;
        int tankHeight = 50;

        // Фон танка (серый)
        graphics.fill(tankX, tankY, tankX + tankWidth, tankY + tankHeight, 0xFF444444);

        // Обводка танка
        graphics.fill(tankX, tankY - 1, tankX + tankWidth, tankY, 0xFF888888); // верх — на 1 пиксель выше
        graphics.fill(tankX, tankY + tankHeight, tankX + tankWidth, tankY + tankHeight + 1, 0xFF888888); // низ — на 1 пиксель ниже
        graphics.fill(tankX, tankY, tankX + 1, tankY + tankHeight, 0xFF888888); // лево (без изменений)
        graphics.fill(tankX + tankWidth - 1, tankY, tankX + tankWidth, tankY + tankHeight, 0xFF888888); // право (без изменений)

        // Жидкость (только если есть)
        FluidStack fluid = menu.getFluid();
        if (!fluid.isEmpty()) {
            int fluidHeight = (int) ((fluid.getAmount() / 1000f) * tankHeight);
            int color = 0xFFD0BE69; // твой цвет масла
            graphics.fill(tankX + 1, tankY + tankHeight - fluidHeight, tankX + tankWidth - 1, tankY + tankHeight, color);
        }

        // Разметка шкалы — ПОСЛЕ жидкости (поверх)
        for (int i = 1; i <= 3; i++) {
            int lineY = tankY + tankHeight - (i * tankHeight / 4);
            if (i == 2) {
                graphics.fill(tankX + 1, lineY, tankX + 7, lineY + 1, 0xFFAAAAAA);
            } else {
                graphics.fill(tankX + 1, lineY, tankX + 4, lineY + 1, 0xFFAAAAAA);
            }
        }

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
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int tankX = x + 116;
        int tankY = y + 16;
        int tankWidth = 16;
        int tankHeight = 50;

        if (mouseX >= tankX && mouseX <= tankX + tankWidth &&
                mouseY >= tankY && mouseY <= tankY + tankHeight) {

            FluidStack fluid = menu.getFluid();
            graphics.renderTooltip(font,
                    Component.literal(fluid.getAmount() + " / 1000 mB"),
                    mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int tankX = x + 116;
        int tankY = y + 16;
        int tankWidth = 16;
        int tankHeight = 50;

        if (mouseX >= tankX && mouseX <= tankX + tankWidth &&
                mouseY >= tankY && mouseY <= tankY + tankHeight) {

            ItemStack carried = menu.getCarried();

            // Пустое ведро → забрать масло
            if (carried.getItem() == Items.BUCKET && menu.getFluid().getAmount() >= 1000) {
                this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                return true;
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
