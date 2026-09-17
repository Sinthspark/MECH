package com.sinthspark.mech.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sinthspark.mech.blockentity.WaterWheelTile;
import com.sinthspark.mech.blocks.WaterWheelBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class WaterWheelRenderer implements BlockEntityRenderer<WaterWheelTile> {
    private final BlockRenderDispatcher blockRenderer;

    public WaterWheelRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(WaterWheelTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        BlockState state = tile.getBlockState();
        if (!state.hasProperty(WaterWheelBlock.CENTER) || !state.getValue(WaterWheelBlock.CENTER)) {
            return;
        }

        // ✅ Правильно получаем facing
        Direction facing = state.getValue(WaterWheelBlock.FACING);

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);

            // ✅ Выбираем ось вращения
            if (facing.getAxis() == Direction.Axis.Z) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(tile.getRotation(partialTick)));
            } else {
                poseStack.mulPose(Axis.XN.rotationDegrees(tile.getRotation(partialTick)));
            }

            poseStack.translate(-0.5, -0.5, -0.5);

            BakedModel model = blockRenderer.getBlockModel(state);

            if (model != null) {
                blockRenderer.getModelRenderer().renderModel(
                        poseStack.last(),
                        buffer.getBuffer(RenderType.cutout()),
                        state,
                        model,
                        1.0f, 1.0f, 1.0f,
                        light,
                        overlay,
                        ModelData.EMPTY,
                        RenderType.cutout()
                );
            }
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.renderDistance().get() * 16;
    }
}
