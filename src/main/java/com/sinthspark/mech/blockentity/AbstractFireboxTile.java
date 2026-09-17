package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.api.IBoilerTank;
import com.sinthspark.mech.api.IHeatSource;
import com.sinthspark.mech.registration.FluidRegistry;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Shared fuel, heat, inventory, persistence and capability logic for all firebox tiers. */
public abstract class AbstractFireboxTile extends BlockEntity implements MenuProvider, IHeatSource {
    public int burnTime, maxBurnTime, temperature, heatCounter;
    private final int heatInterval, maxTemperature;
    private final ItemStackHandler itemHandler;
    private final LazyOptional<IItemHandler> itemCapability, inputCapability, outputCapability;
    protected AbstractFireboxTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int heatInterval, int maxTemperature) {
        super(type, pos, state);
        this.heatInterval = heatInterval;
        this.maxTemperature = maxTemperature;
        this.itemHandler = new ItemStackHandler(2) {
            @Override protected void onContentsChanged(int slot) {
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    if (slot == 1) pushBucketToBoiler();
                }
            }
        };
        this.itemCapability = LazyOptional.of(() -> itemHandler);
        this.inputCapability = LazyOptional.of(this::createInputHandler);
        this.outputCapability = LazyOptional.of(this::createOutputHandler);
    }
    private IItemHandler createInputHandler() { return new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(0); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return isItemValid(slot, stack) ? itemHandler.insertItem(0, stack, simulate) : stack; }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(0); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return ForgeHooks.getBurnTime(stack, null) > 0; }
    }; }
    private IItemHandler createOutputHandler() { return new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(1); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return itemHandler.extractItem(1, amount, simulate); }
        @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(1); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
    }; }
    private void pushBucketToBoiler() {
        if (level == null || level.isClientSide) return;
        ItemStack bucket = itemHandler.getStackInSlot(1);
        if (bucket.isEmpty()) return;
        BlockEntity above = level.getBlockEntity(worldPosition.above());
        if (!(above instanceof IBoilerTank boiler)) return;
        FluidStack toFill = null;
        if (bucket.getItem() == Items.WATER_BUCKET) toFill = new FluidStack(Fluids.WATER, 1000);
        else if (bucket.getItem() == FluidRegistry.CREOSOTE.bucket.get()) toFill = new FluidStack(FluidRegistry.CREOSOTE.source.get(), 1000);
        if (toFill != null && boiler.fillWater(toFill, IFluidHandler.FluidAction.EXECUTE) == 1000) itemHandler.setStackInSlot(1, new ItemStack(Items.BUCKET));
    }
    protected abstract Component getFireboxDisplayName();
    protected abstract AbstractContainerMenu createFireboxMenu(int containerId, Inventory inventory, Player player);
    public static void tick(Level level, BlockPos pos, BlockState state, AbstractFireboxTile tile) {
        if (level.isClientSide) return;
        BlockEntity above = level.getBlockEntity(pos.above());
        if (above instanceof IBoilerTank boiler) {
            if (tile.burnTime > 0) {
                tile.burnTime--;
                tile.heatCounter++;
                if (tile.heatCounter >= tile.heatInterval) {
                    tile.heatCounter = 0;
                    tile.temperature = Math.min(tile.maxTemperature, tile.temperature + 1);
                }
                tile.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            } else tile.coolDown();
            if (tile.burnTime <= 0 && boiler.hasWater()) {
                ItemStack fuel = tile.itemHandler.getStackInSlot(0);
                int burnValue = ForgeHooks.getBurnTime(fuel, null);
                if (burnValue > 0) {
                    tile.burnTime = burnValue;
                    tile.maxBurnTime = burnValue;
                    fuel.shrink(1);
                    tile.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        } else {
            tile.burnTime = 0;
            tile.coolDown();
        }
    }
    private void coolDown() {
        heatCounter++;
        if (heatCounter >= heatInterval) {
            heatCounter = 0;
            temperature = Math.max(0, temperature - 1);
            setChanged();
            if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public ItemStackHandler getItemHandler() { return itemHandler; }
    @Override public int getTemperature() { return temperature; }
    @Override public @NotNull Component getDisplayName() { return getFireboxDisplayName(); }
    @Nullable @Override public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) { return createFireboxMenu(containerId, inventory, player); }
    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemCapability.cast();
            if (side == Direction.DOWN) return outputCapability.cast();
            return inputCapability.cast();
        }
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); itemCapability.invalidate(); inputCapability.invalidate(); outputCapability.invalidate(); }
    @Override protected void saveAdditional(@NotNull CompoundTag tag) { super.saveAdditional(tag); tag.putInt("burnTime", burnTime); tag.putInt("temperature", temperature); tag.put("inventory", itemHandler.serializeNBT()); tag.putInt("maxBurnTime", maxBurnTime); tag.putInt("heatCounter", heatCounter); }
    @Override public void load(@NotNull CompoundTag tag) { super.load(tag); burnTime = tag.getInt("burnTime"); temperature = tag.getInt("temperature"); itemHandler.deserializeNBT(tag.getCompound("inventory")); maxBurnTime = tag.getInt("maxBurnTime"); heatCounter = tag.getInt("heatCounter"); }
    @Override public @NotNull CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); tag.putInt("burnTime", burnTime); tag.putInt("temperature", temperature); tag.put("inventory", itemHandler.serializeNBT()); tag.putInt("maxBurnTime", maxBurnTime); tag.putInt("heatCounter", heatCounter); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag) { super.handleUpdateTag(tag); burnTime = tag.getInt("burnTime"); temperature = tag.getInt("temperature"); itemHandler.deserializeNBT(tag.getCompound("inventory")); maxBurnTime = tag.getInt("maxBurnTime"); heatCounter = tag.getInt("heatCounter"); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
