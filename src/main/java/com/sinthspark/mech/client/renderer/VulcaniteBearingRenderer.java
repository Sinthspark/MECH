package com.sinthspark.mech.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sinthspark.mech.blockentity.VulcaniteBearingTile;
import com.sinthspark.mech.blocks.VulcaniteBearingBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VulcaniteBearingRenderer implements BlockEntityRenderer<VulcaniteBearingTile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("mech", "textures/block/bearing_rotating.png");

    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);;

    public VulcaniteBearingRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(VulcaniteBearingTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        BlockState state = tile.getBlockState();
        Direction facing = state.getValue(VulcaniteBearingBlock.FACING);

        VertexConsumer builder = buffer.getBuffer(RENDER_TYPE);

        poseStack.pushPose();

        // Центрируем по facing
        poseStack.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case EAST  -> {}
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        // Поворот вокруг центра модели на угол из BlockEntity
        poseStack.translate(15.0/16, 12.0/16, 8.0/16);

        // Вращение: берём угол из BlockEntity + partialTick для плавности
        float angle = tile.getRotation(partialTick);
        poseStack.mulPose(Axis.XP.rotationDegrees(angle)); // если ось вращения X

        poseStack.translate(-15.0/16, -12.0/16, -8.0/16);

        // Первый элемент (горизонтальная пластина)
        drawBox(poseStack, builder, light,
                15.0f/16, 10.0f/16,  4.0f/16,
                16.0f/16, 14.0f/16, 12.0f/16);

        // Второй элемент (вертикальная стойка)
        drawBox(poseStack, builder, light,
                15.0f/16,  8.0f/16,  6.0f/16,
                16.0f/16, 16.0f/16, 10.0f/16);

        poseStack.popPose();
    }


    private void drawBox(PoseStack poseStack, VertexConsumer builder, int light,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2) {

        float u1 = 0, u2 = 1;
        float v1 = 0, v2 = 1;

        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();

        // North face
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();

        // South face
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();

        // West face
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();

        // East face
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();

        // Down face
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();

        // Up face
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.renderDistance().get() * 16;
    }
}