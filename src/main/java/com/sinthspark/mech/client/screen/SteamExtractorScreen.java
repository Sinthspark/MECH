package com.sinthspark.mech.client.screen;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.screen.SteamExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

public class SteamExtractorScreen extends AbstractContainerScreen<SteamExtractorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MECH.MOD_ID, "textures/gui/steam/low_pressure/steam_extractor_gui.png");

    public SteamExtractorScreen(SteamExtractorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Танк жидкости
        int tankX = x + 140;
        int tankY = y + 17;
        int tankWidth = 18;
        int tankHeight = 52;

        // Фон танка (серый)
        graphics.fill(tankX, tankY, tankX + tankWidth, tankY + tankHeight, 0xFF444444);

        // Обводка танка
        graphics.fill(tankX, tankY - 1, tankX + tankWidth, tankY, 0xFF888888); // верх
        graphics.fill(tankX, tankY + tankHeight, tankX + tankWidth, tankY + tankHeight + 1, 0xFF888888); // низ
        graphics.fill(tankX, tankY, tankX + 1, tankY + tankHeight, 0xFF888888); // лево
        graphics.fill(tankX + tankWidth - 1, tankY, tankX + tankWidth, tankY + tankHeight, 0xFF888888); // право

        // Жидкость
        FluidStack fluid = menu.blockEntity.getOutputFluid();
        if (!fluid.isEmpty()) {
            int fluidAmount = menu.blockEntity.getOutputFluidAmount();
            int maxFluid = menu.blockEntity.getOutputTankCapacity();
            int fluidHeight = (int) (((float) fluidAmount / maxFluid) * tankHeight);
            int color = 0xFFD0BE69; // можно заменить на цвет конкретной жидкости
            graphics.fill(tankX + 1, tankY + tankHeight - fluidHeight, tankX + tankWidth - 1, tankY + tankHeight, color);
        }

        // Разметка шкалы
        for (int i = 1; i <= 3; i++) {
            int lineY = tankY + tankHeight - (i * tankHeight / 4);
            if (i == 2) {
                graphics.fill(tankX + 1, lineY, tankX + 7, lineY + 1, 0xFFAAAAAA);
            } else {
                graphics.fill(tankX + 1, lineY, tankX + 4, lineY + 1, 0xFFAAAAAA);
            }
        }

        // Прогресс-бар
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
        int tankX = x + 140;
        int tankY = y + 17;
        int tankWidth = 18;
        int tankHeight = 52;

        if (mouseX >= tankX && mouseX <= tankX + tankWidth &&
                mouseY >= tankY && mouseY <= tankY + tankHeight) {

            FluidStack fluid = menu.blockEntity.getOutputFluid();
            String tooltip = fluid.isEmpty() ? "Пусто" :
                    fluid.getDisplayName().getString() + ": " +
                            menu.blockEntity.getOutputFluidAmount() + " / " +
                            menu.blockEntity.getOutputTankCapacity() + " mB";

            graphics.renderTooltip(font, Component.literal(tooltip), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int tankX = x + 26;
        int tankY = y + 17;
        int tankWidth = 18;
        int tankHeight = 52;

        if (mouseX >= tankX && mouseX <= tankX + tankWidth &&
                mouseY >= tankY && mouseY <= tankY + tankHeight) {

            ItemStack carried = menu.getCarried();

            // Пустое ведро → забрать жидкость
            if (carried.getItem() == Items.BUCKET && menu.blockEntity.getOutputFluidAmount() >= 1000) {
                this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                return true;
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}