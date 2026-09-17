package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.DistillerBlock;
import com.sinthspark.mech.energy.MechEnergyStorage;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.screen.DistillerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class DistillerTile extends BlockEntity implements MenuProvider, IFluidHandler {

    private static final int ENERGY_CAPACITY = 3200;
    private static final int ENERGY_PER_TICK = 16;
    private static final int INPUT_TANK_CAPACITY = 16000;
    private static final int OUTPUT_TANK_CAPACITY = 16000;
    private static final int TOTAL_WORK_TICKS = 200;

    public int progress = 0;
    public int maxProgress = TOTAL_WORK_TICKS;

    // Энергия
    private final MechEnergyStorage energyStorage = new MechEnergyStorage(ENERGY_CAPACITY, 32, 32);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    // Танки
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

    // Инвентарь: вход (0), выход (1)
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
            if (slot == 0) {
                return stack.getItem() == ItemRegistry.SULFUR.get();
            }
            return false;
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    public DistillerTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISTILLER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DistillerTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.isActive();
        boolean active = false;

        FluidStack inputFluid = tile.inputTank.getFluid();
        ItemStack inputItem = tile.inventory.getStackInSlot(0);
        boolean hasInput = !inputFluid.isEmpty() || !inputItem.isEmpty();

        if (hasInput && tile.energyStorage.getEnergyStored() >= ENERGY_PER_TICK) {
            ItemStack output = tile.inventory.getStackInSlot(1);
            boolean canOutputItem = output.isEmpty() || output.getCount() < output.getMaxStackSize();
            boolean canOutputFluid = tile.outputTank.getFluidAmount() < tile.outputTank.getCapacity();

            if (canOutputItem && canOutputFluid) {
                tile.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
                tile.progress++;
                active = true;

                if (tile.progress >= TOTAL_WORK_TICKS) {
                    // Обработка жидкости
                    if (!inputFluid.isEmpty() && inputFluid.getAmount() >= 100) {
                        FluidStack result = tile.getDistillResult(inputFluid);
                        if (result != null) {
                            tile.inputTank.drain(100, IFluidHandler.FluidAction.EXECUTE);
                            tile.outputTank.fill(result, IFluidHandler.FluidAction.EXECUTE);
                        }
                    }

                    // Обработка предмета
                    if (!inputItem.isEmpty()) {
                        ItemStack byproduct = tile.getItemByproduct(inputItem);
                        if (byproduct != null && !byproduct.isEmpty()) {
                            tile.inventory.extractItem(0, 1, false);
                            if (output.isEmpty()) {
                                tile.inventory.setStackInSlot(1, byproduct.copy());
                            } else {
                                output.grow(byproduct.getCount());
                            }
                        }
                        // Если жидкость на входе — даём соль
                        if (!inputFluid.isEmpty() && inputFluid.getFluid() == net.minecraft.world.level.material.Fluids.WATER) {
                            tile.inputTank.drain(100, IFluidHandler.FluidAction.EXECUTE);
                            tile.outputTank.fill(new FluidStack(FluidRegistry.DRY_STEAM.source.get(), 80), IFluidHandler.FluidAction.EXECUTE);
                            // Побочный продукт — соль (mech:salt)
                            ItemStack salt = new ItemStack(ItemRegistry.SALT.get(), 1);
                            if (output.isEmpty()) {
                                tile.inventory.setStackInSlot(1, salt);
                            } else if (output.getItem() == ItemRegistry.SALT.get()) {
                                output.grow(1);
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
            level.setBlock(pos, state.setValue(DistillerBlock.ACTIVE, active), 3);
        }

        setChanged(level, pos, state);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    @Nullable
    private FluidStack getDistillResult(FluidStack input) {
        if (input.getFluid() == net.minecraft.world.level.material.Fluids.WATER) {
            return new FluidStack(FluidRegistry.DRY_STEAM.source.get(), 80); // сухой пар
        }
        return null;
    }

    @Nullable
    private ItemStack getItemByproduct(ItemStack input) {
        if (input.is(ItemRegistry.SULFUR.get())) {
            return new ItemStack(Items.YELLOW_DYE, 1); // серный пар → сера (пока заглушка)
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
                    @Override public ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(1); }
                    @Override public ItemStack extractItem(int s, int a, boolean sim) { return inventory.extractItem(1, a, sim); }
                    @Override public ItemStack insertItem(int s, ItemStack stack, boolean sim) { return stack; }
                    @Override public int getSlotLimit(int s) { return 64; }
                    @Override public boolean isItemValid(int s, ItemStack stack) { return false; }
                }).cast();
            }
            return LazyOptional.of(() -> new IItemHandler() {
                @Override public int getSlots() { return 1; }
                @Override public ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(0); }
                @Override public ItemStack extractItem(int s, int a, boolean sim) { return ItemStack.EMPTY; }
                @Override public ItemStack insertItem(int s, ItemStack stack, boolean sim) { return inventory.insertItem(0, stack, sim); }
                @Override public int getSlotLimit(int s) { return 64; }
                @Override public boolean isItemValid(int s, ItemStack stack) { return true; }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); itemHandler.invalidate(); }
    @Override public Component getDisplayName() { return Component.translatable("block.mech.distiller"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new DistillerMenu(id, inv, this); }

    // IFluidHandler — оба танка доступны
    @Override public int getTanks() { return 2; }
    @Override public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? inputTank.getFluid() : outputTank.getFluid();
    }
    @Override public int getTankCapacity(int tank) {
        return tank == 0 ? inputTank.getCapacity() : outputTank.getCapacity();
    }
    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0; // только вход принимает
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