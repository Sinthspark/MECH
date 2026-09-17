package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.SteamAlloyFurnaceBlock;
import com.sinthspark.mech.screen.SteamAlloyFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Base-tier steam alloy furnace. See {@link AbstractSteamAlloyFurnaceTile} for the shared logic. */
public class SteamAlloyFurnaceTile extends AbstractSteamAlloyFurnaceTile {

    private static final int TANK_CAPACITY = 4000;

    public SteamAlloyFurnaceTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_ALLOY_FURNACE.get(), pos, state, TANK_CAPACITY, 1.0f, 1.0f, true);
    }

    @Override
    protected BooleanProperty activeProperty() { return SteamAlloyFurnaceBlock.ACTIVE; }

    @Override
    protected String translationKey() { return "steam_alloy_furnace"; }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new SteamAlloyFurnaceMenu(id, playerInv, this);
    }
}
