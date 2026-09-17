package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.AdvancedSteamFurnaceBlock;
import com.sinthspark.mech.screen.AdvancedSteamFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Advanced-tier steam furnace. See {@link AbstractSteamFurnaceTile} for the shared logic. */
public class AdvancedSteamFurnaceTile extends AbstractSteamFurnaceTile {

    private static final int TANK_CAPACITY = 16000;
    private static final int STEAM_PER_TICK = 45;
    private static final int TOTAL_COOK_TICKS = 80;

    public AdvancedSteamFurnaceTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_STEAM_FURNACE.get(), pos, state, TANK_CAPACITY, STEAM_PER_TICK, TOTAL_COOK_TICKS);
    }

    @Override
    protected BooleanProperty activeProperty() { return AdvancedSteamFurnaceBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "advanced_steam_furnace"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new AdvancedSteamFurnaceMenu(id, playerInv, this);
    }
}
