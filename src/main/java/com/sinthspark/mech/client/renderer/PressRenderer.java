package com.sinthspark.mech.client.renderer;

import com.sinthspark.mech.blocks.PressBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sinthspark.mech.blockentity.PressTile;
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
public class PressRenderer implements BlockEntityRenderer<PressTile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("mech:textures/block/press_rotating.png");

    public PressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PressTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        ItemStack stack = tile.itemHandler.getStackInSlot(0);
        RenderType renderType = RenderType.entityCutoutNoCull(TEXTURE);
        VertexConsumer builder = buffer.getBuffer(renderType);
        Direction facing = tile.getBlockState().getValue(PressBlock.FACING);
        boolean leftActive = tile.leftActive;
        boolean rightActive = tile.rightActive;

        poseStack.pushPose();

        // Поворот по FACING — меняем на +90° для всех
        poseStack.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        if (tile.isActive()) {
            tile.renderAngle = (tile.renderAngle + 10f * partialTick / 20f) % 360;
        }
        float angle = tile.renderAngle;

        // === ПРИЁМНИК 1 (origin 8,7,1) ===
        poseStack.pushPose();
        poseStack.translate(0.5, 0.4375, 0.0625);
        if (tile.leftActive) {
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-45));
        poseStack.translate(-0.5, -0.4375, -0.0625);
        drawBox(poseStack, builder, light, 7/16f, 7/16f, -1/16f, 9/16f, 9/16f, 3/16f);
        drawBox(poseStack, builder, light, 6/16f, 7/16f, 0/16f, 10/16f, 9/16f, 2/16f);
        poseStack.popPose();

        // === ПРИЁМНИК 2 (origin 8,7,15) ===
        poseStack.pushPose();
        poseStack.translate(0.5, 0.4375, 0.9375);
        if (tile.rightActive) {
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-45));
        poseStack.translate(-0.5, -0.4375, -0.9375);
        drawBox(poseStack, builder, light, 6/16f, 7/16f, 14/16f, 10/16f, 9/16f, 16/16f);
        drawBox(poseStack, builder, light, 7/16f, 7/16f, 13/16f, 9/16f, 9/16f, 17/16f);
        poseStack.popPose();

        // === ЦИЛИНДР 1 (верхний, смещается вниз) ===
        poseStack.pushPose();
        poseStack.translate(0.5, 13.65f/16, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-0.5, -13.65f/16, -0.5);
        drawBoxRotated(poseStack, builder, light, 7/16f, 15/16f, 2/16f, 9/16f, 16/16f, 14/16f, 9/16f, 13/16f, 2/16f, 0, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 6.05f/16, 12.4f/16, 2/16f, 7.05f/16, 14.4f/16, 14/16f, 8.5f/16, 14/16f, 2/16f, -45, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 5.65f/16, 12.65f/16, 2/16f, 6.65f/16, 14.65f/16, 14/16f, 8/16f, 14.25f/16, 2/16f, 0, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 5.1f/16, 12/16f, 2/16f, 6.1f/16, 14/16f, 14/16f, 7/16f, 14/16f, 2/16f, 45, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 7.1f/16, 11.25f/16, 2/16f, 9.1f/16, 12.25f/16, 14/16f, 7/16f, 13/16f, 2/16f, 0, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 7.95f/16, 10.6f/16, 2/16f, 9.95f/16, 11.6f/16, 14/16f, 7.75f/16, 12.25f/16, 2/16f, 45, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 9.45f/16, 12.65f/16, 2/16f, 10.45f/16, 14.65f/16, 14/16f, 8.75f/16, 12.25f/16, 2/16f, 0, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 7.05f/16, 13.8f/16, 2/16f, 9.05f/16, 14.8f/16, 14/16f, 9.5f/16, 13/16f, 2/16f, -45, Axis.ZP);
        poseStack.popPose();

        // === ЦИЛИНДР 2 (нижний, смещается вверх) ===
        poseStack.pushPose();
        poseStack.translate(0.5, 7.65f/16, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-angle));
        poseStack.translate(-0.5, -7.65f/16, -0.5);
        drawBoxRotated(poseStack, builder, light, 7/16f, 9/16f, 2/16f, 9/16f, 10/16f, 14/16f, 9/16f, 7/16f, 2/16f, 0, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 6.05f/16, 6.4f/16, 2/16f, 7.05f/16, 8.4f/16, 14/16f, 8.5f/16, 8/16f, 2/16f, -45, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 5.65f/16, 6.65f/16, 2/16f, 6.65f/16, 8.65f/16, 14/16f, 8/16f, 8.25f/16, 2/16f, 0, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 5.1f/16, 6/16f, 2/16f, 6.1f/16, 8/16f, 14/16f, 7/16f, 8/16f, 2/16f, 45, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 7.1f/16, 5.25f/16, 2/16f, 9.1f/16, 6.25f/16, 14/16f, 7/16f, 7/16f, 2/16f, 0, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 7.95f/16, 4.6f/16, 2/16f, 9.95f/16, 5.6f/16, 14/16f, 7.75f/16, 6.25f/16, 2/16f, 45, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 9.45f/16, 6.65f/16, 2/16f, 10.45f/16, 8.65f/16, 14/16f, 8.75f/16, 6.25f/16, 2/16f, 0, Axis.ZP);
        drawBoxRotated(poseStack, builder, light, 7.05f/16, 7.8f/16, 2/16f, 9.05f/16, 8.8f/16, 14/16f, 9.5f/16, 7/16f, 2/16f, -45, Axis.ZP);
        poseStack.popPose();

        // === ПРЕДМЕТ ===
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
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
                                float ox, float oy, float oz, float angle, Axis axis) {
        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);
        poseStack.mulPose(axis.rotationDegrees(angle));
        poseStack.translate(-ox, -oy, -oz);
        drawBox(poseStack, builder, light, x1, y1, z1, x2, y2, z2);
        poseStack.popPose();
    }

    private void drawBox(PoseStack poseStack, VertexConsumer builder, int light,
                         float x1, float y1, float z1, float x2, float y2, float z2) {
        float u1 = 0, u2 = 1, v1 = 0, v2 = 1;
        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();

        builder.vertex(matrix, x1, y1, z1).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();

        builder.vertex(matrix, x1, y1, z2).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();

        builder.vertex(matrix, x1, y1, z1).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();

        builder.vertex(matrix, x2, y1, z1).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();

        builder.vertex(matrix, x1, y1, z1).color(1f,1f,1f,1f).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(1f,1f,1f,1f).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(1f,1f,1f,1f).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(1f,1f,1f,1f).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();

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