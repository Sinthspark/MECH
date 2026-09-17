package com.sinthspark.mech.screen;

import com.sinthspark.mech.blockentity.DistillerTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class DistillerMenu extends AbstractContainerMenu {

    public final DistillerTile blockEntity;

    @SuppressWarnings("resource")
    public DistillerMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public DistillerMenu(int containerId, Inventory inventory, BlockEntity entity) {
        super(ModMenuTypes.DISTILLER_MENU.get(), containerId);
        this.blockEntity = (DistillerTile) entity;

        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 52, 35));
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 1, 116, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
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
            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
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
    public boolean clickMenuButton(Player player, int id) {
        ItemStack carried = getCarried();

        if (id == 0 && carried.getItem() == Items.BUCKET) {
            // Забрать из входного танка
            FluidStack fluid = blockEntity.getInputTank().getFluid();
            if (fluid.getAmount() >= 1000) {
                blockEntity.getInputTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                carried.shrink(1);
                player.addItem(new ItemStack(fluid.getFluid().getBucket()));
                return true;
            }
        }

        if (id == 1 && carried.getItem() == Items.WATER_BUCKET) {
            // Залить воду во входной танк
            int filled = blockEntity.getInputTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (filled == 1000) {
                carried.shrink(1);
                player.addItem(new ItemStack(Items.BUCKET));
                return true;
            }
        }

        if (id == 2 && carried.getItem() == Items.BUCKET) {
            // Забрать из выходного танка
            FluidStack fluid = blockEntity.getOutputTank().getFluid();
            if (fluid.getAmount() >= 1000) {
                blockEntity.getOutputTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                carried.shrink(1);
                player.addItem(new ItemStack(fluid.getFluid().getBucket()));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null
                && blockEntity.getLevel() != null
                && stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }
}
