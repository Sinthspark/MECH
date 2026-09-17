package com.sinthspark.mech.screen;

import com.sinthspark.mech.blockentity.OpenHearthControllerTile;
import com.sinthspark.mech.registration.BlockRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OpenHearthMenu extends AbstractContainerMenu {

    public final OpenHearthControllerTile blockEntity;

    @SuppressWarnings("resource")
    public OpenHearthMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public OpenHearthMenu(int containerId, Inventory inventory, BlockEntity entity) {
        super(ModMenuTypes.OPEN_HEARTH_MENU.get(), containerId);
        this.blockEntity = (OpenHearthControllerTile) entity;

        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 35, 26) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return stack.getItem() == ItemRegistry.CAST_IRON_INGOT.get(); }
        });
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 1, 53, 26) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return stack.is(Items.IRON_INGOT) || stack.is(Items.IRON_NUGGET) || stack.is(Items.IRON_BLOCK); }
        });
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 2, 35, 44) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return stack.is(Items.IRON_ORE) || stack.is(Items.DEEPSLATE_IRON_ORE) || stack.is(Items.RAW_IRON); }
        });
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 3, 53, 44) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return stack.getItem() == BlockRegistry.LIMESTONE.get().asItem(); }
        });

        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 4, 107, 35) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 5, 125, 35) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 6) {
                if (!this.moveItemStackTo(itemstack1, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null
                && blockEntity.getLevel() != null
                && stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }
}