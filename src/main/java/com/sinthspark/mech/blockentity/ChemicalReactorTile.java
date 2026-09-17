package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.ChemicalReactorBlock;
import com.sinthspark.mech.energy.MechEnergyStorage;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.screen.ChemicalReactorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChemicalReactorTile extends BlockEntity implements MenuProvider, IFluidHandler {

    private static final int ENERGY_CAPACITY = 3200;
    private static final int ENERGY_PER_TICK = 16;
    private static final int TANK_CAPACITY = 8000;
    private static final int TOTAL_WORK_TICKS = 200;

    public int progress = 0;
    public int maxProgress = TOTAL_WORK_TICKS;

    private final MechEnergyStorage energyStorage = new MechEnergyStorage(ENERGY_CAPACITY, 32, 32);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    private final FluidTank inputTank1 = new FluidTank(TANK_CAPACITY);
    private final FluidTank inputTank2 = new FluidTank(TANK_CAPACITY);
    private final FluidTank outputTank = new FluidTank(TANK_CAPACITY);

    public ChemicalReactorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_REACTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalReactorTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.isActive();
        boolean active = false;

        FluidStack fluid1 = tile.inputTank1.getFluid();
        FluidStack fluid2 = tile.inputTank2.getFluid();

        if (!fluid1.isEmpty() && !fluid2.isEmpty() && tile.energyStorage.getEnergyStored() >= ENERGY_PER_TICK) {
            if (tile.outputTank.getFluidAmount() < tile.outputTank.getCapacity()) {
                FluidStack result = tile.getReactionResult(fluid1, fluid2);
                if (result != null && !result.isEmpty()) {
                    int needed = 100;
                    if (fluid1.getAmount() >= needed && fluid2.getAmount() >= needed) {
                        tile.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
                        tile.progress++;
                        active = true;

                        if (tile.progress >= TOTAL_WORK_TICKS) {
                            tile.inputTank1.drain(needed, IFluidHandler.FluidAction.EXECUTE);
                            tile.inputTank2.drain(needed, IFluidHandler.FluidAction.EXECUTE);
                            tile.outputTank.fill(new FluidStack(result.getFluid(), needed), IFluidHandler.FluidAction.EXECUTE);
                            tile.progress = 0;
                            active = false;
                        }
                    }
                }
            } else {
                tile.progress = 0;
            }
        } else {
            tile.progress = 0;
        }

        if (wasActive != active) {
            level.setBlock(pos, state.setValue(ChemicalReactorBlock.ACTIVE, active), 3);
        }

        setChanged(level, pos, state);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    @Nullable
    private FluidStack getReactionResult(FluidStack fluid1, FluidStack fluid2) {
        // Дистиллированная вода + сернистый газ = сернистая кислота
        if (isDistilledWater(fluid1) && isSulfurDioxide(fluid2) ||
                isDistilledWater(fluid2) && isSulfurDioxide(fluid1)) {
            return new FluidStack(FluidRegistry.SULFUROUS_ACID.source.get(), 80);
        }
        // Сернистая кислота + перекись водорода = серная кислота
        if (isSulfurousAcid(fluid1) && isHydrogenPeroxide(fluid2) ||
                isSulfurousAcid(fluid2) && isHydrogenPeroxide(fluid1)) {
            return new FluidStack(FluidRegistry.SULFURIC_ACID.source.get(), 80);
        }
        return null;
    }

    // Заглушки — заменить на реальные флюиды
    private boolean isDistilledWater(FluidStack fluid) { return false; }
    private boolean isSulfurDioxide(FluidStack fluid) { return false; }
    private boolean isSulfurousAcid(FluidStack fluid) { return false; }
    private boolean isHydrogenPeroxide(FluidStack fluid) { return false; }

    public boolean isActive() { return progress > 0; }
    public FluidTank getInputTank1() { return inputTank1; }
    public FluidTank getInputTank2() { return inputTank2; }
    public FluidTank getOutputTank() { return outputTank; }
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergy() { return ENERGY_CAPACITY; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return LazyOptional.of(() -> this).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); }
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech.chemical_reactor"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return new ChemicalReactorMenu(id, inv, this); }

    @Override public int getTanks() { return 3; }
    @Override public @NotNull FluidStack getFluidInTank(int tank) {
        return switch (tank) {
            case 0 -> inputTank1.getFluid();
            case 1 -> inputTank2.getFluid();
            case 2 -> outputTank.getFluid();
            default -> FluidStack.EMPTY;
        };
    }
    @Override public int getTankCapacity(int tank) { return TANK_CAPACITY; }
    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank < 2; // только входные танки принимают
    }
    @Override public int fill(FluidStack resource, FluidAction action) {
        if (inputTank1.getFluidAmount() <= inputTank2.getFluidAmount()) {
            return inputTank1.fill(resource, action);
        }
        return inputTank2.fill(resource, action);
    }
    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return outputTank.drain(resource, action);
    }
    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return outputTank.drain(maxDrain, action);
    }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
        tag.put("inputTank1", inputTank1.writeToNBT(new CompoundTag()));
        tag.put("inputTank2", inputTank2.writeToNBT(new CompoundTag()));
        tag.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.putInt("progress", progress);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
        inputTank1.readFromNBT(tag.getCompound("inputTank1"));
        inputTank2.readFromNBT(tag.getCompound("inputTank2"));
        outputTank.readFromNBT(tag.getCompound("outputTank"));
        progress = tag.getInt("progress");
    }
}
