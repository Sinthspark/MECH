package com.sinthspark.mech.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class OilPressDummyBlock extends Block {
    public OilPressDummyBlock(Properties properties) {
        super(properties.noCollission().noOcclusion().instabreak().noLootTable());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                        @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.block();
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                                  @NotNull Player player) {
        super.playerWillDestroy(level, pos, state, player);

        if (!level.isClientSide) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);

            if (belowState.getBlock() instanceof OilPressBlock) {
                level.removeBlock(belowPos, false);
                if (!player.isCreative()) {
                    ItemStack drop = new ItemStack(belowState.getBlock());
                    popResource(level, belowPos, drop);
                }
            }
        }
    }
}
