package com.sinthspark.mech.screen;

import com.sinthspark.mech.blockentity.UpgradedSteamFurnaceTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class UpgradedSteamFurnaceMenu extends AbstractContainerMenu {

    private final UpgradedSteamFurnaceTile tile;
    public final UpgradedSteamFurnaceTile blockEntity;

    public UpgradedSteamFurnaceMenu(int id, Inventory playerInv, BlockEntity entity) {
        super(ModMenuTypes.UPGRADED_STEAM_FURNACE_MENU.get(), id);
        this.tile = (UpgradedSteamFurnaceTile) entity;
        this.blockEntity = tile;

        this.addSlot(new SlotItemHandler(tile.getInventory(), 0, 52, 35));
        this.addSlot(new SlotItemHandler(tile.getInventory(), 1, 116, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @SuppressWarnings("resource")
    public UpgradedSteamFurnaceMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return tile != null && tile.getBlockPos().closerToCenterThan(player.position(), 8);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }

    public UpgradedSteamFurnaceTile getTile() {
        return tile;
    }
}