package com.sinthspark.mech.blocks;

import com.sinthspark.mech.blockentity.AdvancedBoilerTankTile;
import com.sinthspark.mech.blockentity.AdvancedFireboxTile;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.screen.AdvancedBoilerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedFireboxBlock extends Block implements EntityBlock {

    public AdvancedFireboxBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AdvancedFireboxTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof AdvancedFireboxTile tile) {
                AdvancedFireboxTile.tick(level1, pos, state1, tile);
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand,
                                          @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity beAbove = level.getBlockEntity(pos.above());

        if (beAbove instanceof AdvancedBoilerTankTile boiler) {
            if (heldItem.getItem() == Items.WATER_BUCKET) {
                int filled = boiler.getWaterTank().fill(new FluidStack(Fluids.WATER, 1000),
                        IFluidHandler.FluidAction.EXECUTE);
                if (filled == 1000) {
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            if (heldItem.getItem() == FluidRegistry.CREOSOTE.bucket.get()) {
                int filled = boiler.getWaterTank().fill(
                        new FluidStack(FluidRegistry.CREOSOTE.source.get(), 1000),
                        IFluidHandler.FluidAction.EXECUTE);
                if (filled == 1000) {
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            if (heldItem.getItem() == Items.BUCKET) {
                FluidStack fluid = boiler.getWaterTank().getFluid();
                if (fluid.getAmount() >= 1000) {
                    ItemStack bucketResult = getBucketForFluid(fluid);
                    if (!bucketResult.isEmpty()) {
                        boiler.getWaterTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                        if (!player.isCreative()) {
                            player.setItemInHand(hand, bucketResult);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            NetworkHooks.openScreen((ServerPlayer) player,
                    new SimpleMenuProvider((id, inv, p) -> new AdvancedBoilerMenu(id, inv, pos, level),
                            Component.translatable("gui.mech.firebox")),
                    pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @NotNull
    private ItemStack getBucketForFluid(FluidStack fluid) {
        if (fluid.getFluid() == Fluids.WATER) {
            return new ItemStack(Items.WATER_BUCKET);
        }
        if (fluid.getFluid() == FluidRegistry.CREOSOTE.source.get()) {
            return new ItemStack(FluidRegistry.CREOSOTE.bucket.get());
        }
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AdvancedFireboxTile firebox) {
                Containers.dropContents(level, pos,
                        new SimpleContainer(
                                firebox.getItemHandler().getStackInSlot(0),
                                firebox.getItemHandler().getStackInSlot(1)
                        ));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}