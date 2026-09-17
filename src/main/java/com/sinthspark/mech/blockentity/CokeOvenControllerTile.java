package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.CokeOvenControllerBlock;
import com.sinthspark.mech.registration.BlockRegistry;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.screen.CokeOvenMenu;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CokeOvenControllerTile extends BlockEntity implements MenuProvider, IFluidHandler {

    private static final int OUTPUT_TANK_CAPACITY = 4000;
    private static final int COOK_TIME = 1800;

    private boolean structureValid = false;
    private BlockPos centerPos = null;

    public int progress = 0;
    public int maxProgress = COOK_TIME;

    private final FluidTank outputTank = new FluidTank(OUTPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == 0 && getStackInSlot(0).isEmpty()) {
                progress = 0;
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && stack.is(Items.COAL);
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    public CokeOvenControllerTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COKE_OVEN_CONTROLLER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CokeOvenControllerTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.getBlockState().getValue(CokeOvenControllerBlock.ACTIVE);
        boolean isActive = tile.progress > 0 && tile.structureValid;

        if (level.getGameTime() % 100 == 0 || !tile.structureValid) {
            tile.structureValid = tile.checkStructure();
        }

        if (!tile.structureValid) {
            tile.progress = 0;
            if (wasActive) {
                level.setBlock(pos, tile.getBlockState().setValue(CokeOvenControllerBlock.ACTIVE, false), 3);
            }
            return;
        }

        ItemStack input = tile.inventory.getStackInSlot(0);

        if (!input.isEmpty() && input.is(Items.COAL)) {
            ItemStack output = tile.inventory.getStackInSlot(1);

            if (output.isEmpty() || (output.is(ItemRegistry.COKE.get()) && output.getCount() < output.getMaxStackSize())) {
                if (tile.outputTank.getFluidAmount() + 100 <= tile.outputTank.getCapacity()) {
                    if (tile.centerPos != null && level.getBlockState(tile.centerPos).isAir()) {
                        tile.progress++;
                        isActive = true;

                        if (tile.progress >= COOK_TIME) {
                            tile.inventory.extractItem(0, 1, false);
                            if (output.isEmpty()) {
                                tile.inventory.setStackInSlot(1, new ItemStack(ItemRegistry.COKE.get(), 1));
                            } else {
                                output.grow(1);
                            }
                            tile.outputTank.fill(new FluidStack(FluidRegistry.CREOSOTE.source.get(), 100), FluidAction.EXECUTE);
                            tile.progress = 0;
                            isActive = false;
                        }
                    } else {
                        tile.progress = 0;
                    }
                }
            } else {
                tile.progress = 0;
            }
        } else {
            tile.progress = 0;
        }

        if (wasActive != isActive) {
            level.setBlock(pos, tile.getBlockState().setValue(CokeOvenControllerBlock.ACTIVE, isActive), 3);
        }

        setChanged(level, pos, state);
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private boolean checkStructure() {
        assert level != null;
        Direction facing = getBlockState().getValue(CokeOvenControllerBlock.FACING);
        BlockPos controllerPos = worldPosition;
        Block requiredBrick = BlockRegistry.COKE_OVEN_BRICKS.get();

        BlockPos center = controllerPos.relative(facing.getOpposite());
        this.centerPos = center;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos checkPos = center.offset(dx, 0, dz);
                if (checkPos.equals(controllerPos)) continue;
                if (level.getBlockState(checkPos).getBlock() != requiredBrick) return false;
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getBlockState(center.offset(dx, 1, dz)).getBlock() != requiredBrick) return false;
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getBlockState(center.offset(dx, -1, dz)).getBlock() != requiredBrick) return false;
            }
        }

        return level.getBlockState(center).isAir();
    }

    public boolean isStructureValid() { return structureValid; }
    public void onDestroyed() { structureValid = false; centerPos = null; }
    public IItemHandler getInventory() { return inventory; }
    public int getOutputFluidAmount() { return outputTank.getFluidAmount(); }
    public int getOutputTankCapacity() { return OUTPUT_TANK_CAPACITY; }
    public FluidStack getOutputFluid() { return outputTank.getFluid(); }
    public FluidTank getOutputTank() { return outputTank; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return LazyOptional.of(() -> outputTank).cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandler.cast();
            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> new IItemHandler() {
                    @Override public int getSlots() { return 1; }
                    @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(1); }
                    @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return inventory.extractItem(1, a, sim); }
                    @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return stack; }
                    @Override public int getSlotLimit(int s) { return 64; }
                    @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return false; }
                }).cast();
            }
            return LazyOptional.of(() -> new IItemHandler() {
                @Override public int getSlots() { return 1; }
                @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(0); }
                @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return ItemStack.EMPTY; }
                @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return inventory.insertItem(0, stack, sim); }
                @Override public int getSlotLimit(int s) { return 64; }
                @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return stack.is(Items.COAL); }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); itemHandler.invalidate(); }
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech.coke_oven_controller"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return new CokeOvenMenu(id, inv, this); }

    @Override public int getTanks() { return outputTank.getTanks(); }
    @Override public @NotNull FluidStack getFluidInTank(int i) { return outputTank.getFluidInTank(i); }
    @Override public int getTankCapacity(int i) { return outputTank.getTankCapacity(i); }
    @Override public boolean isFluidValid(int i, @NotNull FluidStack s) { return outputTank.isFluidValid(i, s); }
    @Override public int fill(FluidStack r, FluidAction a) { return outputTank.fill(r, a); }
    @Override public @NotNull FluidStack drain(FluidStack r, FluidAction a) { return outputTank.drain(r, a); }
    @Override public @NotNull FluidStack drain(int m, FluidAction a) { return outputTank.drain(m, a); }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putBoolean("structureValid", structureValid);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        outputTank.readFromNBT(tag.getCompound("outputTank"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        structureValid = tag.getBoolean("structureValid");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putBoolean("structureValid", structureValid);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        outputTank.readFromNBT(tag.getCompound("outputTank"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        maxProgress = tag.contains("maxProgress") ? tag.getInt("maxProgress") : COOK_TIME;
        structureValid = tag.getBoolean("structureValid");
    }
}