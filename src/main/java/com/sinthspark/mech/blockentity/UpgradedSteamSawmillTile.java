package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.UpgradedSteamSawmillBlock;
import com.sinthspark.mech.screen.UpgradedSteamSawmillMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Upgraded-tier steam sawmill. See {@link AbstractSteamSawmillTile} for the shared logic. */
public class UpgradedSteamSawmillTile extends AbstractSteamSawmillTile {

    private static final int TANK_CAPACITY = 8000;
    private static final int STEAM_PER_TICK = 10;
    private static final float SPEED_MULTIPLIER = 2f;

    public UpgradedSteamSawmillTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADED_STEAM_SAWMILL.get(), pos, state, TANK_CAPACITY, STEAM_PER_TICK, SPEED_MULTIPLIER);
    }

    @Override
    protected BooleanProperty activeProperty() { return UpgradedSteamSawmillBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "upgraded_steam_sawmill"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new UpgradedSteamSawmillMenu(id, inv, this);
    }
}
