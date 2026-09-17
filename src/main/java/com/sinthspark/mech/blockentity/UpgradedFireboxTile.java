package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.screen.UpgradedBoilerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class UpgradedFireboxTile extends AbstractFireboxTile {
    public UpgradedFireboxTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADED_FIREBOX.get(), pos, state, 5, 1200);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, UpgradedFireboxTile tile) {
        AbstractFireboxTile.tick(level, pos, state, tile);
    }

    @Override protected Component getFireboxDisplayName() { return Component.literal("block.mech.firebox"); }
    @Override protected AbstractContainerMenu createFireboxMenu(int containerId, Inventory inventory, Player player) {
        if (level == null) return null;
        return new UpgradedBoilerMenu(containerId, inventory, getBlockPos(), level);
    }
}
