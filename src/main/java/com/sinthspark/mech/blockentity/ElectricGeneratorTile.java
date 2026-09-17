package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.api.IEnergyHandler;
import com.sinthspark.mech.blocks.ElectricGeneratorBlock;
import com.sinthspark.mech.blocks.SteamTurbineBlock;
import com.sinthspark.mech.energy.MechEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectricGeneratorTile extends BlockEntity implements IEnergyHandler {

    private static final int ENERGY_CAPACITY = 3200;
    private static final int MAX_OUTPUT = 32;

    private final MechEnergyStorage energyStorage = new MechEnergyStorage(ENERGY_CAPACITY, 32, MAX_OUTPUT);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    public ElectricGeneratorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_GENERATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricGeneratorTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.getBlockState().getValue(ElectricGeneratorBlock.ACTIVE);
        boolean isActive = false;

        Direction back = state.getValue(ElectricGeneratorBlock.FACING).getOpposite();
        BlockPos turbinePos = pos.relative(back);
        BlockEntity be = level.getBlockEntity(turbinePos);

        if (be instanceof SteamTurbineTile turbine && turbine.isRunning()) {
            Direction turbineFacing = turbine.getBlockState().getValue(SteamTurbineBlock.FACING);
            if (turbineFacing == state.getValue(ElectricGeneratorBlock.FACING)) {
                int rpm = turbine.rpm;
                int generated = rpm / 112;
                int received = tile.energyStorage.receiveEnergy(generated, false);
                if (received > 0) {
                    isActive = true;
                }
            }
        }

        if (tile.energyStorage.getEnergyStored() > 0) {
            Direction front = state.getValue(ElectricGeneratorBlock.FACING);
            BlockPos neighborPos = pos.relative(front);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor != null) {
                neighbor.getCapability(ForgeCapabilities.ENERGY, front.getOpposite()).ifPresent(handler -> {
                    int extracted = tile.energyStorage.extractEnergy(MAX_OUTPUT, true);
                    if (extracted > 0) {
                        int accepted = handler.receiveEnergy(extracted, false);
                        tile.energyStorage.extractEnergy(accepted, false);
                    }
                });
            }
        }

        if (wasActive != isActive) {
            level.setBlock(pos, state.setValue(ElectricGeneratorBlock.ACTIVE, isActive), 3);
        }

        setChanged(level, pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            Direction front = getBlockState().getValue(ElectricGeneratorBlock.FACING);
            if (side == front) {
                return energyHandler.cast();
            }
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
    }
}