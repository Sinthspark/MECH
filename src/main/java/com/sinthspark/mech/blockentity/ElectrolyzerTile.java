package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.ElectrolyzerBlock;
import com.sinthspark.mech.energy.MechEnergyStorage;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.screen.ElectrolyzerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
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

public class ElectrolyzerTile extends BlockEntity implements MenuProvider, IFluidHandler {

    private static final int ENERGY_CAPACITY = 3200;
    private static final int ENERGY_PER_TICK = 32;
    private static final int TANK_CAPACITY = 8000;
    private static final int TOTAL_WORK_TICKS = 300;

    public int progress = 0;
    public int maxProgress = TOTAL_WORK_TICKS;

    private final MechEnergyStorage energyStorage = new MechEnergyStorage(ENERGY_CAPACITY, 32, 32);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    private final FluidTank inputTank = new FluidTank(TANK_CAPACITY);
    private final FluidTank outputTank1 = new FluidTank(TANK_CAPACITY);
    private final FluidTank outputTank2 = new FluidTank(TANK_CAPACITY);

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
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

    public ElectrolyzerTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYZER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectrolyzerTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.isActive();
        boolean active = false;

        FluidStack inputFluid = tile.inputTank.getFluid();
        ItemStack inputItem = tile.inventory.getStackInSlot(0);

        boolean hasFluidInput = !inputFluid.isEmpty();
        boolean hasItemInput = !inputItem.isEmpty();

        if ((hasFluidInput || hasItemInput) && tile.energyStorage.getEnergyStored() >= ENERGY_PER_TICK) {
            ItemStack outputItem = tile.inventory.getStackInSlot(1);
            boolean canOutputFluid1 = tile.outputTank1.getFluidAmount() < tile.outputTank1.getCapacity();
            boolean canOutputFluid2 = tile.outputTank2.getFluidAmount() < tile.outputTank2.getCapacity();
            boolean canOutputItem = outputItem.isEmpty() || outputItem.getCount() < outputItem.getMaxStackSize();

            if (canOutputFluid1 && canOutputFluid2 && canOutputItem) {
                tile.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
                tile.progress++;
                active = true;

                if (tile.progress >= TOTAL_WORK_TICKS) {
                    // Жидкостный вход
                    if (hasFluidInput && inputFluid.getAmount() >= 100) {
                        FluidStack result1 = tile.getElectrolysisResult1(inputFluid);
                        FluidStack result2 = tile.getElectrolysisResult2(inputFluid);
                        boolean canFill1 = result1 == null || tile.outputTank1.getFluidAmount() + 100 <= tile.outputTank1.getCapacity();
                        boolean canFill2 = result2 == null || tile.outputTank2.getFluidAmount() + 100 <= tile.outputTank2.getCapacity();

                        if (canFill1 && canFill2) {
                            tile.inputTank.drain(100, IFluidHandler.FluidAction.EXECUTE);
                            if (result1 != null) tile.outputTank1.fill(result1, IFluidHandler.FluidAction.EXECUTE);
                            if (result2 != null) tile.outputTank2.fill(result2, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }

                    // Предметный вход — соль
                    if (hasItemInput && inputItem.getItem() == ItemRegistry.SALT.get()) {
                        boolean canOutputSodium = outputItem.isEmpty() || (outputItem.getItem() == ItemRegistry.SODIUM.get() && outputItem.getCount() < outputItem.getMaxStackSize());
                        boolean canOutputChlorine = tile.outputTank1.getFluidAmount() + 100 <= tile.outputTank1.getCapacity();

                        if (canOutputSodium && canOutputChlorine)  {
                            tile.inventory.extractItem(0, 1, false);
                            if (outputItem.isEmpty()) {
                                tile.inventory.setStackInSlot(1, new ItemStack(ItemRegistry.SODIUM.get(), 1));
                            } else {
                                outputItem.grow(1);
                            }
                            tile.outputTank1.fill(new FluidStack(FluidRegistry.CHLORINE.source.get(), 100), IFluidHandler.FluidAction.EXECUTE);
                        }
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
            level.setBlock(pos, state.setValue(ElectrolyzerBlock.ACTIVE, active), 3);
        }

        setChanged(level, pos, state);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    @Nullable
    private FluidStack getElectrolysisResult1(FluidStack input) {
        // Дистиллированная вода → водород
        if (input.getFluid() == FluidRegistry.DISTILLED_WATER.source.get()) {
            return new FluidStack(FluidRegistry.HYDROGEN.source.get(), 100);
        }
        return null;
    }

    @Nullable
    private FluidStack getElectrolysisResult2(FluidStack input) {
        // Дистиллированная вода → кислород
        if (input.getFluid() == FluidRegistry.DISTILLED_WATER.source.get()) {
            return new FluidStack(FluidRegistry.OXYGEN.source.get(), 50); // 2H₂O → 2H₂ + O₂ (водорода вдвое больше)
        }
        // Соль (NaCl) → натрий (твёрдый, выпадает в слот)
        return null;
    }

    @Nullable
    private ItemStack getItemElectrolysisResult(ItemStack input) {
        if (input.getItem() == ItemRegistry.SALT.get()) {
            return new ItemStack(ItemRegistry.SODIUM.get(), 1);
        }
        return null;
    }


    public boolean isActive() { return progress > 0; }
    public IItemHandler getInventory() { return inventory; }
    public FluidTank getInputTank() { return inputTank; }
    public FluidTank getOutputTank1() { return outputTank1; }
    public FluidTank getOutputTank2() { return outputTank2; }
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergy() { return ENERGY_CAPACITY; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return LazyOptional.of(() -> this).cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
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
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech.electrolyzer"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return new ElectrolyzerMenu(id, inv, this); }

    @Override public int getTanks() { return 3; }
    @Override public @NotNull FluidStack getFluidInTank(int tank) {
        return switch (tank) {
            case 0 -> inputTank.getFluid();
            case 1 -> outputTank1.getFluid();
            case 2 -> outputTank2.getFluid();
            default -> FluidStack.EMPTY;
        };
    }
    @Override public int getTankCapacity(int tank) { return TANK_CAPACITY; }
    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return tank == 0; }
    @Override public int fill(FluidStack resource, FluidAction action) { return inputTank.fill(resource, action); }
    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack drained = outputTank1.drain(resource, action);
        if (!drained.isEmpty()) return drained;
        return outputTank2.drain(resource, action);
    }
    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack drained = outputTank1.drain(maxDrain, action);
        if (!drained.isEmpty()) return drained;
        return outputTank2.drain(maxDrain, action);
    }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
        tag.put("inputTank", inputTank.writeToNBT(new CompoundTag()));
        tag.put("outputTank1", outputTank1.writeToNBT(new CompoundTag()));
        tag.put("outputTank2", outputTank2.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
        inputTank.readFromNBT(tag.getCompound("inputTank"));
        outputTank1.readFromNBT(tag.getCompound("outputTank1"));
        outputTank2.readFromNBT(tag.getCompound("outputTank2"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }
}