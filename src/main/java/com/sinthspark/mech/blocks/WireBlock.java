package com.sinthspark.mech.blocks;

import com.sinthspark.mech.api.IWire;
import com.sinthspark.mech.blockentity.WireTile;
import com.sinthspark.mech.energy.EnergyNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WireBlock extends Block implements EntityBlock, IWire {

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

    private static final VoxelShape CENTER = box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape NORTH_SIDE = box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SOUTH_SIDE = box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape EAST_SIDE = box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape WEST_SIDE = box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape UP_SIDE = box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape DOWN_SIDE = box(6, 0, 6, 10, 6, 10);

    private final int maxTransfer;
    private final int lossPerBlock;
    private final boolean insulated;

    public WireBlock(Properties properties, int maxTransfer, int lossPerBlock, boolean insulated) {
        super(properties);
        this.maxTransfer = maxTransfer;
        this.lossPerBlock = lossPerBlock;
        this.insulated = insulated;
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

    public boolean isInsulated() { return insulated; }
    public int getMaxTransfer() { return maxTransfer; }
    public int getLossPerBlock() { return lossPerBlock; }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape = CENTER;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SIDE);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SIDE);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SIDE);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SIDE);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_SIDE);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_SIDE);
        return shape;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WireTile(pos, state, maxTransfer, lossPerBlock, insulated);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof WireTile tile) {
                WireTile.tick(level1, pos, state1, tile);
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        updateConnections(state, level, pos);
        if (!level.isClientSide) {
            EnergyNetworkManager.scheduleNetworkUpdate(level, pos);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving) {
        updateConnections(state, level, pos);
        if (!level.isClientSide) {
            EnergyNetworkManager.scheduleNetworkUpdate(level, pos);
        }
        if (!level.isClientSide) {
            EnergyNetworkManager.onWirePlaced(level, pos);
        }
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
        }
    }

    private boolean isConnectable(Level level, BlockPos neighborPos, Direction sideToNeighbor) {
        if (level == null) return false;
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof WireBlock) return true;

        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be != null) {
            return be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY, sideToNeighbor).isPresent();
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                EnergyNetworkManager.scheduleNetworkUpdate(level, pos);
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof WireBlock neighborBlock) {
                    BooleanProperty neighborManual = neighborBlock.getManualProperty(direction.getOpposite());
                    if (neighborState.getValue(neighborManual)) {
                        neighborState = neighborState.setValue(neighborManual, false);
                        level.setBlock(neighborPos, neighborState, 3);
                        neighborBlock.updateConnections(neighborState, level, neighborPos);
                        EnergyNetworkManager.scheduleNetworkUpdate(level, neighborPos);
                    }
                }
            }
        }
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            EnergyNetworkManager.onWireRemoved(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public PartType getPartType(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        Vec3 hitVec = player.pick(5.0, 1.0f, false).getLocation();
        Vec3 localHit = hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());

        if (localHit.x >= 6/16.0 && localHit.x <= 10/16.0 &&
                localHit.y >= 6/16.0 && localHit.y <= 10/16.0 &&
                localHit.z >= 6/16.0 && localHit.z <= 10/16.0) {
            return PartType.CENTER;
        }

        if (state.getValue(NORTH) && localHit.z <= 6/16.0) return PartType.NORTH;
        if (state.getValue(SOUTH) && localHit.z >= 10/16.0) return PartType.SOUTH;
        if (state.getValue(EAST) && localHit.x >= 10/16.0) return PartType.EAST;
        if (state.getValue(WEST) && localHit.x <= 6/16.0) return PartType.WEST;
        if (state.getValue(UP) && localHit.y >= 10/16.0) return PartType.UP;
        if (state.getValue(DOWN) && localHit.y <= 6/16.0) return PartType.DOWN;

        return PartType.NONE;
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
}
