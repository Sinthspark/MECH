package com.sinthspark.mech.client.screen;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.screen.ElectrolyzerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class ElectrolyzerScreen extends AbstractContainerScreen<ElectrolyzerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MECH.MOD_ID, "textures/gui/electricity/electrolyzer_gui.png");

    public ElectrolyzerScreen(ElectrolyzerMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // === ВХОДНОЙ ТАНК (слева) ===
        drawTank(graphics, x + 36, y + 17, 18, 52,
                menu.blockEntity.getInputTank().getFluid(),
                menu.blockEntity.getInputTank().getCapacity(),
                0xFF8888FF);

        // === ВЫХОДНОЙ ТАНК 1 ===
        drawTank(graphics, x + 128, y + 17, 18, 52,
                menu.blockEntity.getOutputTank1().getFluid(),
                menu.blockEntity.getOutputTank1().getCapacity(),
                0xFF88FF88);

        // === ВЫХОДНОЙ ТАНК 2 ===
        drawTank(graphics, x + 150, y + 17, 18, 52,
                menu.blockEntity.getOutputTank2().getFluid(),
                menu.blockEntity.getOutputTank2().getCapacity(),
                0xFFFF8888);

        // Прогресс-бар
        if (menu.blockEntity.progress > 0) {
            int progressWidth = (int) ((menu.blockEntity.progress / (float) menu.blockEntity.maxProgress) * 25);
            graphics.blit(TEXTURE, x + 79, y + 35, 176, 14, progressWidth, 16);
        }

        // Энергия (снизу)
        int energy = menu.blockEntity.getEnergyStored();
        int maxEnergy = menu.blockEntity.getMaxEnergy();
        if (maxEnergy > 0) {
            int energyWidth = (int) (70 * ((float) energy / maxEnergy));
            graphics.fill(x + 53, y + 70, x + 53 + energyWidth, y + 74, 0xFFFF4444);
        }
    }

    private void drawTank(GuiGraphics graphics, int tankX, int tankY, int tankWidth, int tankHeight,
                          FluidStack fluid, int capacity, int color) {
        graphics.fill(tankX, tankY, tankX + tankWidth, tankY + tankHeight, 0xFF444444);
        if (!fluid.isEmpty() && capacity > 0) {
            int fluidHeight = (int) (((float) fluid.getAmount() / capacity) * tankHeight);
            graphics.fill(tankX + 1, tankY + tankHeight - fluidHeight, tankX + tankWidth - 1, tankY + tankHeight, color);
        }
        graphics.fill(tankX, tankY - 1, tankX + tankWidth, tankY, 0xFF888888);
        graphics.fill(tankX, tankY + tankHeight, tankX + tankWidth, tankY + tankHeight + 1, 0xFF888888);
        graphics.fill(tankX, tankY, tankX + 1, tankY + tankHeight, 0xFF888888);
        graphics.fill(tankX + tankWidth - 1, tankY, tankX + tankWidth, tankY + tankHeight, 0xFF888888);
        // Разметка только для высокого танка (> 30px)
        if (tankHeight > 30) {
            for (int i = 1; i <= 3; i++) {
                int lineY = tankY + tankHeight - (i * tankHeight / 4);
                int lineW = (i == 2) ? 7 : 4;
                graphics.fill(tankX + 1, lineY, tankX + 1 + lineW, lineY + 1, 0xFFAAAAAA);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        checkTooltip(graphics, mouseX, mouseY, x + 36, y + 17, 18, 52,
                menu.blockEntity.getInputTank().getFluid(), menu.blockEntity.getInputTank().getCapacity());
        checkTooltip(graphics, mouseX, mouseY, x + 128, y + 17, 18, 52,
                menu.blockEntity.getOutputTank1().getFluid(), menu.blockEntity.getOutputTank1().getCapacity());
        checkTooltip(graphics, mouseX, mouseY, x + 150, y + 17, 18, 52,
                menu.blockEntity.getOutputTank2().getFluid(), menu.blockEntity.getOutputTank2().getCapacity());
    }

    private void checkTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                              int tankX, int tankY, int tankW, int tankH,
                              FluidStack fluid, int capacity) {
        if (mouseX >= tankX && mouseX <= tankX + tankW && mouseY >= tankY && mouseY <= tankY + tankH) {
            String tooltip = fluid.isEmpty() ? "Пусто" :
                    fluid.getDisplayName().getString() + ": " + fluid.getAmount() + " / " + capacity + " mB";
            graphics.renderTooltip(font, Component.literal(tooltip), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
