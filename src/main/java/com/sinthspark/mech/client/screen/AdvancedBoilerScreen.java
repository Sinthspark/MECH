package com.sinthspark.mech.client.screen;

import com.sinthspark.mech.MECH;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.screen.AdvancedBoilerMenu;
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
import org.jetbrains.annotations.NotNull;

public class AdvancedBoilerScreen extends AbstractContainerScreen<AdvancedBoilerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MECH.MOD_ID, "textures/gui/steam/high_pressure/advanced_boiler_gui.png");

    public AdvancedBoilerScreen(AdvancedBoilerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        int tankY = y + 22;

        // === ТАНК ВОДЫ ===
        FluidStack waterFluid = menu.getWaterFluid();
        int waterX = x + 52;
        graphics.fill(waterX, tankY, waterX + 16, tankY + 40, 0xFF444444);
        if (!waterFluid.isEmpty()) {
            int waterHeight = (int) ((waterFluid.getAmount() / 16000f) * 40);
            int color;
            if (waterFluid.getFluid() == FluidRegistry.CREOSOTE.source.get()) {
                color = 0xFF3A2E1E;
            } else {
                color = IClientFluidTypeExtensions.of(waterFluid.getFluid()).getTintColor();
            }
            graphics.fill(waterX, tankY + 40 - waterHeight, waterX + 16, tankY + 40, color);
        }
        drawTankBorder(graphics, waterX, tankY, 16, 40);

        // === ТЕМПЕРАТУРА ===
        int tempX = waterX + 20;
        int tempWidth = 2;
        int tempHeight = 40;

        graphics.fill(tempX, tankY, tempX + tempWidth, tankY + tempHeight, 0xFF444444);
        if (menu.firebox != null && menu.firebox.temperature > 0) {
            int tempLevel = (int) ((menu.firebox.temperature / 1600f) * tempHeight);
            int red = (int) (255 * (menu.firebox.temperature / 1600f));
            int green = (int) (255 * (1 - menu.firebox.temperature / 1600f));
            int tempColor = (0xFF << 24) | (red << 16) | (green << 8);
            graphics.fill(tempX, tankY + tempHeight - tempLevel, tempX + tempWidth, tankY + tempHeight, tempColor);
        }
        drawTankBorder(graphics, tempX, tankY, tempWidth, tempHeight);

        // === ТАНК ПАРА ===
        FluidStack steamFluid = menu.getSteamFluid();
        int steamX = tempX + 34;
        graphics.fill(steamX, tankY, steamX + 16, tankY + 40, 0xFF444444);
        if (!steamFluid.isEmpty()) {
            int steamHeight = (int) ((steamFluid.getAmount() / 16000f) * 40);
            int color = IClientFluidTypeExtensions.of(steamFluid.getFluid()).getTintColor();
            graphics.fill(steamX, tankY + 40 - steamHeight, steamX + 16, tankY + 40, color);
        }
        drawTankBorder(graphics, steamX, tankY, 16, 40);

        // Разметка шкалы танка воды
        for (int mb = 500; mb < 16000; mb += 500) {
            float fillPercent = mb / 16000f;
            int lineY = tankY + 40 - (int)(fillPercent * 40);

            if (mb % 1000 == 0) {
                graphics.fill(waterX, lineY, waterX + 6, lineY + 1, 0xFFAAAAAA);
            } else {
                graphics.fill(waterX, lineY, waterX + 3, lineY + 1, 0xFFAAAAAA);
            }
        }
        graphics.drawString(font, "0°C", tempX + tempWidth + 7, tankY + tempHeight - 8, 0xFFAAAAAA, false);
        graphics.drawString(font, "1600°C", tempX + tempWidth + 7, tankY + 2, 0xFFAAAAAA, false);

        // Разметка шкалы танка пара
        for (int mb = 500; mb < 16000; mb += 500) {
            int lineY = tankY + 40 - (mb * 40 / 16000);
            if (mb % 1000 == 0) {
                graphics.fill(steamX, lineY, steamX + 6, lineY + 1, 0xFFAAAAAA);
            } else {
                graphics.fill(steamX, lineY, steamX + 3, lineY + 1, 0xFFAAAAAA);
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
        int tankY = y + 22;

        // Тултип для танка воды (X=52-68, Y=22-62)
        if (mouseX >= x + 52 && mouseX <= x + 68 && mouseY >= tankY && mouseY <= tankY + 40) {
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
                    Component.literal((fluid.isEmpty() ? 0 : fluid.getAmount()) + " / 16000 mB " + fluidName),
                    mouseX, mouseY);
        }

        // Тултип для температуры (X=72-76, Y=22-62)
        if (mouseX >= x + 72 && mouseX <= x + 76 && mouseY >= tankY && mouseY <= tankY + 40) {
            int temp = menu.firebox != null ? menu.firebox.temperature : 0;
            graphics.renderTooltip(font,
                    Component.literal("Температура: " + temp + "°C"),
                    mouseX, mouseY);
        }

        // Тултип для танка пара (X=110-126, Y=22-62)
        if (mouseX >= x + 110 && mouseX <= x + 126 && mouseY >= tankY && mouseY <= tankY + 40) {
            FluidStack fluid = menu.getSteamFluid();
            graphics.renderTooltip(font,
                    Component.literal((fluid.isEmpty() ? 0 : fluid.getAmount()) + " / 16000 mB пар"),
                    mouseX, mouseY);
        }
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
                    clickMenuButton(0);
                    return true;
                }
            }
            if (carried.getItem() == Items.WATER_BUCKET) {
                clickMenuButton(1);
                return true;
            }
            if (carried.getItem() == FluidRegistry.CREOSOTE.bucket.get()) {
                clickMenuButton(2);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ФИКС: Защита от NPE и использование корректного способа отправки клика
    private void clickMenuButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }
}
