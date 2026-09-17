package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.SteamExtractorBlock;
import com.sinthspark.mech.screen.SteamExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Base-tier steam extractor. See {@link AbstractSteamExtractorTile} for the shared logic. */
public class SteamExtractorTile extends AbstractSteamExtractorTile {
    private static final int STEAM_TANK_CAPACITY = 4000;
    private static final int OUTPUT_TANK_CAPACITY = 4000;

    public SteamExtractorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_EXTRACTOR.get(), pos, state, STEAM_TANK_CAPACITY, OUTPUT_TANK_CAPACITY, 1.0f, 1.0f);
    }

    @Override protected BooleanProperty activeProperty() { return SteamExtractorBlock.ACTIVE; }
    @Override protected String translationKey() { return "steam_extractor"; }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new SteamExtractorMenu(id, inv, this);
    }
}
