package com.sinthspark.mech.api;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** Common contract used by fireboxes and boiler tiers without exposing boiler storage internals. */
public interface IBoilerTank {
    int fillWater(FluidStack stack, IFluidHandler.FluidAction action);
    boolean hasWater();
}
