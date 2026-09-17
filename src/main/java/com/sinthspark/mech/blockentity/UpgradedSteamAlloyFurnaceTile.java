package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.UpgradedSteamAlloyFurnaceBlock;
import com.sinthspark.mech.screen.UpgradedSteamAlloyFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Upgraded-tier steam alloy furnace. See {@link AbstractSteamAlloyFurnaceTile} for the shared logic. */
public class UpgradedSteamAlloyFurnaceTile extends AbstractSteamAlloyFurnaceTile {

    private static final int TANK_CAPACITY = 8000;

    public UpgradedSteamAlloyFurnaceTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADED_STEAM_ALLOY_FURNACE.get(), pos, state, TANK_CAPACITY, 2.0f, 0.75f, false);
    }

    @Override
    protected BooleanProperty activeProperty() { return UpgradedSteamAlloyFurnaceBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "upgraded_steam_alloy_furnace"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new UpgradedSteamAlloyFurnaceMenu(id, playerInv, this);
    }
}
