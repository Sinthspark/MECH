package com.sinthspark.mech.screen;

import com.sinthspark.mech.blockentity.ElectricCompressorTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ElectricCompressorMenu extends AbstractContainerMenu {

    public final ElectricCompressorTile blockEntity;

    @SuppressWarnings("resource")
    public ElectricCompressorMenu(int id, Inventory inv, FriendlyByteBuf extra) {
        this(id, inv, inv.player.level().getBlockEntity(extra.readBlockPos()));
    }

    public ElectricCompressorMenu(int id, Inventory inv, BlockEntity entity) {
        super(ModMenuTypes.ELECTRIC_COMPRESSOR_MENU.get(), id);
        this.blockEntity = (ElectricCompressorTile) entity;

        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 52, 35));
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 1, 116, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack s = slot.getItem();
            stack = s.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(s, 2, 38, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(s, 0, 1, false)) return ItemStack.EMPTY;
            }
            if (s.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null
                && blockEntity.getLevel() != null
                && stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }
}