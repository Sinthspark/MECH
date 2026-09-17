package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.screen.AdvancedBoilerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedFireboxTile extends AbstractFireboxTile {
    public AdvancedFireboxTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_FIREBOX.get(), pos, state, 3, 1600);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, AdvancedFireboxTile tile) {
        AbstractFireboxTile.tick(level, pos, state, tile);
    }

    @Override protected Component getFireboxDisplayName() { return Component.literal("block.mech.firebox"); }
    @Override protected AbstractContainerMenu createFireboxMenu(int containerId, Inventory inventory, Player player) {
        if (level == null) return null;
        return new AdvancedBoilerMenu(containerId, inventory, getBlockPos(), level);
    }
}
