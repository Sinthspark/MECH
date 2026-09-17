package com.sinthspark.mech.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sinthspark.mech.blockentity.VulcaniteGearTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class VulcaniteGearRenderer implements BlockEntityRenderer<VulcaniteGearTile> {
    private final BlockRenderDispatcher blockRenderer;

    public VulcaniteGearRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(VulcaniteGearTile tile, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();

        // Центрируем, вращаем, возвращаем
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(tile.getRotation(partialTick)));
        poseStack.translate(-0.5, -0.5, -0.5);

        BlockState state = tile.getBlockState();
        BakedModel model = blockRenderer.getBlockModel(state);

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.solid()),
                state,
                model,
                1.0f, 1.0f, 1.0f,
                light,
                overlay,
                ModelData.EMPTY,
                RenderType.solid()
        );

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.renderDistance().get() * 16;
    }
}
