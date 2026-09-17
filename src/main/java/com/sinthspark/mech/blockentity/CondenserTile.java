package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.CondenserBlock;
import com.sinthspark.mech.energy.MechEnergyStorage;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.screen.CondenserMenu;
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

public class CondenserTile extends BlockEntity implements MenuProvider, IFluidHandler {

    private static final int ENERGY_CAPACITY = 3200;
    private static final int ENERGY_PER_TICK = 8; // конденсация требует меньше энергии чем нагрев
    private static final int INPUT_TANK_CAPACITY = 16000;
    private static final int OUTPUT_TANK_CAPACITY = 16000;
    private static final int TOTAL_WORK_TICKS = 200;

    public int progress = 0;
    public int maxProgress = TOTAL_WORK_TICKS;

    private final MechEnergyStorage energyStorage = new MechEnergyStorage(ENERGY_CAPACITY, 32, 32);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    private final FluidTank inputTank = new FluidTank(INPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank outputTank = new FluidTank(OUTPUT_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    // Слот выхода для твёрдого остатка (очищенная сера и т.д.)
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false; // только выдача
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    public CondenserTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDENSER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CondenserTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.isActive();
        boolean active = false;

        FluidStack inputFluid = tile.inputTank.getFluid();

        if (!inputFluid.isEmpty() && tile.energyStorage.getEnergyStored() >= ENERGY_PER_TICK) {
            boolean canOutputFluid = tile.outputTank.getFluidAmount() < tile.outputTank.getCapacity();
            ItemStack outputItem = tile.inventory.getStackInSlot(0);
            boolean canOutputItem = outputItem.isEmpty() || outputItem.getCount() < outputItem.getMaxStackSize();

            if (canOutputFluid && canOutputItem) {
                tile.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
                tile.progress++;
                active = true;

                if (tile.progress >= TOTAL_WORK_TICKS) {
                    // Конденсация пара в жидкость
                    if (inputFluid.getAmount() >= 100) {
                        FluidStack result = tile.getCondenseResult(inputFluid);
                        ItemStack byproduct = tile.getByproduct(inputFluid);

                        if (result != null) {
                            tile.inputTank.drain(100, IFluidHandler.FluidAction.EXECUTE);
                            tile.outputTank.fill(result, IFluidHandler.FluidAction.EXECUTE);
                        }

                        if (byproduct != null && !byproduct.isEmpty()) {
                            if (outputItem.isEmpty()) {
                                tile.inventory.setStackInSlot(0, byproduct.copy());
                            } else if (outputItem.getItem() == byproduct.getItem()) {
                                outputItem.grow(byproduct.getCount());
                            }
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
            level.setBlock(pos, state.setValue(CondenserBlock.ACTIVE, active), 3);
        }

        setChanged(level, pos, state);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    @Nullable
    private FluidStack getCondenseResult(FluidStack input) {
        if (input.getFluid() == FluidRegistry.DRY_STEAM.source.get()) {
            return new FluidStack(FluidRegistry.DISTILLED_WATER.source.get(), 80);
        }
        return null;
    }

    @Nullable
    private ItemStack getByproduct(FluidStack input) {
        if (input.getFluid() == FluidRegistry.SULFUR_STEAM.source.get()) {
            return new ItemStack(ItemRegistry.SULFUR.get(), 1);
        }
        return null;
    }

    public boolean isActive() { return progress > 0; }
    public IItemHandler getInventory() { return inventory; }
    public FluidTank getInputTank() { return inputTank; }
    public FluidTank getOutputTank() { return outputTank; }
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergy() { return ENERGY_CAPACITY; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return LazyOptional.of(() -> this).cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> new IItemHandler() {
                    @Override public int getSlots() { return 1; }
                    @Override public ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(0); }
                    @Override public ItemStack extractItem(int s, int a, boolean sim) { return inventory.extractItem(0, a, sim); }
                    @Override public ItemStack insertItem(int s, ItemStack stack, boolean sim) { return stack; }
                    @Override public int getSlotLimit(int s) { return 64; }
                    @Override public boolean isItemValid(int s, ItemStack stack) { return false; }
                }).cast();
            }
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); itemHandler.invalidate(); }
    @Override public Component getDisplayName() { return Component.translatable("block.mech.condenser"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new CondenserMenu(id, inv, this); }

    @Override public int getTanks() { return 2; }
    @Override public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? inputTank.getFluid() : outputTank.getFluid();
    }
    @Override public int getTankCapacity(int tank) {
        return tank == 0 ? inputTank.getCapacity() : outputTank.getCapacity();
    }
    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0;
    }
    @Override public int fill(FluidStack resource, FluidAction action) {
        return inputTank.fill(resource, action);
    }
    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return outputTank.drain(resource, action);
    }
    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return outputTank.drain(maxDrain, action);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
        tag.put("inputTank", inputTank.writeToNBT(new CompoundTag()));
        tag.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
        inputTank.readFromNBT(tag.getCompound("inputTank"));
        outputTank.readFromNBT(tag.getCompound("outputTank"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }
}
