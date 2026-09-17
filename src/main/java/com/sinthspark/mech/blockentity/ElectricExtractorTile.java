package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.ElectricExtractorBlock;
import com.sinthspark.mech.energy.MechEnergyStorage;
import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.screen.ElectricExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectricExtractorTile extends BlockEntity implements MenuProvider, IFluidHandler {

    private static final int ENERGY_CAPACITY = 500;
    private static final int ENERGY_PER_TICK = 32;
    private static final int OUTPUT_TANK_CAPACITY = 4000;

    public int progress = 0;
    public int maxProgress = 1;

    private final MechEnergyStorage energyStorage = new MechEnergyStorage(ENERGY_CAPACITY, 32, 32);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

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
            return slot == 0;
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    public ElectricExtractorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_EXTRACTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricExtractorTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.isActive();
        boolean active = false;

        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, tile.inventory.getStackInSlot(0));

        var recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.EXTRACTING_TYPE.get(), container, level)
                .orElse(null);

        if (recipe != null && tile.energyStorage.getEnergyStored() >= ENERGY_PER_TICK) {
            ItemStack output = tile.inventory.getStackInSlot(1);
            ItemStack result = recipe.getResult();
            FluidStack fluidResult = recipe.getFluidResult();

            boolean canFitItem = result.isEmpty() || output.isEmpty() ||
                    (output.is(result.getItem()) && output.getCount() + result.getCount() <= output.getMaxStackSize());
            boolean canFitFluid = fluidResult.isEmpty() ||
                    (tile.outputTank.fill(fluidResult, FluidAction.SIMULATE) >= fluidResult.getAmount());

            if (canFitItem && canFitFluid) {
                tile.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
                tile.maxProgress = recipe.getProcessTime();
                tile.progress++;
                active = true;

                if (tile.progress >= recipe.getProcessTime()) {
                    tile.inventory.extractItem(0, 1, false);

                    if (!result.isEmpty()) {
                        if (output.isEmpty()) {
                            tile.inventory.setStackInSlot(1, result.copy());
                        } else {
                            output.grow(result.getCount());
                        }
                    }

                    if (!fluidResult.isEmpty()) {
                        tile.outputTank.fill(fluidResult.copy(), FluidAction.EXECUTE);
                    }

                    tile.progress = 0;
                    active = false;
                }
            } else {
                tile.progress = 0;
            }
        } else {
            tile.progress = 0;
        }

        if (wasActive != active) {
            level.setBlock(pos, state.setValue(ElectricExtractorBlock.ACTIVE, active), 3);
        }

        setChanged(level, pos, state);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    public boolean isActive() { return progress > 0; }
    public IItemHandler getInventory() { return inventory; }
    public FluidStack getOutputFluid() { return outputTank.getFluid(); }
    public int getOutputFluidAmount() { return outputTank.getFluidAmount(); }
    public int getOutputTankCapacity() { return OUTPUT_TANK_CAPACITY; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> outputTank).cast();
            }
            return LazyOptional.empty();
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
                @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return true; }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); itemHandler.invalidate(); }
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech.electric_extractor"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return new ElectricExtractorMenu(id, inv, this); }

    @Override public int getTanks() { return outputTank.getTanks(); }
    @Override public @NotNull FluidStack getFluidInTank(int i) { return outputTank.getFluidInTank(i); }
    @Override public int getTankCapacity(int i) { return outputTank.getTankCapacity(i); }
    @Override public boolean isFluidValid(int i, @NotNull FluidStack s) { return outputTank.isFluidValid(i, s); }
    @Override public int fill(FluidStack r, FluidAction a) { return outputTank.fill(r, a); }
    @Override public @NotNull FluidStack drain(FluidStack r, FluidAction a) { return outputTank.drain(r, a); }
    @Override public @NotNull FluidStack drain(int m, FluidAction a) { return outputTank.drain(m, a); }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
        tag.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
        outputTank.readFromNBT(tag.getCompound("outputTank"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
        tag.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
        outputTank.readFromNBT(tag.getCompound("outputTank"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
    }
}