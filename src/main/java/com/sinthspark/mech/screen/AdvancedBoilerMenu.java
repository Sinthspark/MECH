package com.sinthspark.mech.screen;

import com.sinthspark.mech.blockentity.AdvancedBoilerTankTile;
import com.sinthspark.mech.blockentity.AdvancedFireboxTile;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class AdvancedBoilerMenu extends AbstractContainerMenu {

    public final AdvancedFireboxTile firebox;
    public final AdvancedBoilerTankTile boiler;

    public AdvancedBoilerMenu(int containerId, Inventory inventory, BlockPos pos, Level level) {
        super(ModMenuTypes.ADVANCED_BOILER_MENU.get(), containerId);

        this.firebox = level.getBlockEntity(pos) instanceof AdvancedFireboxTile ft ? ft : null;
        this.boiler = level.getBlockEntity(pos.above()) instanceof AdvancedBoilerTankTile bt ? bt : null;

        if (this.firebox != null) {
            this.firebox.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                this.addSlot(new SlotItemHandler(handler, 0, 26, 47) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return stack.getItem() == Items.COAL
                                || stack.getItem() == Items.CHARCOAL
                                || stack.getItem() == ItemRegistry.COKE.get();
                    }
                });
                this.addSlot(new SlotItemHandler(handler, 1, 26, 21) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return stack.getItem() == Items.BUCKET
                                || stack.getItem() == Items.WATER_BUCKET
                                || stack.getItem() == FluidRegistry.CREOSOTE.bucket.get();
                    }
                });
            });
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    public FluidStack getWaterFluid() {
        return boiler != null ? boiler.getWaterTank().getFluid() : FluidStack.EMPTY;
    }

    public FluidStack getSteamFluid() {
        return boiler != null ? boiler.getSteamTank().getFluid() : FluidStack.EMPTY;
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
                if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
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
        return firebox != null && firebox.getLevel() != null
                && stillValid(ContainerLevelAccess.create(firebox.getLevel(), firebox.getBlockPos()),
                player, firebox.getBlockState().getBlock());
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (boiler == null) {
            return false;
        }

        FluidStack tankFluid = boiler.getWaterTank().getFluid();
        ItemStack carried = getCarried();

        // id 0: Слить жидкость (ведро)
        if (id == 0 && carried.getItem() == Items.BUCKET && tankFluid.getAmount() >= 1000) {
            if (tankFluid.getFluid() == Fluids.WATER) {
                boiler.getWaterTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                if (!player.isCreative()) {
                    carried.shrink(1);
                    player.addItem(new ItemStack(Items.WATER_BUCKET));
                }
                return true;
            } else if (tankFluid.getFluid() == FluidRegistry.CREOSOTE.source.get()) {
                boiler.getWaterTank().drain(1000, IFluidHandler.FluidAction.EXECUTE);
                if (!player.isCreative()) {
                    carried.shrink(1);
                    player.addItem(new ItemStack(FluidRegistry.CREOSOTE.bucket.get()));
                }
                return true;
            }
        }

        // id 1: Залить воду
        if (id == 1 && carried.getItem() == Items.WATER_BUCKET) {
            int filled = boiler.getWaterTank().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (filled == 1000) {
                if (!player.isCreative()) {
                    carried.shrink(1);
                    player.addItem(new ItemStack(Items.BUCKET));
                }
                return true;
            }
        }

        // id 2: Залить креозот
        if (id == 2 && carried.getItem() == FluidRegistry.CREOSOTE.bucket.get()) {
            int filled = boiler.getWaterTank().fill(new FluidStack(FluidRegistry.CREOSOTE.source.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
            if (filled == 1000) {
                if (!player.isCreative()) {
                    carried.shrink(1);
                    player.addItem(new ItemStack(Items.BUCKET));
                }
                return true;
            }
        }

        return false;
    }
}
