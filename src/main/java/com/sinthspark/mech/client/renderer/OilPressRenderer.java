package com.sinthspark.mech.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sinthspark.mech.blockentity.OilPressTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class OilPressRenderer implements BlockEntityRenderer<OilPressTile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("mech:textures/block/oil_press_rotating.png");

    public OilPressRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(OilPressTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        RenderType renderType = RenderType.entityCutoutNoCull(TEXTURE);
        VertexConsumer builder = buffer.getBuffer(renderType);

        poseStack.pushPose();

        // === ДЕТАЛЬ 1: ПОРШЕНЬ ===
        if (tile.isActive()) {
            float time = (System.currentTimeMillis() % 1000) / 1000f;
            if (time < 0.5f) {
                tile.pistonOffset = -time * 2 * 5/16f;
            } else {
                tile.pistonOffset = -(1.0f - time) * 2 * 5/16f;
            }
        }

        poseStack.pushPose();
        poseStack.translate(0, tile.pistonOffset, 0);

        drawBox(poseStack, builder, light, 7/16f, 10/16f, 7/16f, 9/16f, 29/16f, 9/16f);
        drawBoxRotated(poseStack, builder, light, 6.75f/16, 10/16f, 7.75f/16, 8.75f/16, 29/16f, 9.75f/16,
                7/16f, 4/16f, 8/16f, 45, Axis.YP);
        drawBox(poseStack, builder, light, 5/16f, 9/16f, 5/16f, 11/16f, 10/16f, 11/16f);

        poseStack.popPose();

        // === ДЕТАЛЬ 2: ВРАЩАЮЩИЙСЯ ВАЛ ===
        poseStack.pushPose();

        float angle = 0;
        if (tile.isActive()) {
            float speed = tile.getSpeed();
            tile.renderAngle = (tile.renderAngle + speed * partialTick / 20.0f * tile.rotationDirection) % 360;
            angle = tile.renderAngle;
        }

        poseStack.translate(0.5, 24/16f, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, -24/16f, -0.5);

        drawBox(poseStack, builder, light, 0/16f, 23/16f, 7/16f, 4/16f, 25/16f, 9/16f);
        drawBoxRotated(poseStack, builder, light, 11/16f, 23/16f, 2/16f, 15/16f, 25/16f, 4/16f,
                13/16f, 23/16f, 4/16f, 45, Axis.YP);
        drawBox(poseStack, builder, light, 7/16f, 23/16f, 0/16f, 9/16f, 25/16f, 4/16f);
        drawBoxRotated(poseStack, builder, light, 2/16f, 23/16f, 2/16f, 6/16f, 25/16f, 4/16f,
                3/16f, 23/16f, 3/16f, -45, Axis.YP);
        drawBoxRotated(poseStack, builder, light, 2/16f, 23/16f, 10/16f, 4/16f, 25/16f, 14/16f,
                3/16f, 23/16f, 13/16f, -45, Axis.YP);
        drawBox(poseStack, builder, light, 7/16f, 23/16f, 12/16f, 9/16f, 25/16f, 16/16f);
        drawBoxRotated(poseStack, builder, light, 11/16f, 23/16f, 11/16f, 15/16f, 25/16f, 13/16f,
                12/16f, 23/16f, 12/16f, -45, Axis.YP);
        drawBox(poseStack, builder, light, 12/16f, 23/16f, 7/16f, 16/16f, 25/16f, 9/16f);
        drawBox(poseStack, builder, light, 4/16f, 23/16f, 4/16f, 12/16f, 25/16f, 12/16f);

        poseStack.popPose();

        // === ПРЕДМЕТ ===
        ItemStack stack = tile.itemHandler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.2, 0.5);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.GROUND, light, overlay, poseStack, bufferSource, tile.getLevel(), 0);
            bufferSource.endBatch();
            poseStack.popPose();
        }

        // === МАСЛО В ТАНКЕ ===
        FluidStack fluid = tile.getFluidTank().getFluid();
        if (!fluid.isEmpty()) {
            float amount = fluid.getAmount();
            float maxFluidHeight = 3/16f; // Y: 4/16 → 7/16 (макс 3 пикселя высоты)
            float fluidHeight = (amount / 1000f) * maxFluidHeight;
            float yBottom = 4/16f;
            float yTop = yBottom + fluidHeight;
            if (yTop > 7/16f) yTop = 7/16f;

            RenderType fluidRenderType = RenderType.entityTranslucent(TEXTURE);
            VertexConsumer fluidBuilder = buffer.getBuffer(fluidRenderType);

            poseStack.pushPose();
            drawColoredBox(poseStack, fluidBuilder, light, 5/16f, yBottom, 5/16f, 11/16f, yTop, 11/16f, 0xFFD0BE69);
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

    private void drawColoredBox(PoseStack poseStack, VertexConsumer builder, int light,
                                float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = 0.8f;

        float u1 = 0, u2 = 1, v1 = 0, v2 = 1;
        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();

        builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();

        builder.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();

        builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();

        builder.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();

        builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();

        builder.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.renderDistance().get() * 16;
    }
}
