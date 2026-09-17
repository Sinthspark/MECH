package com.sinthspark.mech.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.NotNull;

public class SolidifyingFluidBlock extends LiquidBlock {
    private final Block solidBlock;
    private final int solidifyTime;

    @SuppressWarnings("deprecation")
    public SolidifyingFluidBlock(FlowingFluid fluid, Block solidBlock, int solidifyTime, Properties properties) {
        super(fluid, properties);
        this.solidBlock = solidBlock;
        this.solidifyTime = solidifyTime;
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && isSource(state)) {
            scheduleSolidify(level, pos);
        }
    }

    private void scheduleSolidify(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            float biomeFactor = getBiomeFactor(level, pos);
            float heightFactor = getHeightFactor(pos.getY());
            int ticks = (int) (solidifyTime * biomeFactor * heightFactor);
            serverLevel.scheduleTick(pos, this, Math.max(ticks, 100));
        }
    }

    private float getHeightFactor(int y) {
        if (y >= 256) return 0.6f;
        if (y >= 192) return 0.7f;
        if (y >= 128) return 0.8f;
        if (y >= 64) return 0.9f;
        return 1.0f;
    }

    private float getBiomeFactor(Level level, BlockPos pos) {
        Biome biome = level.getBiome(pos).value();
        float temp = biome.getBaseTemperature();

        if (temp <= 0.0f) return 0.6f;
        if (temp <= 0.2f) return 0.8f;
        if (temp <= 0.5f) return 1.0f;
        if (temp <= 0.8f) return 1.2f;
        return 1.4f;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (isSource(state) && solidBlock != null) {
            level.setBlock(pos, solidBlock.defaultBlockState(), 3);
        }
    }

    private boolean isSource(BlockState state) {
        return state.getValue(LiquidBlock.LEVEL) == 0;
    }
}