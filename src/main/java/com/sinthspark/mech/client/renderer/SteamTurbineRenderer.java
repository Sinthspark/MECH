package com.sinthspark.mech.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sinthspark.mech.blockentity.SteamTurbineTile;
import com.sinthspark.mech.blocks.SteamTurbineBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class SteamTurbineRenderer implements BlockEntityRenderer<SteamTurbineTile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("mech", "textures/block/steam_turbine_shaft.png");

    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE);

    public SteamTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SteamTurbineTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        BlockState state = tile.getBlockState();
        Direction facing = state.getValue(SteamTurbineBlock.FACING);

        VertexConsumer builder = buffer.getBuffer(RENDER_TYPE);

        poseStack.pushPose();

        // Центрируем модель
        poseStack.translate(0.5, 0.5, 0.5);

        // Поворачиваем по facing
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            default -> {}
        }

        // Вращение вала
        if (tile.isRunning()) {
            tile.rotationAngle += 1f;
            poseStack.mulPose(Axis.ZP.rotationDegrees(tile.rotationAngle));
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        int fullBright = LightTexture.FULL_BRIGHT;

        // Куб 1: основной вал
        drawBox(poseStack, builder, fullBright,
                7.0f/16, 7.0f/16, -3.0f/16,
                9.0f/16, 9.0f/16, 0.0f/16);

        // Куб 2: такая же деталь, повёрнутая на 45°
        poseStack.pushPose();
        poseStack.translate(8.0f/16, 8.0f/16, -1.5f/16);
        poseStack.mulPose(Axis.ZP.rotationDegrees(45));
        poseStack.translate(-8.0f/16, -8.0f/16, 1.5f/16);

        drawBox(poseStack, builder, fullBright,
                7.0f/16, 7.0f/16, -3.0f/16,
                9.0f/16, 9.0f/16, 0.0f/16);
        poseStack.popPose();

        poseStack.popPose(); // закрываем основной pushPose
    }

    private void drawBox(PoseStack poseStack, VertexConsumer builder, int light,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2) {

        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();

        // North face (Z-)
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();

        // South face (Z+)
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();

        // West face (X-)
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();

        // East face (X+)
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();

        // Top face (Y+)
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();

        // Bottom face (Y-)
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.renderDistance().get() * 16;
    }
}
