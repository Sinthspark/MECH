package com.sinthspark.mech.screen;

import com.sinthspark.mech.blockentity.OilPressTile;
import com.sinthspark.mech.registration.FluidRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OilPressMenu extends AbstractContainerMenu {

    public final OilPressTile blockEntity;

    @SuppressWarnings("resource")
    public OilPressMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public OilPressMenu(int containerId, Inventory inventory, BlockEntity entity) {
        super(ModMenuTypes.OIL_PRESS_MENU.get(), containerId);
        this.blockEntity = (OilPressTile) entity;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler ->
                this.addSlot(new SlotItemHandler(handler, 0, 52, 35)
                ));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    public FluidStack getFluid() {
        return blockEntity.getFluidTank().getFluid();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 1 && index < 37) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
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

    public OilPressTile getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        FluidStack fluid = blockEntity.getFluidTank().getFluid();
        ItemStack carried = getCarried();

        if (id == 0 && carried.getItem() == Items.BUCKET && fluid.getAmount() >= 1000) {
            blockEntity.getFluidTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
            carried.shrink(1);
            player.addItem(new ItemStack(FluidRegistry.RAPESEED_OIL.bucket.get()));
            return true;
        }
        return false;
    }
}