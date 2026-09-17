package com.sinthspark.mech.api;

import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IFluidPipe extends IFluidHandler {
    int getFluidAmount();
    int getCapacity();
}