package com.sinthspark.mech.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import com.sinthspark.mech.screen.BoilerMenu;

public class FireboxTile extends AbstractFireboxTile {
    public FireboxTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIREBOX.get(), pos, state, 5, 800);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, FireboxTile tile) {
        AbstractFireboxTile.tick(level, pos, state, tile);
    }

    @Override protected Component getFireboxDisplayName() { return Component.translatable("gui.mech.firebox"); }
    @Override protected AbstractContainerMenu createFireboxMenu(int containerId, Inventory inventory, Player player) {
        if (level == null) return null;
        return new BoilerMenu(containerId, inventory, getBlockPos(), level);
    }
}
