package com.sinthspark.mech.api;

import net.minecraft.world.level.block.state.BlockState;

public interface IInsulatablePipe {
    boolean isInsulated();
    BlockState getBlockState();
    void setInsulated(boolean insulated);
}
