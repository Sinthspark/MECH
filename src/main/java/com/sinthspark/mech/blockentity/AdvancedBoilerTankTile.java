package com.sinthspark.mech.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class AdvancedBoilerTankTile extends AbstractBoilerTankTile {
    public AdvancedBoilerTankTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_BOILER_TANK.get(), pos, state, 16000);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, AdvancedBoilerTankTile tile) {
        AbstractBoilerTankTile.tick(level, pos, tile);
    }

    @Override protected int getMaxSteamPerTick() { return 12; }
    @Override protected float getExplosionPower() { return 6.0f; }
}
