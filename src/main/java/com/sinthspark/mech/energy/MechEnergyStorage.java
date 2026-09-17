package com.sinthspark.mech.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.IEnergyStorage;

public class MechEnergyStorage implements IEnergyStorage {

    private int energy;
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;

    public MechEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = 0;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        if (amount <= 0) return 0;
        int received = Math.min(amount, Math.min(maxReceive, capacity - energy));
        if (!simulate) energy += received;
        return received;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        if (amount <= 0) return 0;
        int extracted = Math.min(amount, Math.min(maxExtract, energy));
        if (!simulate) energy -= extracted;
        return extracted;
    }

    @Override
    public int getEnergyStored() { return energy; }

    @Override
    public int getMaxEnergyStored() { return capacity; }

    @Override
    public boolean canExtract() { return maxExtract > 0 && energy > 0; }

    @Override
    public boolean canReceive() { return maxReceive > 0 && energy < capacity; }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, capacity));
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putInt("energy", energy);
        return tag;
    }

    public void readFromNBT(CompoundTag tag) {
        energy = Math.max(0, Math.min(tag.getInt("energy"), capacity));
    }
}