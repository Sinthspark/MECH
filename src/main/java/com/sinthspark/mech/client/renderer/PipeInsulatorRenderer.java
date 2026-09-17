package com.sinthspark.mech.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sinthspark.mech.api.IInsulatablePipe;
import com.sinthspark.mech.blocks.PipeBlock;
import com.sinthspark.mech.blocks.PipeInsulatorBlock;
import com.sinthspark.mech.registration.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class PipeInsulatorRenderer implements BlockEntityRenderer<BlockEntity> {

    @SuppressWarnings("unused")
    public PipeInsulatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull BlockEntity be, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (!(be instanceof IInsulatablePipe pipe)) return;
        if (!pipe.isInsulated()) return;

        BlockState pipeState = pipe.getBlockState();
        if (!(pipeState.getBlock() instanceof PipeBlock)) return;

        BlockState insulatorState = BlockRegistry.PIPE_INSULATOR.get().defaultBlockState()
                .setValue(PipeInsulatorBlock.NORTH, pipeState.getValue(PipeBlock.NORTH))
                .setValue(PipeInsulatorBlock.SOUTH, pipeState.getValue(PipeBlock.SOUTH))
                .setValue(PipeInsulatorBlock.EAST, pipeState.getValue(PipeBlock.EAST))
                .setValue(PipeInsulatorBlock.WEST, pipeState.getValue(PipeBlock.WEST))
                .setValue(PipeInsulatorBlock.UP, pipeState.getValue(PipeBlock.UP))
                .setValue(PipeInsulatorBlock.DOWN, pipeState.getValue(PipeBlock.DOWN));

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(insulatorState);

        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            return;
        }

        poseStack.pushPose();

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()),
                insulatorState,
                model,
                1.0f, 1.0f, 1.0f,
                packedLight,
                packedOverlay,
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

