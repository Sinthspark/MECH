package com.sinthspark.mech.blocks;

import com.sinthspark.mech.blockentity.ModBlockEntities;
import com.sinthspark.mech.blockentity.VulcaniteGearTile;
import com.sinthspark.mech.registration.FluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VulcaniteGearBlock extends Block implements EntityBlock {

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 7, 7, 4, 9, 9),
            Block.box(12, 7, 7, 16, 9, 9),
            Block.box(7, 7, 12, 9, 9, 16),
            Block.box(7, 7, 0, 9, 9, 4),
            Block.box(7, 0, 7, 9, 16, 9),
            Block.box(4, 7, 4, 12, 9, 12)
    );

    public VulcaniteGearBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new VulcaniteGearTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        if (type != ModBlockEntities.VULCANITE_GEAR.get()) return null;
        return (lvl, pos, st, tile) -> VulcaniteGearTile.tick(lvl, pos, st, (VulcaniteGearTile) tile);
    }

    @Override
    public boolean canHarvestBlock(@NotNull BlockState state, @NotNull BlockGetter level,
                                   @NotNull BlockPos pos, @NotNull Player player) {
        return player.getMainHandItem().getItem() instanceof PickaxeItem;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
            if (be instanceof VulcaniteGearTile gear) {
                gear.applyLubricant();
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}