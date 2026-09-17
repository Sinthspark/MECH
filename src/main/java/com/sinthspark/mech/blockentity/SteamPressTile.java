package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.SteamPressBlock;
import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.screen.SteamPressMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteamPressTile extends BlockEntity implements MenuProvider, IFluidHandler {

    private static final int TANK_CAPACITY = 4000;
    private static final int STEAM_PER_TICK = 7;
    private static final int TOTAL_PRESS_TICKS = 1200;
    private static final float SPEED_MULTIPLIER = 1.5f;

    public int progress = 0;
    public int maxProgress = TOTAL_PRESS_TICKS;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0;
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    public SteamPressTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_PRESS.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamPressTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.isActive();
        boolean active = false;

        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, tile.inventory.getStackInSlot(0));

        var recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.PRESSING_TYPE.get(), container, level)
                .orElse(null);

        if (recipe != null) {
            int processTime = (int) (recipe.getCookingTime() / SPEED_MULTIPLIER);
            tile.maxProgress = processTime;
            FluidStack fluid = tile.tank.getFluid();

            if (!fluid.isEmpty() && fluid.getAmount() >= STEAM_PER_TICK) {
                ItemStack output = tile.inventory.getStackInSlot(1);
                ItemStack result = recipe.getResultItem(level.registryAccess());

                if (output.isEmpty() || (output.is(result.getItem()) && output.getCount() < output.getMaxStackSize())) {
                    tile.tank.drain(STEAM_PER_TICK, FluidAction.EXECUTE);
                    tile.progress++;
                    active = true;

                    if (tile.progress >= processTime) {
                        tile.inventory.extractItem(0, 1, false);
                        if (output.isEmpty()) {
                            tile.inventory.setStackInSlot(1, result.copy());
                        } else {
                            output.grow(1);
                        }
                        tile.progress = 0;
                        active = false;
                    }
                } else {
                    tile.progress = 0;
                }
            } else {
                tile.progress = 0;
            }
        } else {
            tile.progress = 0;
        }

        if (wasActive != active) {
            level.setBlock(pos, state.setValue(SteamPressBlock.ACTIVE, active), 3);
        }

        setChanged(level, pos, state);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    public boolean isActive() { return progress > 0; }
    public int getFluidAmount() { return tank.getFluidAmount(); }
    public int getTankCapacity() { return TANK_CAPACITY; }
    public IItemHandler getInventory() { return inventory; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return LazyOptional.of(() -> this).cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandler.cast();
            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> new IItemHandler() {
                    @Override public int getSlots() { return 1; }
                    @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(1); }
                    @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return inventory.extractItem(1, a, sim); }
                    @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return stack; }
                    @Override public int getSlotLimit(int s) { return 64; }
                    @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return false; }
                }).cast();
            }
            return LazyOptional.of(() -> new IItemHandler() {
                @Override public int getSlots() { return 1; }
                @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(0); }
                @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return ItemStack.EMPTY; }
                @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return inventory.insertItem(0, stack, sim); }
                @Override public int getSlotLimit(int s) { return 64; }
                @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return true; }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); itemHandler.invalidate(); }
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech.steam_press"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return new SteamPressMenu(id, inv, this); }
    @Override public int getTanks() { return tank.getTanks(); }
    @Override public @NotNull FluidStack getFluidInTank(int i) { return tank.getFluidInTank(i); }
    @Override public int getTankCapacity(int i) { return tank.getTankCapacity(i); }
    @Override public boolean isFluidValid(int i, @NotNull FluidStack s) { return tank.isFluidValid(i, s); }
    @Override public int fill(FluidStack r, FluidAction a) { return tank.fill(r, a); }
    @Override public @NotNull FluidStack drain(FluidStack r, FluidAction a) { return tank.drain(r, a); }
    @Override public @NotNull FluidStack drain(int m, FluidAction a) { return tank.drain(m, a); }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
    }
}
