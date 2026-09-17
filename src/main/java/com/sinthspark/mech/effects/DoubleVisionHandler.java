package com.sinthspark.mech.effects;

import com.sinthspark.mech.config.ModConfig;
import com.sinthspark.mech.registration.EffectRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.joml.Matrix4f;

public class DoubleVisionHandler {

    private static boolean doubleVisionEnabled = true;
    private static boolean configLoaded = false;

    /**
     * Публичный метод для вызова из главного класса мода
     */
    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == ModConfig.CLIENT_SPEC) {
            doubleVisionEnabled = ModConfig.CLIENT.doubleVisionEnabled.get();
            configLoaded = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (configLoaded && !doubleVisionEnabled) return;

        if (!configLoaded) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!player.hasEffect(EffectRegistry.SHOCK.get())) return;

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        float shiftX = (float) (Math.random() - 0.5) * 3.0f;
        float shiftY = (float) (Math.random() - 0.5) * 3.0f;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.15f);
        RenderSystem.setShaderTexture(0, mc.getMainRenderTarget().getColorTextureId());

        Matrix4f matrix = new Matrix4f().translate(shiftX, shiftY, 0);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, 0, height, 0).uv(0, 0).endVertex();
        buffer.vertex(matrix, width, height, 0).uv(1, 0).endVertex();
        buffer.vertex(matrix, width, 0, 0).uv(1, 1).endVertex();
        buffer.vertex(matrix, 0, 0, 0).uv(0, 1).endVertex();
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}