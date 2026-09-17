package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.BlastFurnaceControllerBlock;
import com.sinthspark.mech.registration.BlockRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.screen.BlastFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlastFurnaceControllerTile extends BlockEntity implements MenuProvider {

    private static final int COOK_TIME = 2400;

    private boolean structureValid = false;
    public int progress = 0;
    public int maxProgress = COOK_TIME;

    private final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot < 3 && getStackInSlot(0).isEmpty()) {
                progress = 0;
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(Items.IRON_INGOT);
                case 1 -> stack.getItem() == ItemRegistry.COKE.get();
                case 2 -> stack.getItem() == BlockRegistry.LIMESTONE.get().asItem();
                default -> false;
            };
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);
    private final LazyOptional<IItemHandler> inputHandler = LazyOptional.of(() -> new IItemHandler() {
        @Override public int getSlots() { return 3; }
        @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(s); }
        @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return ItemStack.EMPTY; }
        @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return inventory.insertItem(s, stack, sim); }
        @Override public int getSlotLimit(int s) { return 64; }
        @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return s < 3; }
    });
    private final LazyOptional<IItemHandler> outputHandler = LazyOptional.of(() -> new IItemHandler() {
        @Override public int getSlots() { return 2; }
        @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(s + 3); }
        @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return inventory.extractItem(s + 3, a, sim); }
        @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return stack; }
        @Override public int getSlotLimit(int s) { return 64; }
        @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return false; }
    });

    public BlastFurnaceControllerTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLAST_FURNACE_CONTROLLER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlastFurnaceControllerTile tile) {
        if (level.isClientSide) return;

        if (level.getGameTime() % 100 == 0 || !tile.structureValid) {
            tile.structureValid = tile.checkStructure();
        }

        boolean wasActive = tile.getBlockState().getValue(BlastFurnaceControllerBlock.ACTIVE);
        boolean isActive = tile.progress > 0 && tile.structureValid;

        if (!tile.structureValid) {
            tile.progress = 0;
            if (wasActive) {
                level.setBlock(pos, tile.getBlockState().setValue(BlastFurnaceControllerBlock.ACTIVE, false), 3);
            }
            return;
        }

        ItemStack iron = tile.inventory.getStackInSlot(0);
        ItemStack coke = tile.inventory.getStackInSlot(1);
        ItemStack limestone = tile.inventory.getStackInSlot(2);

        if (!iron.isEmpty() && !coke.isEmpty() && !limestone.isEmpty()) {
            ItemStack castIronSlot = tile.inventory.getStackInSlot(3);
            ItemStack slagSlot = tile.inventory.getStackInSlot(4);

            boolean canFit = (castIronSlot.isEmpty() || castIronSlot.getCount() < castIronSlot.getMaxStackSize())
                    && (slagSlot.isEmpty() || slagSlot.getCount() < slagSlot.getMaxStackSize());

            if (canFit && level.getBlockState(tile.getCenterPos()).isAir()) {
                tile.progress++;
                isActive = true;

                if (tile.progress >= COOK_TIME) {
                    tile.inventory.extractItem(0, 1, false);
                    tile.inventory.extractItem(1, 1, false);
                    tile.inventory.extractItem(2, 1, false);

                    if (castIronSlot.isEmpty()) {
                        tile.inventory.setStackInSlot(3, new ItemStack(ItemRegistry.CAST_IRON_INGOT.get(), 1));
                    } else {
                        castIronSlot.grow(1);
                    }

                    if (slagSlot.isEmpty()) {
                        tile.inventory.setStackInSlot(4, new ItemStack(ItemRegistry.SLAG.get(), 1));
                    } else {
                        slagSlot.grow(1);
                    }

                    tile.progress = 0;
                    isActive = false;
                }
            } else {
                tile.progress = 0;
            }
        } else {
            tile.progress = 0;
        }

        if (wasActive != isActive) {
            level.setBlock(pos, tile.getBlockState().setValue(BlastFurnaceControllerBlock.ACTIVE, isActive), 3);
        }

        setChanged(level, pos, state);
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private BlockPos getCenterPos() {
        Direction forward = getBlockState().getValue(BlastFurnaceControllerBlock.FACING).getOpposite();
        return worldPosition.relative(forward).above();
    }

    private boolean checkStructure() {
        Direction facing = getBlockState().getValue(BlastFurnaceControllerBlock.FACING);
        BlockPos controllerPos = worldPosition;
        Direction forward = facing.getOpposite();
        Direction right = facing.getCounterClockWise();

        BlockPos centerHigh = controllerPos.above();
        BlockPos centerFrontLow = controllerPos.relative(forward);
        BlockPos centerFrontHigh = controllerPos.relative(forward).above();

        Block requiredBrick = BlockRegistry.BLAST_FURNACE_BRICKS.get();

        // Фундамент (y-1): 3×3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                if (!isBrick(controllerPos.offset(
                        dx * right.getStepX() + dz * forward.getStepX(),
                        -1,
                        dx * right.getStepZ() + dz * forward.getStepZ()
                ), requiredBrick)) return false;
            }
        }

        // Уровень y
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                BlockPos checkPos = controllerPos.offset(
                        dx * right.getStepX() + dz * forward.getStepX(),
                        0,
                        dx * right.getStepZ() + dz * forward.getStepZ()
                );
                if (checkPos.equals(controllerPos)) continue;
                if (checkPos.equals(centerFrontLow)) {
                    if (level == null || !level.getBlockState(checkPos).isAir()) return false;
                } else {
                    if (!isBrick(checkPos, requiredBrick)) return false;
                }
            }
        }

        // Уровень y+1
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                BlockPos checkPos = centerHigh.offset(
                        dx * right.getStepX() + dz * forward.getStepX(),
                        0,
                        dx * right.getStepZ() + dz * forward.getStepZ()
                );
                if (checkPos.equals(centerFrontHigh)) {
                    if (level == null || !level.getBlockState(checkPos).isAir()) return false;
                } else {
                    if (!isBrick(checkPos, requiredBrick)) return false;
                }
            }
        }

        // Верхний слой (y+2)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                if (!isBrick(centerHigh.offset(
                        dx * right.getStepX() + dz * forward.getStepX(),
                        1,
                        dx * right.getStepZ() + dz * forward.getStepZ()
                ), requiredBrick)) return false;
            }
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isBrick(BlockPos pos, Block brick) {
        return level != null && level.getBlockState(pos).getBlock() == brick;
    }

    public boolean isStructureValid() { return structureValid; }
    public void onDestroyed() { structureValid = false; }
    public IItemHandler getInventory() { return inventory; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandler.cast();
            if (side == Direction.DOWN) return outputHandler.cast();
            return inputHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        inputHandler.invalidate();
        outputHandler.invalidate();
    }

    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech.blast_furnace_controller"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new BlastFurnaceMenu(id, inv, this);
    }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putBoolean("structureValid", structureValid);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        structureValid = tag.getBoolean("structureValid");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putBoolean("structureValid", structureValid);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        maxProgress = tag.contains("maxProgress") ? tag.getInt("maxProgress") : COOK_TIME;
        structureValid = tag.getBoolean("structureValid");
    }
}
