package com.sinthspark.mech.items;

import com.sinthspark.mech.api.IPipe;
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

public class WrenchItem extends Item {

    public WrenchItem(int durability) {
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
        if (!(state.getBlock() instanceof IPipe pipe)) {
            return InteractionResult.PASS;
        }

        PartType part = pipe.getPartType(state, level, pos, player);
        boolean isCenter = (part == PartType.CENTER);
        boolean isConnection = (part != PartType.CENTER && part != PartType.NONE);

        // Shift + клик по соединению — авто-режим
        if (isConnection && player.isShiftKeyDown()) {
            BooleanProperty manualProp = pipe.getManualProperty(part);
            if (manualProp != null) {
                state = state.setValue(manualProp, false);
                level.setBlock(pos, state, 3);
                pipe.updateConnections(state, level, pos);
                level.sendBlockUpdated(pos, state, state, 3);
                player.displayClientMessage(Component.literal("Auto mode for this side restored"), true);
                applyToolUse(level, pos, player, stack, context.getHand());
                return InteractionResult.SUCCESS;
            }
        }

        // Клик по центру — создать соединение
        if (isCenter) {
            Direction face = context.getClickedFace();
            BooleanProperty connProp = pipe.getConnectionProperty(face);
            BooleanProperty manualProp = pipe.getManualProperty(face);

            if (connProp != null && manualProp != null) {
                BlockPos neighborPos = pos.relative(face);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.getBlock() instanceof IPipe neighborPipe) {
                    BooleanProperty neighborConnProp = neighborPipe.getConnectionProperty(face.getOpposite());
                    BooleanProperty neighborManualProp = neighborPipe.getManualProperty(face.getOpposite());

                    if (neighborConnProp != null && neighborManualProp != null) {
                        state = state.setValue(connProp, true).setValue(manualProp, false);
                        level.setBlock(pos, state, 3);

                        neighborState = neighborState.setValue(neighborConnProp, true).setValue(neighborManualProp, false);
                        level.setBlock(neighborPos, neighborState, 3);
                        level.sendBlockUpdated(neighborPos, neighborState, neighborState, 3);

                        applyToolUse(level, pos, player, stack, context.getHand());
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    player.displayClientMessage(Component.literal("No pipe on this side!"), true);
                }
            }
        }

        // Клик по соединению — переключить ручной режим
        if (isConnection) {
            Direction dir = getDirectionForPart(part);
            BooleanProperty connProp = pipe.getConnectionProperty(dir);
            BooleanProperty manualProp = pipe.getManualProperty(part);

            if (connProp != null && manualProp != null) {
                boolean isManual = state.getValue(manualProp);

                if (isManual) {
                    // Убрать ручное соединение
                    state = state.setValue(connProp, false);
                    level.setBlock(pos, state, 3);
                    level.sendBlockUpdated(pos, state, state, 3);
                    updateNeighborPipe(level, pos.relative(dir), dir.getOpposite(), false);
                    player.displayClientMessage(Component.literal("Manual connection removed"), true);
                } else {
                    // Включить ручной режим (отключить авто-соединение)
                    state = state.setValue(connProp, false).setValue(manualProp, true);
                    level.setBlock(pos, state, 3);
                    level.sendBlockUpdated(pos, state, state, 3);
                    updateNeighborPipe(level, pos.relative(dir), dir.getOpposite(), true);
                    player.displayClientMessage(Component.literal("Connection disabled (manual mode)"), true);
                }

                applyToolUse(level, pos, player, stack, context.getHand());
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private void updateNeighborPipe(@NotNull Level level, @NotNull BlockPos pos, @NotNull Direction oppositeFace,
                                    boolean manual) {
        BlockState neighborState = level.getBlockState(pos);
        if (neighborState.getBlock() instanceof IPipe neighborPipe) {
            BooleanProperty connProp = neighborPipe.getConnectionProperty(oppositeFace);
            BooleanProperty manualProp = neighborPipe.getManualProperty(oppositeFace);
            if (connProp != null && manualProp != null) {
                neighborState = neighborState.setValue(connProp, false).setValue(manualProp, manual);
                level.setBlock(pos, neighborState, 3);
                level.sendBlockUpdated(pos, neighborState, neighborState, 3);
            }
        }
    }

    private void applyToolUse(@NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
                              @NotNull ItemStack stack, @NotNull InteractionHand hand) {
        level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.7F, 1.5F);
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