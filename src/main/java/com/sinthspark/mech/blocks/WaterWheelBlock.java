package com.sinthspark.mech.blocks;

import com.sinthspark.mech.blockentity.ModBlockEntities;
import com.sinthspark.mech.blockentity.WaterWheelTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaterWheelBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty CENTER = BooleanProperty.create("center");

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0, -16, 4, 16, 32, 13),
            Block.box(-16, 0, 4, 32, 16, 13),
            Block.box(-14, 16, 4, 30, 20, 13),
            Block.box(-12, 20, 4, 28, 24, 13),
            Block.box(-10, 24, 4, 26, 27, 13),
            Block.box(-7, 27, 4, 23, 29, 13),
            Block.box(-5, 29, 4, 21, 31, 13),
            Block.box(-14, -4, 4, 30, 0, 13),
            Block.box(-12, -8, 4, 28, -4, 13),
            Block.box(-10, -11, 4, 26, -8, 13),
            Block.box(-7, -13, 4, 23, -11, 13),
            Block.box(-5, -15, 4, 21, -13, 13),
            Block.box(7, 7, 0, 9, 9, 4)
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(0, -16, 4, 16, 32, 13),
            Block.box(-16, 0, 4, 32, 16, 13),
            Block.box(-14, 16, 4, 30, 20, 13),
            Block.box(-12, 20, 4, 28, 24, 13),
            Block.box(-10, 24, 4, 26, 27, 13),
            Block.box(-7, 27, 4, 23, 29, 13),
            Block.box(-5, 29, 4, 21, 31, 13),
            Block.box(-14, -4, 4, 30, 0, 13),
            Block.box(-12, -8, 4, 28, -4, 13),
            Block.box(-10, -11, 4, 26, -8, 13),
            Block.box(-7, -13, 4, 23, -11, 13),
            Block.box(-5, -15, 4, 21, -13, 13),
            Block.box(7, 7, 13, 9, 9, 16)
    );

    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(4, -16, 0, 13, 32, 16),
            Block.box(4, 0, -16, 13, 16, 32),
            Block.box(4, 16, -14, 13, 20, 30),
            Block.box(4, 20, -12, 13, 24, 28),
            Block.box(4, 24, -10, 13, 27, 26),
            Block.box(4, 27, -7, 13, 29, 23),
            Block.box(4, 29, -5, 13, 31, 21),
            Block.box(4, -4, -14, 13, 0, 30),
            Block.box(4, -8, -12, 13, -4, 28),
            Block.box(4, -11, -10, 13, -8, 26),
            Block.box(4, -13, -7, 13, -11, 23),
            Block.box(4, -15, -5, 13, -13, 21),
            Block.box(0, 7, 7, 4, 9, 9)
    );

    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(4, -16, 0, 13, 32, 16),
            Block.box(4, 0, -16, 13, 16, 32),
            Block.box(4, 16, -14, 13, 20, 30),
            Block.box(4, 20, -12, 13, 24, 28),
            Block.box(4, 24, -10, 13, 27, 26),
            Block.box(4, 27, -7, 13, 29, 23),
            Block.box(4, 29, -5, 13, 31, 21),
            Block.box(4, -4, -14, 13, 0, 30),
            Block.box(4, -8, -12, 13, -4, 28),
            Block.box(4, -11, -10, 13, -8, 26),
            Block.box(4, -13, -7, 13, -11, 23),
            Block.box(4, -15, -5, 13, -13, 21),
            Block.box(13, 7, 7, 16, 9, 9)
    );

    public WaterWheelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CENTER, false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                        @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (!state.getValue(CENTER)) {
            return Shapes.block();
        }

        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH; // NORTH и все остальные
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                 @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return state.getValue(CENTER) ? new WaterWheelTile(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        if (!state.getValue(CENTER)) return null;
        if (type != ModBlockEntities.WATER_WHEEL.get()) return null;
        return (lvl, pos, st, tile) -> WaterWheelTile.tick(lvl, pos, st, (WaterWheelTile) tile);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            level.setBlock(pos, state.setValue(CENTER, true), 3);
            placeWheelBlocks(level, pos, facing);
        }
    }

    private void placeWheelBlocks(Level level, BlockPos centerPos, Direction facing) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                BlockPos partPos;
                if (facing.getAxis() == Direction.Axis.Z) {
                    partPos = centerPos.offset(dx, dy, 0);
                } else {
                    partPos = centerPos.offset(0, dy, dx);
                }

                level.setBlock(partPos, this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(CENTER, false), 3);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    private BlockPos findCenterPos(Level level, BlockPos partPos) {
        BlockState state = level.getBlockState(partPos);
        if (state.getBlock() instanceof WaterWheelBlock && state.getValue(CENTER)) {
            return partPos;
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = partPos.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);

                    if (checkState.getBlock() instanceof WaterWheelBlock && checkState.getValue(CENTER)) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    private void removeAllWheelBlocks(Level level, BlockPos centerPos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    BlockPos partPos = centerPos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(partPos);

                    if (state.getBlock() instanceof WaterWheelBlock) {
                        level.removeBlock(partPos, false);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();

        if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            return null;
        }

        BlockPos wheelPos = context.getClickedPos();
        Level level = context.getLevel();
        Direction wheelFacing = clickedFace.getOpposite();

        BlockPos bearingPos = wheelPos.relative(wheelFacing);
        BlockState bearingState = level.getBlockState(bearingPos);

        if (!(bearingState.getBlock() instanceof VulcaniteBearingBlock)) {
            return null;
        }

        Direction bearingFacing = bearingState.getValue(VulcaniteBearingBlock.FACING);
        if (bearingFacing != wheelFacing) {
            return null;
        }

        if (!canPlaceWheel(level, wheelPos, wheelFacing)) {
            return null;
        }

        return this.defaultBlockState().setValue(FACING, wheelFacing).setValue(CENTER, true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canBeReplaced(@NotNull BlockState state, @NotNull BlockPlaceContext useContext) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState rotate(@NotNull BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState mirror(@NotNull BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    private boolean canPlaceWheel(Level level, BlockPos centerPos, Direction facing) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos checkPos;
                if (facing.getAxis() == Direction.Axis.Z) {
                    checkPos = centerPos.offset(dx, dy, 0);
                } else {
                    checkPos = centerPos.offset(0, dy, dx);
                }

                if (dx == 0 && dy == 0) continue;
                if (checkPos.equals(centerPos.relative(facing.getOpposite()))) continue;

                BlockState state = level.getBlockState(checkPos);
                if (state.getBlock() instanceof VulcaniteBearingBlock) continue;

                boolean isFluid = !state.getFluidState().isEmpty();
                if (!state.isAir() && !isFluid) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CENTER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        if (!state.getValue(CENTER)) {
            return RenderShape.INVISIBLE;
        }
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void attack(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        if (!level.isClientSide) {
            BlockPos centerPos = state.getValue(CENTER) ? pos : findCenterPos(level, pos);

            if (centerPos != null) {
                BlockState centerState = level.getBlockState(centerPos);
                if (centerState.getBlock() instanceof WaterWheelBlock) {
                    removeAllWheelBlocks(level, centerPos);

                    if (!player.isCreative()) {
                        ItemStack drop = new ItemStack(this);
                        popResource(level, centerPos, drop);
                    }

                    level.removeBlock(centerPos, false);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isCollisionShapeFullBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return !state.getValue(CENTER);
    }

    @Override
    public boolean onDestroyedByPlayer(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                       @NotNull Player player, boolean willHarvest, @NotNull FluidState fluid) {
        if (!level.isClientSide) {
            BlockPos centerPos = state.getValue(CENTER) ? pos : findCenterPos(level, pos);

            if (centerPos != null) {
                BlockState centerState = level.getBlockState(centerPos);
                if (centerState.getBlock() instanceof WaterWheelBlock) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                BlockPos partPos = centerPos.offset(dx, dy, dz);
                                if (partPos.equals(pos)) continue;

                                BlockState partState = level.getBlockState(partPos);
                                if (partState.getBlock() instanceof WaterWheelBlock) {
                                    level.removeBlock(partPos, false);
                                }
                            }
                        }
                    }

                    if (!player.isCreative() && (state.getValue(CENTER) || !level.getBlockState(centerPos).isAir())) {
                        ItemStack drop = new ItemStack(this);
                        popResource(level, centerPos, drop);
                    }
                }
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                                  @NotNull Player player) {
        if (!level.isClientSide && player.isCreative()) {
            BlockPos centerPos = state.getValue(CENTER) ? pos : findCenterPos(level, pos);
            if (centerPos != null && !centerPos.equals(pos)) {
                level.removeBlock(centerPos, false);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return !state.getValue(CENTER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.getValue(CENTER) ? super.getShadeBrightness(state, level, pos) : 1.0f;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean useShapeForLightOcclusion(@NotNull BlockState state) {
        return state.getValue(CENTER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock,
                                @NotNull BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (state.getValue(CENTER)) {
                for (Direction dir : Direction.values()) {
                    FluidState fluidState = level.getFluidState(pos.relative(dir));
                    if (fluidState.getType() == Fluids.LAVA || fluidState.getType() == Fluids.FLOWING_LAVA) {
                        destroyWholeWheel(level, pos, state);
                        return;
                    }
                }

                Direction facing = state.getValue(FACING);
                BlockPos bearingPos = pos.relative(facing);
                if (neighborPos.equals(bearingPos)) {
                    BlockState bearingState = level.getBlockState(bearingPos);
                    if (!(bearingState.getBlock() instanceof VulcaniteBearingBlock)) {
                        destroyWholeWheel(level, pos, state);
                        return;
                    }
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            for (Direction dir : Direction.values()) {
                FluidState fluidState = level.getFluidState(pos.relative(dir));
                if (fluidState.getType() == Fluids.LAVA || fluidState.getType() == Fluids.FLOWING_LAVA) {
                    destroyWholeWheel(level, pos, state);
                    return;
                }
            }
        }
    }

    private void destroyWholeWheel(Level level, BlockPos pos, BlockState state) {
        BlockPos centerPos = state.getValue(CENTER) ? pos : findCenterPos(level, pos);

        if (centerPos != null) {
            BlockState centerState = level.getBlockState(centerPos);
            if (centerState.getBlock() instanceof WaterWheelBlock) {
                dropResources(centerState, level, centerPos, null, null, ItemStack.EMPTY);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos partPos = centerPos.offset(dx, dy, dz);
                            if (level.getBlockState(partPos).getBlock() instanceof WaterWheelBlock) {
                                level.removeBlock(partPos, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isFlammable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return 60;
    }

    @Override
    public int getFireSpreadSpeed(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return 30;
    }
}

