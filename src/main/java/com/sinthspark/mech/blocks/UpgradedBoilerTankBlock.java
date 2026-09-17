package com.sinthspark.mech.blocks;

import com.sinthspark.mech.blockentity.UpgradedBoilerTankTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradedBoilerTankBlock extends Block implements EntityBlock {

    public UpgradedBoilerTankBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new UpgradedBoilerTankTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof UpgradedBoilerTankTile tile) {
                UpgradedBoilerTankTile.tick(level1, pos, tile);
            }
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof UpgradedBoilerTankTile boiler && boiler.getTemperature() >= 150) {
                level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        3.0f, true, Level.ExplosionInteraction.TNT);

                double radius = 5.0;
                List<Player> players = level.getEntitiesOfClass(Player.class,
                        new AABB(pos).inflate(radius));

                for (Player player : players) {
                    double dist = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    if (dist < radius * radius) {
                        player.hurt(level.damageSources().explosion(null), 5.0f);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}