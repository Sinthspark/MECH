package com.sinthspark.mech.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sinthspark.mech.blockentity.QuernTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuernRenderer implements BlockEntityRenderer<QuernTile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("mech:textures/block/quern_rotating.png");

    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    public QuernRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(QuernTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        VertexConsumer builder = buffer.getBuffer(RENDER_TYPE);

        poseStack.pushPose();

        float speed = tile.getSpeed();
        if (speed > 0) {
            tile.renderAngle = (tile.renderAngle + speed * partialTick / 20.0f) % 360;
        }
        float angle = tile.renderAngle;

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.5, -0.5);


        // Кубы 1-8 без поворота
        drawBox(poseStack, builder, light, 7/16f, 7/16f, 9/16f, 9/16f, 9/16f, 12/16f); // 1
        drawBox(poseStack, builder, light, 9/16f, 7/16f, 7/16f, 12/16f, 9/16f, 12/16f); // 2
        drawBox(poseStack, builder, light, 4/16f, 7/16f, 4/16f, 7/16f, 9/16f, 12/16f); // 3
        drawBox(poseStack, builder, light, 7/16f, 7/16f, 4/16f, 12/16f, 9/16f, 7/16f); // 4
        drawBox(poseStack, builder, light, 12/16f, 7/16f, 7/16f, 16/16f, 9/16f, 9/16f); // 5
        drawBox(poseStack, builder, light, 0/16f, 7/16f, 7/16f, 4/16f, 9/16f, 9/16f); // 6
        drawBox(poseStack, builder, light, 7/16f, 7/16f, 12/16f, 9/16f, 9/16f, 16/16f); // 7
        drawBox(poseStack, builder, light, 7/16f, 7/16f, 0/16f, 9/16f, 9/16f, 4/16f); // 8

// Кубы 9-12 с поворотом на 45°
// Куб 9: origin (2, 7, 14)
        poseStack.pushPose();
        poseStack.translate(2/16f, 7/16f, 14/16f);
        poseStack.mulPose(Axis.YP.rotationDegrees(45));
        poseStack.translate(-2/16f, -7/16f, -14/16f);
        drawBox(poseStack, builder, light, 2/16f, 7/16f, 13/16f, 6/16f, 9/16f, 15/16f);
        poseStack.popPose();

// Куб 10: origin (12, 7, 12)
        poseStack.pushPose();
        poseStack.translate(12/16f, 7/16f, 12/16f);
        poseStack.mulPose(Axis.YP.rotationDegrees(45));
        poseStack.translate(-12/16f, -7/16f, -12/16f);
        drawBox(poseStack, builder, light, 11/16f, 7/16f, 11/16f, 13/16f, 9/16f, 15/16f);
        poseStack.popPose();

// Куб 11: origin (12, 7, 4)
        poseStack.pushPose();
        poseStack.translate(12/16f, 7/16f, 4/16f);
        poseStack.mulPose(Axis.YP.rotationDegrees(45));
        poseStack.translate(-12/16f, -7/16f, -4/16f);
        drawBox(poseStack, builder, light, 11/16f, 7/16f, 3/16f, 15/16f, 9/16f, 5/16f);
        poseStack.popPose();

// Куб 12: origin (4, 7, 4)
        poseStack.pushPose();
        poseStack.translate(4/16f, 7/16f, 4/16f);
        poseStack.mulPose(Axis.YP.rotationDegrees(45));
        poseStack.translate(-4/16f, -7/16f, -4/16f);
        drawBox(poseStack, builder, light, 3/16f, 7/16f, 1/16f, 5/16f, 9/16f, 5/16f);
        poseStack.popPose();

        // В render(), после отрисовки всех кубов и перед popPose():
        ItemStack stack = tile.itemHandler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5 , 0.6 - 2/16f, 0.5); // над жерновами (Y=9/16 + немного)
            poseStack.scale(0.5f, 0.5f, 0.5f);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.GROUND,
                    light,
                    overlay,
                    poseStack,
                    buffer,
                    tile.getLevel(),
                    0
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void drawBox(PoseStack poseStack, VertexConsumer builder, int light,
                         float x1, float y1, float z1, float x2, float y2, float z2) {
        float u1 = 0, u2 = 1, v1 = 0, v2 = 1;
        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();

        // North
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, -1).endVertex();

        // South
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 0, 1).endVertex();

        // West
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, -1, 0, 0).endVertex();

        // East
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 1, 0, 0).endVertex();

        // Down
        builder.vertex(matrix, x1, y1, z1).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x1, y1, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z2).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();
        builder.vertex(matrix, x2, y1, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, -1, 0).endVertex();

        // Up face — инвертируем UV
        builder.vertex(matrix, x1, y2, z1).color(255,255,255,255).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z1).color(255,255,255,255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255,255,255,255).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
        builder.vertex(matrix, x1, y2, z2).color(255,255,255,255).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.renderDistance().get() * 16;
    }
}
