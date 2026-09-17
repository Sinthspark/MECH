package com.sinthspark.mech.client.screen;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.screen.OpenHearthMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OpenHearthScreen extends AbstractContainerScreen<OpenHearthMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MECH.MOD_ID, "textures/gui/multiblock/steam/medium_pressure/open_hearth_gui.png");

    public OpenHearthScreen(OpenHearthMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Прогресс-бар
        if (menu.blockEntity.progress > 0) {
            int progressWidth = (int) ((menu.blockEntity.progress / (float) menu.blockEntity.maxProgress) * 25);
            graphics.blit(TEXTURE, x + 75, y + 35, 176, 14, progressWidth, 16);
        }

        // Индикатор пара (танк со всех хатчей)
        int steamAmount = menu.blockEntity.currentSteam;
        int maxSteam = menu.blockEntity.totalTankCapacity;

        if (maxSteam > 0) {
            int tankX = x + 10;
            int tankY = y + 17;
            int tankWidth = 14;
            int tankHeight = 52;

            // Фон танка (тёмно-серый)
            graphics.fill(tankX, tankY, tankX + tankWidth, tankY + tankHeight, 0xFF2A2A2A);

            // Пар (серо-белый)
            if (steamAmount > 0) {
                int steamHeight = (int) ((float) steamAmount / maxSteam * tankHeight);
                int steamY = tankY + tankHeight - steamHeight;
                // Градиент пара (светлее сверху)
                graphics.fill(tankX, steamY, tankX + tankWidth, tankY + tankHeight, 0xFFB0B0B0);
                graphics.fill(tankX, steamY, tankX + tankWidth, steamY + 4, 0xFFD0D0D0);
            }

            // Обводка
            graphics.fill(tankX - 1, tankY - 1, tankX + tankWidth + 1, tankY, 0xFF666666);
            graphics.fill(tankX - 1, tankY + tankHeight, tankX + tankWidth + 1, tankY + tankHeight + 1, 0xFF666666);
            graphics.fill(tankX - 1, tankY, tankX, tankY + tankHeight, 0xFF666666);
            graphics.fill(tankX + tankWidth, tankY, tankX + tankWidth + 1, tankY + tankHeight, 0xFF666666);

            // Разметка шкалы
            for (int i = 1; i <= 3; i++) {
                int lineY = tankY + tankHeight - (i * tankHeight / 4);
                int lineWidth = (i == 2) ? 8 : 5;
                graphics.fill(tankX + tankWidth - lineWidth, lineY, tankX + tankWidth, lineY + 1, 0xFF888888);
            }

            // Тултип
            if (mouseX >= tankX && mouseX <= tankX + tankWidth && mouseY >= tankY && mouseY <= tankY + tankHeight) {
                String tooltip = "Пар: " + steamAmount + " / " + maxSteam + " mB";
                graphics.renderTooltip(font, Component.literal(tooltip), mouseX, mouseY);
            }
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
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}