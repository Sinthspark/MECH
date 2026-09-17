package com.sinthspark.mech.client.screen;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.screen.UpgradedBoilerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class UpgradedBoilerScreen extends AbstractContainerScreen<UpgradedBoilerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MECH.MOD_ID, "textures/gui/steam/medium_pressure/upgraded_boiler_gui.png");

    public UpgradedBoilerScreen(UpgradedBoilerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // === ТАНК ВОДЫ (слева, но ближе к центру) ===
        FluidStack waterFluid = menu.getWaterFluid();
        int waterX = x + 52;  // ближе к центру
        int waterY = y + 22;
        graphics.fill(waterX, waterY, waterX + 16, waterY + 40, 0xFF444444); // уменьшил высоту до 40
        if (!waterFluid.isEmpty()) {
            int waterHeight = (int) ((waterFluid.getAmount() / 8000f) * 40);
            int color;
            if (waterFluid.getFluid() == FluidRegistry.CREOSOTE.source.get()) {
                color = 0xFF3A2E1E; // тёмно-коричневый для креозота
            } else {
                color = IClientFluidTypeExtensions.of(waterFluid.getFluid()).getTintColor();
            }
            graphics.fill(waterX, waterY + 40 - waterHeight, waterX + 16, waterY + 40, color);
        }
        drawTankBorder(graphics, waterX, waterY, 16, 40);

        // === ТЕМПЕРАТУРА (рядом с водой) ===
        int tempX = waterX + 20; // справа от воды
        int tempY = waterY;
        int tempWidth = 2;
        int tempHeight = 40;

        graphics.fill(tempX, tempY, tempX + tempWidth, tempY + tempHeight, 0xFF444444);
        if (menu.firebox != null && menu.firebox.temperature > 0) {
            int tempLevel = (int) ((menu.firebox.temperature / 1200f) * tempHeight);
            int red = (int) (255 * (menu.firebox.temperature / 1200f));
            int green = (int) (255 * (1 - menu.firebox.temperature / 1200f));
            int tempColor = (0xFF << 24) | (red << 16) | (green << 8);
            graphics.fill(tempX, tempY + tempHeight - tempLevel, tempX + tempWidth, tempY + tempHeight, tempColor);
        }
        drawTankBorder(graphics, tempX, tempY, tempWidth, tempHeight);

        // === ТАНК ПАРА (справа от температуры) ===
        FluidStack steamFluid = menu.getSteamFluid();
        int steamX = tempX + 34; // справа от температуры
        int steamY = waterY;
        graphics.fill(steamX, steamY, steamX + 16, steamY + 40, 0xFF444444);
        if (!steamFluid.isEmpty()) {
            int steamHeight = (int) ((steamFluid.getAmount() / 8000f) * 40);
            int color = IClientFluidTypeExtensions.of(steamFluid.getFluid()).getTintColor();
            graphics.fill(steamX, steamY + 40 - steamHeight, steamX + 16, steamY + 40, color);
        }
        drawTankBorder(graphics, steamX, steamY, 16, 40);

        // Разметка шкалы танка воды
        for (int mb = 500; mb < 8000; mb += 500) {
            float fillPercent = mb / 8000f; // 0.0 = пусто, 1.0 = полно
            int lineY = waterY + 40 - (int)(fillPercent * 40); // снизу вверх!

            if (mb % 1000 == 0) {
                graphics.fill(waterX + 0, lineY, waterX + 6, lineY + 1, 0xFFAAAAAA);
            } else {
                graphics.fill(waterX + 0, lineY, waterX + 3, lineY + 1, 0xFFAAAAAA);
            }
            graphics.drawString(font, "0°C", tempX + tempWidth + 7, tempY + tempHeight - 8, 0xFFAAAAAA, false);
            graphics.drawString(font, "1200°C", tempX + tempWidth + 7, tempY + 2, 0xFFAAAAAA, false);
        }

        // Разметка шкалы танка пара
        for (int mb = 500; mb < 8000; mb += 500) {
            int lineY = steamY + 40 - (mb * 40 / 8000);
            if (mb % 1000 == 0) {
                graphics.fill(steamX + 0, lineY, steamX + 6, lineY + 1, 0xFFAAAAAA); // длинная на 1000, 2000, 3000
            } else {
                graphics.fill(steamX + 0, lineY, steamX + 3, lineY + 1, 0xFFAAAAAA); // короткая на 500, 1500, 2500, 3500
            }
        }
    }

    private void drawTankBorder(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x - 1, y - 1, x + w + 1, y, 0xFF888888);
        graphics.fill(x - 1, y + h, x + w + 1, y + h + 1, 0xFF888888);
        graphics.fill(x - 1, y, x, y + h, 0xFF888888);
        graphics.fill(x + w, y, x + w + 1, y + h, 0xFF888888);
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

        // Тултип для танка воды (X=52-68, Y=22-62)
        if (mouseX >= x + 52 && mouseX <= x + 68 && mouseY >= y + 22 && mouseY <= y + 62) {
            FluidStack fluid = menu.getWaterFluid();
            String fluidName = "Пусто";
            if (!fluid.isEmpty()) {
                if (fluid.getFluid() == Fluids.WATER) {
                    fluidName = "Вода";
                } else if (fluid.getFluid() == FluidRegistry.CREOSOTE.source.get()) {
                    fluidName = "Креозот";
                }
            }
            graphics.renderTooltip(font,
                    Component.literal((fluid.isEmpty() ? 0 : fluid.getAmount()) + " / 8000 mB " + fluidName),
                    mouseX, mouseY);
        }

        // Тултип для температуры (X=72-76, Y=22-62)
        if (mouseX >= x + 72 && mouseX <= x + 76 && mouseY >= y + 22 && mouseY <= y + 62) {
            int temp = menu.firebox != null ? menu.firebox.temperature : 0;
            graphics.renderTooltip(font,
                    Component.literal("Температура: " + temp + "°C"),
                    mouseX, mouseY);
        }

        // Тултип для танка пара (X=110-126, Y=22-62)
        if (mouseX >= x + 110 && mouseX <= x + 126 && mouseY >= y + 22 && mouseY <= y + 62) {
            FluidStack fluid = menu.getSteamFluid();
            graphics.renderTooltip(font,
                    Component.literal((fluid.isEmpty() ? 0 : fluid.getAmount()) + " / 8000 mB пар"),
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

        // Танк воды: X=52-68, Y=22-62
        if (mouseX >= x + 52 && mouseX <= x + 68 && mouseY >= y + 22 && mouseY <= y + 62) {
            ItemStack carried = menu.getCarried();

            if (carried.getItem() == Items.BUCKET) {
                FluidStack fluid = menu.getWaterFluid();
                if (fluid.getAmount() >= 1000) {
                    this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                    return true;
                }
            }
            if (carried.getItem() == Items.WATER_BUCKET) {
                this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                return true;
            }
            if (carried.getItem() == FluidRegistry.CREOSOTE.bucket.get()) {
                this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
