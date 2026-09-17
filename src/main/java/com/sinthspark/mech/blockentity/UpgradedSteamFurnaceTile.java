package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.UpgradedSteamFurnaceBlock;
import com.sinthspark.mech.screen.UpgradedSteamFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Upgraded-tier steam furnace. See {@link AbstractSteamFurnaceTile} for the shared logic. */
public class UpgradedSteamFurnaceTile extends AbstractSteamFurnaceTile {

    private static final int TANK_CAPACITY = 8000;
    private static final int STEAM_PER_TICK = 20;
    private static final int TOTAL_COOK_TICKS = 120;

    public UpgradedSteamFurnaceTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADED_STEAM_FURNACE.get(), pos, state, TANK_CAPACITY, STEAM_PER_TICK, TOTAL_COOK_TICKS);
    }

    @Override
    protected BooleanProperty activeProperty() { return UpgradedSteamFurnaceBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "upgraded_steam_furnace"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new UpgradedSteamFurnaceMenu(id, playerInv, this);
    }
}
