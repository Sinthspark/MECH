package com.sinthspark.mech.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import com.sinthspark.mech.blocks.PartType;

public interface IWire {
    PartType getPartType(BlockState state, BlockGetter level, BlockPos pos, Player player);
    BooleanProperty getConnectionProperty(Direction direction);
    BooleanProperty getManualProperty(Direction direction);
    void updateConnections(BlockState state, Level level, BlockPos pos);
    BooleanProperty getManualProperty(PartType part);
}
