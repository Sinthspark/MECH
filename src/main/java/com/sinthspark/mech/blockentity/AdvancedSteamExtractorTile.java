package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.AdvancedSteamExtractorBlock;
import com.sinthspark.mech.screen.AdvancedSteamExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Advanced-tier steam extractor. See {@link AbstractSteamExtractorTile} for the shared logic. */
public class AdvancedSteamExtractorTile extends AbstractSteamExtractorTile {
    private static final int STEAM_TANK_CAPACITY = 16000;
    private static final int OUTPUT_TANK_CAPACITY = 16000;

    public AdvancedSteamExtractorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_STEAM_EXTRACTOR.get(), pos, state, STEAM_TANK_CAPACITY, OUTPUT_TANK_CAPACITY, 9.0f, 0.5f);
    }

    @Override protected BooleanProperty activeProperty() { return AdvancedSteamExtractorBlock.ACTIVE; }
    @Override protected String translationKey() { return "advanced_steam_extractor"; }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new AdvancedSteamExtractorMenu(id, inv, this);
    }
}
