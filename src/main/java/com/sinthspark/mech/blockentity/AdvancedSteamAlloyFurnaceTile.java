package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.AdvancedSteamAlloyFurnaceBlock;
import com.sinthspark.mech.screen.AdvancedSteamAlloyFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Advanced-tier steam alloy furnace. See {@link AbstractSteamAlloyFurnaceTile} for the shared logic. */
public class AdvancedSteamAlloyFurnaceTile extends AbstractSteamAlloyFurnaceTile {

    private static final int TANK_CAPACITY = 16000;

    public AdvancedSteamAlloyFurnaceTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_STEAM_ALLOY_FURNACE.get(), pos, state, TANK_CAPACITY, 9.0f, 0.5f, false);
    }

    @Override
    protected BooleanProperty activeProperty() { return AdvancedSteamAlloyFurnaceBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "advanced_steam_alloy_furnace"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new AdvancedSteamAlloyFurnaceMenu(id, playerInv, this);
    }
}
