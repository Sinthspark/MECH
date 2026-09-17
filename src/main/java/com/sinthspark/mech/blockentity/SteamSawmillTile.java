package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.SteamSawmillBlock;
import com.sinthspark.mech.screen.SteamSawmillMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Base-tier steam sawmill. See {@link AbstractSteamSawmillTile} for the shared logic. */
public class SteamSawmillTile extends AbstractSteamSawmillTile {

    private static final int TANK_CAPACITY = 4000;
    private static final int STEAM_PER_TICK = 5;
    private static final float SPEED_MULTIPLIER = 1.5f;

    public SteamSawmillTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_SAWMILL.get(), pos, state, TANK_CAPACITY, STEAM_PER_TICK, SPEED_MULTIPLIER);
    }

    @Override
    protected BooleanProperty activeProperty() { return SteamSawmillBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "steam_sawmill"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new SteamSawmillMenu(id, inv, this);
    }
}
