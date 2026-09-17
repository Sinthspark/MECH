package com.sinthspark.mech.blocks;

import com.sinthspark.mech.api.IHeatedBlock;
import com.sinthspark.mech.blockentity.AdvancedBoilerTankTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedBoilerTankBlock extends Block implements EntityBlock {

    public AdvancedBoilerTankBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AdvancedBoilerTankTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof AdvancedBoilerTankTile tile) {
                AdvancedBoilerTankTile.tick(level1, pos, tile);
            }
        };
    }

      @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = player.level();

        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);

         if (be instanceof IHeatedBlock heated && heated.getTemperature() >= 100) {
            player.hurt(level.damageSources().hotFloor(), 2.0f);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        // Взрыв обрабатывается в BoilerBreakHandler
        super.onRemove(state, level, pos, newState, isMoving);
    }
}