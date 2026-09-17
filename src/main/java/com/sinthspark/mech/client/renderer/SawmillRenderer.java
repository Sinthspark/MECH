package com.sinthspark.mech.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sinthspark.mech.blockentity.SawmillTile;
import com.sinthspark.mech.blocks.SawmillBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SawmillRenderer implements BlockEntityRenderer<SawmillTile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("mech:textures/block/sawmill_rotating.png");

    private float sawRotation = 0;
    private float renderAngle = 0;

    public SawmillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SawmillTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        ItemStack stack = tile.itemHandler.getStackInSlot(0);
        RenderType renderType = RenderType.entityCutoutNoCull(TEXTURE);
        VertexConsumer builder = buffer.getBuffer(renderType);
        Direction facing = tile.getBlockState().getValue(SawmillBlock.FACING);

        // Рассчёт углов
        if (!Minecraft.getInstance().isPaused() && tile.isActive()) {
            tile.sawRotation = (tile.sawRotation + 5f * partialTick / 20f) % 360;
        }
        float sawAngle = tile.sawRotation;

        if (!Minecraft.getInstance().isPaused() && tile.isActive()) {
            float speed = tile.getSpeed();
            if (speed > 0) {
                tile.renderAngle = (tile.renderAngle + speed * partialTick / 20.0f * tile.rotationDirection) % 360;
            }
        }
        float powerAngle = tile.renderAngle;

        poseStack.pushPose();

        // Поворот по FACING
        poseStack.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        // === ЦИРКУЛЯРНАЯ ПИЛА ===
        poseStack.pushPose();
        poseStack.translate(0.5, 0.4375, 0.4375);
        poseStack.mulPose(Axis.YP.rotationDegrees(sawAngle));
        poseStack.translate(-0.5, -0.4375, -0.4375);

        drawBox(poseStack, builder, light, 4 / 16f, 7 / 16f, 2 / 16f, 10 / 16f, 7 / 16f, 5 / 16f);
        drawBoxRotated(poseStack, builder, light, 1 / 16f, 7 / 16f, 7 / 16f, 7 / 16f, 7 / 16f, 10 / 16f, 4 / 16f, 7 / 16f, 8 / 16f, 90);
        drawBoxRotated(poseStack, builder, light, 6 / 16f, 7 / 16f, 10 / 16f, 12 / 16f, 7 / 16f, 13 / 16f, 9 / 16f, 7 / 16f, 11 / 16f, -180);
        drawBoxRotated(poseStack, builder, light, 9 / 16f, 7 / 16f, 5 / 16f, 15 / 16f, 7 / 16f, 8 / 16f, 12 / 16f, 7 / 16f, 6 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 8 / 16f, 7 / 16f, 5 / 16f, 9 / 16f, 7 / 16f, 6 / 16f, 9 / 16f, 7 / 16f, 6 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 5 / 16f, 7 / 16f, 5 / 16f, 6 / 16f, 7 / 16f, 6 / 16f, 6 / 16f, 7 / 16f, 6 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 5 / 16f, 7 / 16f, 8 / 16f, 6 / 16f, 7 / 16f, 9 / 16f, 6 / 16f, 7 / 16f, 9 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 8 / 16f, 7 / 16f, 8 / 16f, 9 / 16f, 7 / 16f, 9 / 16f, 9 / 16f, 7 / 16f, 9 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 2 / 16f, 7 / 16f, 2 / 16f, 3 / 16f, 7 / 16f, 3 / 16f, 3 / 16f, 7 / 16f, 3 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 2 / 16f, 7 / 16f, 11 / 16f, 3 / 16f, 7 / 16f, 12 / 16f, 3 / 16f, 7 / 16f, 12 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 11 / 16f, 7 / 16f, 11 / 16f, 12 / 16f, 7 / 16f, 12 / 16f, 12 / 16f, 7 / 16f, 12 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 11 / 16f, 7 / 16f, 2 / 16f, 12 / 16f, 7 / 16f, 3 / 16f, 12 / 16f, 7 / 16f, 3 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 7 / 16f, 7 / 16f, 0 / 16f, 8 / 16f, 7 / 16f, 1 / 16f, 8 / 16f, 7 / 16f, 1 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 6 / 16f, 7 / 16f, 13 / 16f, 7 / 16f, 7 / 16f, 14 / 16f, 7 / 16f, 7 / 16f, 14 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 13 / 16f, 7 / 16f, 7 / 16f, 14 / 16f, 7 / 16f, 8 / 16f, 14 / 16f, 7 / 16f, 8 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 0 / 16f, 7 / 16f, 6 / 16f, 1 / 16f, 7 / 16f, 7 / 16f, 1 / 16f, 7 / 16f, 7 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 8 / 16f, 7 / 16f, 1 / 16f, 9 / 16f, 7 / 16f, 3 / 16f, 9 / 16f, 7 / 16f, 2 / 16f, -90);
        drawBoxRotated(poseStack, builder, light, 6 / 16f, 7 / 16f, 12 / 16f, 7 / 16f, 7 / 16f, 14 / 16f, 7 / 16f, 7 / 16f, 13 / 16f, -90);
        drawBox(poseStack, builder, light, 2 / 16f, 7 / 16f, 5 / 16f, 3 / 16f, 7 / 16f, 7 / 16f);
        drawBox(poseStack, builder, light, 13 / 16f, 7 / 16f, 7 / 16f, 14 / 16f, 7 / 16f, 9 / 16f);

        poseStack.popPose();

        // === ПРИЁМЩИК ЭНЕРГИИ ===

        poseStack.pushPose();
        poseStack.translate(0, 2 / 16f, 0);

        poseStack.translate(0.5, 0.5, -0.0625);
        poseStack.mulPose(Axis.ZP.rotationDegrees(powerAngle));
        poseStack.translate(-0.5, -0.5, 0.0625);

        // Куб 1
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, -0.0625);
        poseStack.mulPose(Axis.ZP.rotationDegrees(45));
        poseStack.translate(-0.5, -0.5, 0.0625);
        drawBox(poseStack, builder, light, 7 / 16f, 6 / 16f, -1 / 16f, 9 / 16f, 10 / 16f, 0 / 16f);
        poseStack.popPose();

        // Куб 2
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, -0.0625);
        poseStack.mulPose(Axis.ZP.rotationDegrees(45));
        poseStack.translate(-0.5, -0.5, 0.0625);
        drawBox(poseStack, builder, light, 6 / 16f, 7 / 16f, -1 / 16f, 10 / 16f, 9 / 16f, 0 / 16f);
        poseStack.popPose();

        poseStack.popPose();

        // === ПРЕДМЕТ ===
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            poseStack.scale(0.5f, 0.5f, 0.5f);
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.GROUND, light, overlay, poseStack, bufferSource, tile.getLevel(), 0);
            bufferSource.endBatch();
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void drawBoxRotated(PoseStack poseStack, VertexConsumer builder, int light,
                                float x1, float y1, float z1, float x2, float y2, float z2,
                                float ox, float oy, float oz, float angle) {
        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-ox, -oy, -oz);
        drawBox(poseStack, builder, light, x1, y1, z1, x2, y2, z2);
        poseStack.popPose();
    }

    private void drawBox(PoseStack poseStack, VertexConsumer builder, int light,
                         float x1, float y1, float z1, float x2, float y2, float z2) {
        float u1 = 0, u2 = 1, v1 = 0, v2 = 1;
        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();

        // North
        builder.vertex(matrix, x1, y1, z1).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        // South
        builder.vertex(matrix, x1, y1, z2).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        // East - инвертируем UV
        builder.vertex(matrix, x2, y1, z1).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        // West - инвертируем UV
        builder.vertex(matrix, x1, y1, z1).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        // Down
        builder.vertex(matrix, x1, y1, z1).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        // Up
        builder.vertex(matrix, x1, y2, z1).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.renderDistance().get() * 16;
    }
}
