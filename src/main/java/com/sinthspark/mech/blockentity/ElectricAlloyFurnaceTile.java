package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.ElectricAlloyFurnaceBlock;
import com.sinthspark.mech.energy.MechEnergyStorage;
import com.sinthspark.mech.recipe.AlloyingRecipe;
import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.screen.ElectricAlloyFurnaceMenu;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectricAlloyFurnaceTile extends BlockEntity implements MenuProvider {

    private static final int ENERGY_CAPACITY = 500;
    private static final int ENERGY_PER_TICK = 8;

    public int progress = 0;
    public int maxProgress;

    private final MechEnergyStorage energyStorage = new MechEnergyStorage(ENERGY_CAPACITY, 32, 32);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot < 3;
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    public ElectricAlloyFurnaceTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_ALLOY_FURNACE.get(), pos, state);
        this.maxProgress = 100;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricAlloyFurnaceTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.isActive();
        boolean active = false;

        SimpleContainer container = new SimpleContainer(3);
        for (int i = 0; i < 3; i++) {
            container.setItem(i, tile.inventory.getStackInSlot(i));
        }

        var recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.ALLOYING_TYPE.get(), container, level)
                .orElse(null);

        if (recipe != null && tile.energyStorage.getEnergyStored() >= ENERGY_PER_TICK) {
            ItemStack output = tile.inventory.getStackInSlot(3);
            ItemStack result = recipe.getResultItem(level.registryAccess());

            if (output.isEmpty() || (output.is(result.getItem()) && output.getCount() < output.getMaxStackSize())) {
                tile.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
                tile.maxProgress = recipe.getProcessTime();
                tile.progress++;
                active = true;

                if (tile.progress >= recipe.getProcessTime()) {
                    for (AlloyingRecipe.CountedIngredient ci : recipe.getCountedIngredients()) {
                        int remaining = ci.count();
                        for (int i = 0; i < 3; i++) {
                            ItemStack slotStack = tile.inventory.getStackInSlot(i);
                            if (ci.ingredient().test(slotStack)) {
                                int toExtract = Math.min(remaining, slotStack.getCount());
                                tile.inventory.extractItem(i, toExtract, false);
                                remaining -= toExtract;
                                if (remaining <= 0) break;
                            }
                        }
                    }
                    if (output.isEmpty()) {
                        tile.inventory.setStackInSlot(3, result.copy());
                    } else {
                        output.grow(result.getCount());
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

        if (wasActive != active) {
            level.setBlock(pos, state.setValue(ElectricAlloyFurnaceBlock.ACTIVE, active), 3);
        }

        setChanged(level, pos, state);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    public boolean isActive() { return progress > 0; }
    public IItemHandler getInventory() { return inventory; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandler.cast();
            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> new IItemHandler() {
                    @Override public int getSlots() { return 1; }
                    @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(3); }
                    @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return inventory.extractItem(3, a, sim); }
                    @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return stack; }
                    @Override public int getSlotLimit(int s) { return 64; }
                    @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return false; }
                }).cast();
            }
            return LazyOptional.of(() -> new IItemHandler() {
                @Override public int getSlots() { return 3; }
                @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(s); }
                @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return ItemStack.EMPTY; }
                @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return inventory.insertItem(s, stack, sim); }
                @Override public int getSlotLimit(int s) { return 64; }
                @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return s < 3; }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); energyHandler.invalidate(); itemHandler.invalidate(); }
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech.electric_alloy_furnace"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return new ElectricAlloyFurnaceMenu(id, inv, this); }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("energy", energyStorage.writeToNBT(new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        energyStorage.readFromNBT(tag.getCompound("energy"));
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }
}
