package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.UpgradedSteamExtractorBlock;
import com.sinthspark.mech.screen.UpgradedSteamExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Upgraded-tier steam extractor. See {@link AbstractSteamExtractorTile} for the shared logic. */
public class UpgradedSteamExtractorTile extends AbstractSteamExtractorTile {
    private static final int STEAM_TANK_CAPACITY = 8000;
    private static final int OUTPUT_TANK_CAPACITY = 8000;

    public UpgradedSteamExtractorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADED_STEAM_EXTRACTOR.get(), pos, state, STEAM_TANK_CAPACITY, OUTPUT_TANK_CAPACITY, 2.0f, 0.75f);
    }

    @Override protected BooleanProperty activeProperty() { return UpgradedSteamExtractorBlock.ACTIVE; }
    @Override protected String translationKey() { return "upgraded_steam_extractor"; }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new UpgradedSteamExtractorMenu(id, inv, this);
    }
}
