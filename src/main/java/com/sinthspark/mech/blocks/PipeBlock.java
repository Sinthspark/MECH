package com.sinthspark.mech.blocks;

import com.sinthspark.mech.api.IPipe;
import com.sinthspark.mech.blockentity.PipeTile;
import com.sinthspark.mech.fluid.FluidNetworkManager;
import com.sinthspark.mech.registration.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PipeBlock extends Block implements EntityBlock, IPipe {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public static final BooleanProperty MANUAL_NORTH = BooleanProperty.create("manual_north");
    public static final BooleanProperty MANUAL_SOUTH = BooleanProperty.create("manual_south");
    public static final BooleanProperty MANUAL_EAST = BooleanProperty.create("manual_east");
    public static final BooleanProperty MANUAL_WEST = BooleanProperty.create("manual_west");
    public static final BooleanProperty MANUAL_UP = BooleanProperty.create("manual_up");
    public static final BooleanProperty MANUAL_DOWN = BooleanProperty.create("manual_down");

    private static final VoxelShape CENTER = box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape NORTH_CONN = box(5, 5, 0, 11, 11, 5);
    private static final VoxelShape SOUTH_CONN = box(5, 5, 11, 11, 11, 16);
    private static final VoxelShape EAST_CONN = box(11, 5, 5, 16, 11, 11);
    private static final VoxelShape WEST_CONN = box(0, 5, 5, 5, 11, 11);
    private static final VoxelShape UP_CONN = box(5, 11, 5, 11, 16, 11);
    private static final VoxelShape DOWN_CONN = box(5, 0, 5, 11, 5, 11);

    private final int capacity;
    private final int transferRate;
    private final float condenseRate;
    private final int explosionPower;

    public int getCapacity() { return capacity; }
    public int getTransferRate() { return transferRate; }
    public float getCondenseRate() { return condenseRate; }

    public PipeBlock(Properties properties, int capacity, int transferRate, float condenseRate, int explosionPower) {
        super(properties);
        this.capacity = capacity;
        this.transferRate = transferRate;
        this.condenseRate = condenseRate;
        this.explosionPower = explosionPower;
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false)
                .setValue(MANUAL_NORTH, false).setValue(MANUAL_SOUTH, false)
                .setValue(MANUAL_EAST, false).setValue(MANUAL_WEST, false)
                .setValue(MANUAL_UP, false).setValue(MANUAL_DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN,
                MANUAL_NORTH, MANUAL_SOUTH, MANUAL_EAST, MANUAL_WEST,
                MANUAL_UP, MANUAL_DOWN);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape = CENTER;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_CONN);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_CONN);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_CONN);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_CONN);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_CONN);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_CONN);
        return shape;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PipeTile(pos, state, capacity, transferRate, condenseRate);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof PipeTile tile) {
                PipeTile.tick(level1, pos, tile);
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        updateConnections(state, level, pos);

        // Обновляем ВСЕ соседние трубы
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof PipeBlock neighborPipe) {
                neighborPipe.updateConnections(neighborState, level, neighborPos);
            }
        }

        if (!level.isClientSide) {
            FluidNetworkManager.onPipePlaced(level, pos);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, BlockPos pos, @NotNull Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        Direction direction = null;
        if (neighborPos.getX() > pos.getX()) direction = Direction.EAST;
        else if (neighborPos.getX() < pos.getX()) direction = Direction.WEST;
        else if (neighborPos.getY() > pos.getY()) direction = Direction.UP;
        else if (neighborPos.getY() < pos.getY()) direction = Direction.DOWN;
        else if (neighborPos.getZ() > pos.getZ()) direction = Direction.SOUTH;
        else if (neighborPos.getZ() < pos.getZ()) direction = Direction.NORTH;

        if (direction != null) {
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (!(neighborBe instanceof net.minecraftforge.fluids.capability.IFluidHandler)) {
                BooleanProperty manualProp = getManualProperty(direction);
                if (state.getValue(manualProp)) {
                    state = state.setValue(manualProp, false);
                    level.setBlock(pos, state, 3);
                }
            }
        }
        updateConnections(state, level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof PipeTile pipe) {
                if (pipe.isInsulated() && !level.isClientSide) {
                    ItemStack insulatorStack = new ItemStack(BlockRegistry.PIPE_INSULATOR.get());
                    ItemEntity itemEntity = new ItemEntity(
                            level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            insulatorStack
                    );
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }

                if (!level.isClientSide) {
                    int steamAmount = pipe.getFluidAmount();
                    if (steamAmount > 800) {
                        float power = Math.min(explosionPower, steamAmount / 1000.0f);
                        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                power, true, Level.ExplosionInteraction.BLOCK);
                    }
                }
            }
        }
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                FluidNetworkManager.onPipeRemoved(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public PartType getPartType(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        Vec3 hitVec = player.pick(5.0, 1.0f, false).getLocation();
        Vec3 localHit = hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());

        if (localHit.x >= 5/16.0 && localHit.x <= 11/16.0 &&
                localHit.y >= 5/16.0 && localHit.y <= 11/16.0 &&
                localHit.z >= 5/16.0 && localHit.z <= 11/16.0) {
            return PartType.CENTER;
        }

        if (state.getValue(NORTH) && localHit.z <= 5/16.0) return PartType.NORTH;
        if (state.getValue(SOUTH) && localHit.z >= 11/16.0) return PartType.SOUTH;
        if (state.getValue(EAST) && localHit.x >= 11/16.0) return PartType.EAST;
        if (state.getValue(WEST) && localHit.x <= 5/16.0) return PartType.WEST;
        if (state.getValue(UP) && localHit.y >= 11/16.0) return PartType.UP;
        if (state.getValue(DOWN) && localHit.y <= 5/16.0) return PartType.DOWN;

        return PartType.NONE;
    }

    @Override
    public BooleanProperty getManualProperty(PartType part) {
        return switch (part) {
            case NORTH -> MANUAL_NORTH;
            case SOUTH -> MANUAL_SOUTH;
            case EAST -> MANUAL_EAST;
            case WEST -> MANUAL_WEST;
            case UP -> MANUAL_UP;
            case DOWN -> MANUAL_DOWN;
            default -> null;
        };
    }

    public void updateConnections(BlockState state, Level level, BlockPos pos) {
        boolean north = !state.getValue(MANUAL_NORTH) && isConnectable(level, pos.north(), Direction.SOUTH);
        boolean south = !state.getValue(MANUAL_SOUTH) && isConnectable(level, pos.south(), Direction.NORTH);
        boolean east = !state.getValue(MANUAL_EAST) && isConnectable(level, pos.east(), Direction.WEST);
        boolean west = !state.getValue(MANUAL_WEST) && isConnectable(level, pos.west(), Direction.EAST);
        boolean up = !state.getValue(MANUAL_UP) && isConnectable(level, pos.above(), Direction.DOWN);
        boolean down = !state.getValue(MANUAL_DOWN) && isConnectable(level, pos.below(), Direction.UP);

        BlockState newState = state
                .setValue(NORTH, north)
                .setValue(SOUTH, south)
                .setValue(EAST, east)
                .setValue(WEST, west)
                .setValue(UP, up)
                .setValue(DOWN, down);

        if (!state.equals(newState)) {
            level.setBlock(pos, newState, 3);
            if (level.getBlockEntity(pos) instanceof PipeTile tile) {
                tile.updateConnections(north, south, east, west, up, down);
            }
        }
    }

    private boolean isConnectable(Level level, BlockPos neighborPos, Direction sideToNeighbor) {
        if (level == null) return false;
        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be == null) return false;
        return be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, sideToNeighbor).isPresent();
    }

    public BooleanProperty getConnectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    public BooleanProperty getManualProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> MANUAL_NORTH;
            case SOUTH -> MANUAL_SOUTH;
            case EAST -> MANUAL_EAST;
            case WEST -> MANUAL_WEST;
            case UP -> MANUAL_UP;
            case DOWN -> MANUAL_DOWN;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof PipeTile pipe)) {
            return InteractionResult.PASS;
        }

        // Проверка на изолятор
        if (held.getItem() == BlockRegistry.PIPE_INSULATOR.get().asItem()) {
            if (pipe.isInsulated()) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide) {
                pipe.setInsulated(true);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Снятие изолятора
        if (player.isShiftKeyDown() && held.isEmpty() && pipe.isInsulated()) {
            if (!level.isClientSide) {
                pipe.setInsulated(false);
                ItemStack insulatorStack = new ItemStack(BlockRegistry.PIPE_INSULATOR.get());
                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, insulatorStack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
