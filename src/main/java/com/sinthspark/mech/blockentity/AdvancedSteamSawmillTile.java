package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.AdvancedSteamSawmillBlock;
import com.sinthspark.mech.screen.AdvancedSteamSawmillMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Advanced-tier steam sawmill. See {@link AbstractSteamSawmillTile} for the shared logic. */
public class AdvancedSteamSawmillTile extends AbstractSteamSawmillTile {

    private static final int TANK_CAPACITY = 16000;
    private static final int STEAM_PER_TICK = 45;
    private static final float SPEED_MULTIPLIER = 3.0f;

    public AdvancedSteamSawmillTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_STEAM_SAWMILL.get(), pos, state, TANK_CAPACITY, STEAM_PER_TICK, SPEED_MULTIPLIER);
    }

    @Override
    protected BooleanProperty activeProperty() { return AdvancedSteamSawmillBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "advanced_steam_sawmill"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new AdvancedSteamSawmillMenu(id, inv, this);
    }
}
