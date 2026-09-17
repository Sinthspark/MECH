package com.sinthspark.mech.blocks;

import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class CastillaLogBlock extends RotatedPillarBlock {

    public static final BooleanProperty TAPPED = BooleanProperty.create("tapped");
    public static final DirectionProperty FACING = DirectionProperty.create("facing", java.util.Arrays.asList(Direction.values()));
    public static final BooleanProperty LATEX = BooleanProperty.create("latex");

    public CastillaLogBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(TAPPED, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(LATEX, false));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TAPPED, FACING, LATEX);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level,
                                          @NotNull BlockPos pos, Player player,
                                          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() == Items.BUCKET && state.getValue(LATEX)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LATEX, false), 3);

                if (!player.isCreative()) {
                    held.shrink(1);
                }

                BucketItem latexBucket = FluidRegistry.LATEX.bucket.get();
                if (!player.getInventory().add(latexBucket.getDefaultInstance())) {
                    player.drop(latexBucket.getDefaultInstance(), false);
                }

                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.getItem() == ItemRegistry.KNIFE.get()) {
            if (state.getValue(TAPPED)) {
                return InteractionResult.PASS;
            }

            Direction clickedFace = hit.getDirection();
            Direction.Axis logAxis = state.getValue(AXIS);

            if (clickedFace.getAxis() == logAxis) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide) {
                level.setBlock(pos, state
                        .setValue(TAPPED, true)
                        .setValue(FACING, clickedFace)
                        .setValue(LATEX, false), 3);

                if (!player.isCreative()) {
                    held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }

                level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level,
                           @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(TAPPED) && !state.getValue(LATEX)) {
            if (random.nextFloat() < 0.05f) {
                level.setBlock(pos, state.setValue(LATEX, true), 3);
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return state.getValue(TAPPED) && !state.getValue(LATEX);
    }
}
