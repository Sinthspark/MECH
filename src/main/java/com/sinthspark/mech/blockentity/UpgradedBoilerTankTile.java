package com.sinthspark.mech.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class UpgradedBoilerTankTile extends AbstractBoilerTankTile {
    public UpgradedBoilerTankTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADED_BOILER_TANK.get(), pos, state, 8000);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, UpgradedBoilerTankTile tile) {
        AbstractBoilerTankTile.tick(level, pos, tile);
    }

    @Override protected int getMaxSteamPerTick() { return 8; }
    @Override protected float getExplosionPower() { return 4.0f; }
}
