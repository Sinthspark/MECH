package com.sinthspark.mech.items;

import com.sinthspark.mech.api.IWire;
import com.sinthspark.mech.blocks.PartType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class WireCuttersItem extends Item {

    public WireCuttersItem(int durability) {
        super(new Properties()
                .durability(durability)
                .setNoRepair()
        );
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide || player == null) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof IWire wire)) {
            return InteractionResult.PASS;
        }

        PartType part = wire.getPartType(state, level, pos, player);
        boolean isCenter = (part == PartType.CENTER);
        boolean isConnection = (part != PartType.CENTER && part != PartType.NONE);

        // Shift + клик по соединению — авто-режим
        if (isConnection && player.isShiftKeyDown()) {
            BooleanProperty manualProp = wire.getManualProperty(part);
            if (manualProp != null) {
                state = state.setValue(manualProp, false);
                level.setBlock(pos, state, 3);
                wire.updateConnections(state, level, pos);
                level.sendBlockUpdated(pos, state, state, 3);
                applyToolUse(level, pos, player, stack, context.getHand());
                return InteractionResult.SUCCESS;
            }
        }

        // Клик по центру — подключить
        if (isCenter) {
            Direction face = context.getClickedFace();
            BooleanProperty connProp = wire.getConnectionProperty(face);
            BooleanProperty manualProp = wire.getManualProperty(face);

            if (connProp != null && manualProp != null) {
                state = state.setValue(connProp, true).setValue(manualProp, false);
                level.setBlock(pos, state, 3);
                updateNeighborWireConnect(level, pos.relative(face), face.getOpposite());
                applyToolUse(level, pos, player, stack, context.getHand());
                return InteractionResult.SUCCESS;
            }
        }

        // Клик по соединению — отключить (manual)
        if (isConnection) {
            Direction dir = getDirectionForPart(part);
            BooleanProperty connProp = wire.getConnectionProperty(dir);
            BooleanProperty manualProp = wire.getManualProperty(part);

            if (connProp != null && manualProp != null) {
                state = state.setValue(connProp, false).setValue(manualProp, true);
                level.setBlock(pos, state, 3);
                level.sendBlockUpdated(pos, state, state, 3);
                updateNeighborWireDisconnect(level, pos.relative(dir), dir.getOpposite());
                applyToolUse(level, pos, player, stack, context.getHand());
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private void updateNeighborWireConnect(@NotNull Level level, @NotNull BlockPos pos, @NotNull Direction oppositeFace) {
        BlockState neighborState = level.getBlockState(pos);
        if (neighborState.getBlock() instanceof IWire neighborWire) {
            BooleanProperty connProp = neighborWire.getConnectionProperty(oppositeFace);
            BooleanProperty manualProp = neighborWire.getManualProperty(oppositeFace);
            if (connProp != null && manualProp != null) {
                neighborState = neighborState.setValue(connProp, true).setValue(manualProp, false);
                level.setBlock(pos, neighborState, 3);
            }
        }
    }

    private void updateNeighborWireDisconnect(@NotNull Level level, @NotNull BlockPos pos, @NotNull Direction oppositeFace) {
        BlockState neighborState = level.getBlockState(pos);
        if (neighborState.getBlock() instanceof IWire neighborWire) {
            BooleanProperty connProp = neighborWire.getConnectionProperty(oppositeFace);
            BooleanProperty manualProp = neighborWire.getManualProperty(oppositeFace);
            if (connProp != null && manualProp != null) {
                neighborState = neighborState.setValue(connProp, false).setValue(manualProp, true);
                level.setBlock(pos, neighborState, 3);
                level.sendBlockUpdated(pos, neighborState, neighborState, 3);
            }
        }
    }

    private void applyToolUse(@NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
                              @NotNull ItemStack stack, @NotNull InteractionHand hand) {
        level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.6F, 1.3F);
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        player.swing(hand);
    }

    @NotNull
    private Direction getDirectionForPart(@NotNull PartType part) {
        return switch (part) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default -> throw new IllegalStateException("Unexpected part type: " + part);
        };
    }

    @Override
    public boolean hasCraftingRemainingItem(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setDamageValue(copy.getDamageValue() + 1);
        if (copy.getDamageValue() >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return copy;
    }
}