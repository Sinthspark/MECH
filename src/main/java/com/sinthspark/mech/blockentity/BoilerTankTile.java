package com.sinthspark.mech.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BoilerTankTile extends AbstractBoilerTankTile {
    public BoilerTankTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOILER_TANK.get(), pos, state, 4000);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BoilerTankTile tile) {
        AbstractBoilerTankTile.tick(level, pos, tile);
    }

    @Override protected int getMaxSteamPerTick() { return 4; }
    @Override protected float getExplosionPower() { return 2.0f; }
    @Override protected boolean damagesPlayersOnExplosion() { return true; }
}
