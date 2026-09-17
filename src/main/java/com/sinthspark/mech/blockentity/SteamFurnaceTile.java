package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.SteamFurnaceBlock;
import com.sinthspark.mech.screen.SteamFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Base-tier steam furnace. See {@link AbstractSteamFurnaceTile} for the shared logic. */
public class SteamFurnaceTile extends AbstractSteamFurnaceTile {

    private static final int TANK_CAPACITY = 4000;
    private static final int STEAM_PER_TICK = 5;
    private static final int TOTAL_COOK_TICKS = 160;

    public SteamFurnaceTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_FURNACE.get(), pos, state, TANK_CAPACITY, STEAM_PER_TICK, TOTAL_COOK_TICKS);
    }

    @Override
    protected BooleanProperty activeProperty() { return SteamFurnaceBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "steam_furnace"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new SteamFurnaceMenu(id, playerInv, this);
    }
}
