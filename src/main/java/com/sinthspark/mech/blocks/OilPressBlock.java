package com.sinthspark.mech.blocks;

import com.sinthspark.mech.blockentity.OilPressTile;
import com.sinthspark.mech.registration.BlockRegistry;
import com.sinthspark.mech.registration.FluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OilPressBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 7, 2, 14, 9),
            Block.box(7, 1, 0, 9, 14, 2),
            Block.box(7, 1, 14, 9, 14, 16),
            Block.box(14, 1, 7, 16, 14, 9),
            Block.box(7, 10, 7, 9, 29, 9),
            Block.box(0, 14, 7, 16, 16, 9),
            Block.box(7, 14, 0, 9, 16, 16),
            Block.box(4, 23, 4, 12, 25, 12),
            Block.box(12, 23, 7, 16, 25, 9),
            Block.box(7, 23, 12, 9, 25, 16),
            Block.box(0, 23, 7, 4, 25, 9),
            Block.box(7, 23, 0, 9, 25, 4)
    );

    public OilPressBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new OilPressTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof OilPressTile tile) {
                OilPressTile.tick(level1, pos, state1, tile);
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                        @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand,
                                          @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() == FluidRegistry.RAPESEED_OIL.bucket.get()) {
            if (be instanceof OilPressTile oilPress) {
                oilPress.applyLubricant();
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
        }

        if (heldItem.getItem() == Items.BUCKET) {
            if (be instanceof OilPressTile oilPress) {
                FluidStack fluid = oilPress.getFluidTank().getFluid();
                if (fluid.getAmount() >= 1000) {
                    oilPress.getFluidTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    player.setItemInHand(hand, new ItemStack(FluidRegistry.RAPESEED_OIL.bucket.get()));
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (be instanceof OilPressTile oilPress) {
            NetworkHooks.openScreen((ServerPlayer) player, oilPress, pos);
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (!level.getBlockState(pos.above()).isAir()) {
            return null;
        }

        return this.defaultBlockState();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.setBlock(pos.above(), BlockRegistry.OIL_PRESS_DUMMY.get().defaultBlockState(), 3);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
            if (level.getBlockState(pos.above()).getBlock() == BlockRegistry.OIL_PRESS_DUMMY.get()) {
                level.removeBlock(pos.above(), false);
            }
        }
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof OilPressTile oilPress) {
                SimpleContainer inventory = new SimpleContainer(1);
                inventory.setItem(0, oilPress.itemHandler.getStackInSlot(0));
                Containers.dropContents(level, pos, inventory);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}