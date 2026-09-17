package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.energy.KineticNetworkManager;
import com.sinthspark.mech.registration.FluidRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.screen.OilPressMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OilPressTile extends BlockEntity implements MenuProvider {

    private final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == 0 && getStackInSlot(0).isEmpty()) {
                progress = 0;
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public float inputPower = 0;
    public float perTick = 0;
    public float minPower = 50.0f;
    public float maxPower = 80.0f;
    public float progress = 0;
    public int maxProgress = 1200;
    public static final float OIL_PRESS_EFFICIENCY = 0.85f;
    public static final float OIL_PRESS_FRICTION = 6.0f;
    public static final float MOMENT_OF_INERTIA = 0.5f;
    public static final float PI = 3.14159f;
    public float renderAngle = 0;
    public float rotationDirection = 1.0f;
    public float pistonOffset = 0;
    public boolean lubricated = false;
    public int lubricantTimer = 0;
    public static final float LUBRICANT_REDUCTION = 0.3f;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public OilPressTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OIL_PRESS.get(), pos, state);
    }

    public boolean isActive() { return progress > 0; }

    public float getFriction() {
        if (lubricated) return OIL_PRESS_FRICTION * (1 - LUBRICANT_REDUCTION);
        return OIL_PRESS_FRICTION;
    }

    public void applyLubricant() {
        lubricated = true;
        lubricantTimer = 6000;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OilPressTile tile) {
        if (level.isClientSide) return;

        // inputPower и perTick устанавливаются КИНЕТИЧЕСКОЙ СЕТЬЮ
        // Не обнуляем и не ищем соседей здесь!

        boolean hasValidPower = tile.inputPower >= tile.minPower &&
                tile.inputPower <= tile.maxPower;

        boolean canWork = hasValidPower &&
                !tile.itemHandler.getStackInSlot(0).isEmpty() &&
                tile.willOutputFit();

        if (canWork) {
            float speedMultiplier = Math.min(2.0f, tile.inputPower / 50.0f);
            tile.progress += speedMultiplier;
            tile.perTick = speedMultiplier;

            // Смазка
            if (tile.lubricated) {
                tile.lubricantTimer--;
                if (tile.lubricantTimer <= 0) {
                    tile.lubricated = false;
                }
            }

            tile.setChanged();
            if (level.getGameTime() % 5 == 0) {
                level.sendBlockUpdated(pos, state, state, 3);
            }

            if (tile.progress >= tile.maxProgress) {
                tile.craftItem();
                tile.progress = 0;
            }
        } else {
            tile.perTick = 0;

            // Медленный откат прогресса при отсутствии питания
            if (!hasValidPower && tile.progress > 0) {
                tile.progress = Math.max(0, tile.progress - 1);
            }
        }
    }

    public float getSpeed() {
        if (inputPower <= 0) return 0;
        float friction = getFriction();
        return (float) Math.sqrt((60 * inputPower) / (OIL_PRESS_EFFICIENCY * MOMENT_OF_INERTIA * PI * friction));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.mech.oil_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new OilPressMenu(containerId, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return lazyItemHandler.cast();

            if (side == Direction.DOWN) return LazyOptional.empty();

            return LazyOptional.of(() -> new IItemHandlerModifiable() {
                @Override
                public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                    itemHandler.setStackInSlot(slot, stack);
                }

                @Override
                public int getSlots() { return 1; }

                @Override
                public @NotNull ItemStack getStackInSlot(int slot) {
                    return itemHandler.getStackInSlot(0);
                }

                @Override
                public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                    return itemHandler.insertItem(0, stack, simulate);
                }

                @Override
                public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return ItemStack.EMPTY;
                }

                @Override
                public int getSlotLimit(int slot) { return 64; }

                @Override
                public boolean isItemValid(int slot, @NotNull ItemStack stack) { return true; }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        if (level != null && !level.isClientSide) {
            KineticNetworkManager.scheduleNetworkUpdate(level, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            KineticNetworkManager.scheduleNetworkUpdate(level, worldPosition);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    private boolean willOutputFit() {
        return fluidTank.getFluidAmount() < fluidTank.getCapacity();
    }

    private void craftItem() {
        ItemStack input = itemHandler.getStackInSlot(0);

        if (input.getItem() == ItemRegistry.RAPESEED_CLEANED.get()) {
            int filled = fluidTank.fill(new FluidStack(FluidRegistry.RAPESEED_OIL.source.get(), 50),
                    IFluidHandler.FluidAction.EXECUTE);
            if (filled == 50) {
                input.shrink(1);
                setChanged();
                if (level == null) return;
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putFloat("progress", progress);
        tag.putFloat("perTick", perTick);
        CompoundTag fluidTag = new CompoundTag();
        fluidTank.writeToNBT(fluidTag);
        tag.put("fluid", fluidTag);
        tag.putFloat("inputPower", inputPower);
        tag.putBoolean("lubricated", lubricated);
        tag.putInt("lubricantTimer", lubricantTimer);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getFloat("progress");
        perTick = tag.getFloat("perTick");
        fluidTank.readFromNBT(tag.getCompound("fluid"));
        this.inputPower = tag.getFloat("inputPower");
        this.lubricated = tag.getBoolean("lubricated");
        this.lubricantTimer = tag.getInt("lubricantTimer");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putFloat("progress", progress);
        tag.putFloat("perTick", perTick);
        CompoundTag fluidTag = new CompoundTag();
        fluidTank.writeToNBT(fluidTag);
        tag.put("fluid", fluidTag);
        tag.putFloat("inputPower", inputPower);
        tag.putBoolean("lubricated", lubricated);
        tag.putInt("lubricantTimer", lubricantTimer);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getFloat("progress");
        perTick = tag.getFloat("perTick");
        fluidTank.readFromNBT(tag.getCompound("fluid"));
        inputPower = tag.getFloat("inputPower");
        this.lubricated = tag.getBoolean("lubricated");
        this.lubricantTimer = tag.getInt("lubricantTimer");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
