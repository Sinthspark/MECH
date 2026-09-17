package com.sinthspark.mech.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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

/** Shared logic for single-input / single-output steam machines. */
public abstract class AbstractSteamProcessorTile<R extends Recipe<SimpleContainer>> extends BlockEntity implements MenuProvider, IFluidHandler {
    private final float steamRateMultiplier;
    private final float timeMultiplier;
    private final boolean applyRateFloor;
    private float steamAccumulator;
    public int progress = 0;
    public int maxProgress = 1;
    private final FluidTank tank;
    private final ItemStackHandler inventory;
    private ResourceLocation lastRecipeId;

    private final LazyOptional<IFluidHandler> fluidHandler;
    private final LazyOptional<IItemHandler> itemHandler;

    private LazyOptional<IItemHandler> inputOnlyHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> outputOnlyHandler = LazyOptional.empty();

    protected AbstractSteamProcessorTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int tankCapacity, float steamRateMultiplier, float timeMultiplier, boolean applyRateFloor) {
        super(type, pos, state);
        this.steamRateMultiplier = steamRateMultiplier;
        this.timeMultiplier = timeMultiplier;
        this.applyRateFloor = applyRateFloor;
        this.tank = new FluidTank(tankCapacity) {
            @Override protected void onContentsChanged() { setChanged(); if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
        };
        this.inventory = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();

                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(
                            worldPosition,
                            getBlockState(),
                            getBlockState(),
                            3
                    );
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot == 0;
            }
        };

        this.fluidHandler = LazyOptional.of(() -> this);
        this.itemHandler = LazyOptional.of(() -> this.inventory);
    }

    protected abstract RecipeType<R> recipeType();
    protected abstract BooleanProperty activeProperty();
    protected abstract String translationKey();
    protected abstract int steamCost(R recipe);
    protected abstract int processTime(R recipe);
    protected abstract ItemStack resultItem(R recipe, Level level);
    protected int inputConsumeCount(R recipe) { return 1; }

    public static <R extends Recipe<SimpleContainer>> void tick(Level level, BlockPos pos, BlockState state, AbstractSteamProcessorTile<R> tile) {
        if (level.isClientSide) return;
        boolean wasActive = state.getValue(tile.activeProperty());
        boolean active = false;
        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, tile.inventory.getStackInSlot(0));
        R recipe = level.getRecipeManager().getRecipeFor(tile.recipeType(), container, level).orElse(null);
        ResourceLocation recipeId = recipe == null ? null : recipe.getId();
        if (recipeId == null || !recipeId.equals(tile.lastRecipeId)) {
            tile.progress = 0;
            tile.steamAccumulator = 0;
            tile.lastRecipeId = recipeId;
        }
        if (recipe != null) {
            int cost = tile.steamCost(recipe);
            int recipeTime = tile.processTime(recipe);
            if (recipeTime <= 0) {
                tile.progress = 0;
                tile.steamAccumulator = 0;
            } else {
                float rawRate = (float) cost / recipeTime * tile.steamRateMultiplier;
                if (tile.applyRateFloor) rawRate = Math.max(1.0f, rawRate);
                int processTime = Math.max(1, (int) (recipeTime * tile.timeMultiplier));
                tile.maxProgress = processTime;
                ItemStack output = tile.inventory.getStackInSlot(1);
                ItemStack result = tile.resultItem(recipe, level);
                boolean resultFitsSingleStack = !result.isEmpty() && result.getCount() > 0 && result.getCount() <= result.getMaxStackSize();
                boolean outputFits = resultFitsSingleStack && (output.isEmpty() || (ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize()));
                if (outputFits) {
                    tile.steamAccumulator += rawRate;
                    int steamToConsume = Math.min((int) tile.steamAccumulator, tile.tank.getFluidAmount());
                    if (steamToConsume > 0) {
                        tile.tank.drain(steamToConsume, FluidAction.EXECUTE);
                        tile.steamAccumulator -= steamToConsume;
                    }
                    if (tile.tank.getFluidAmount() > 0 || steamToConsume > 0) {
                        tile.progress++;
                        active = true;
                        if (tile.progress >= processTime) {
                            tile.inventory.extractItem(0, tile.inputConsumeCount(recipe), false);
                            ItemStack updatedOutput = output.isEmpty() ? result.copy() : output.copy();
                            if (!output.isEmpty()) updatedOutput.grow(result.getCount());
                            tile.inventory.setStackInSlot(1, updatedOutput);
                            tile.progress = 0;
                            tile.steamAccumulator = 0;
                            active = false;
                        }
                    }
                }
            }
        } else {
            tile.steamAccumulator = 0;
        }
        if (wasActive != active) {
            level.setBlock(pos, state.setValue(tile.activeProperty(), active), 3);
            tile.setChanged();
        }
    }

    public boolean isActive() { return level != null && level.getBlockState(worldPosition).hasProperty(activeProperty()) && level.getBlockState(worldPosition).getValue(activeProperty()); }
    public int getFluidAmount() { return tank.getFluidAmount(); }
    public int getTankCapacity() { return tank.getCapacity(); }
    public IItemHandler getInventory() { return inventory; }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandler.cast();
            if (side == Direction.DOWN) {
                if (!outputOnlyHandler.isPresent()) outputOnlyHandler = LazyOptional.of(() -> new IItemHandler() {
                    @Override public int getSlots() { return 1; }
                    @Override public @NotNull ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(1); }
                    @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return inventory.extractItem(1, amount, simulate); }
                    @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }
                    @Override public int getSlotLimit(int slot) { return 64; }
                    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
                });
                return outputOnlyHandler.cast();
            }
            if (!inputOnlyHandler.isPresent()) inputOnlyHandler = LazyOptional.of(() -> new IItemHandler() {
                @Override public int getSlots() { return 1; }
                @Override public @NotNull ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(0); }
                @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
                @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return inventory.insertItem(0, stack, simulate); }
                @Override public int getSlotLimit(int slot) { return 64; }
                @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return inventory.isItemValid(0, stack); }
            });
            return inputOnlyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); itemHandler.invalidate(); fluidHandler.invalidate(); inputOnlyHandler.invalidate(); outputOnlyHandler.invalidate(); }
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech." + translationKey()); }
    @Override public int getTanks() { return tank.getTanks(); }
    @Override public @NotNull FluidStack getFluidInTank(int i) { return tank.getFluidInTank(i); }
    @Override public int getTankCapacity(int i) { return tank.getTankCapacity(i); }
    @Override public boolean isFluidValid(int i, @NotNull FluidStack stack) { return tank.isFluidValid(i, stack); }
    @Override public int fill(FluidStack resource, FluidAction action) { return tank.fill(resource, action); }
    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return tank.drain(resource, action); }
    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return tank.drain(maxDrain, action); }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) { super.saveAdditional(tag); tag.put("tank", tank.writeToNBT(new CompoundTag())); tag.put("inventory", inventory.serializeNBT()); tag.putInt("progress", progress); tag.putInt("maxProgress", maxProgress); tag.putFloat("steamAccumulator", steamAccumulator); if (lastRecipeId != null) tag.putString("lastRecipeId", lastRecipeId.toString()); }
    @Override public void load(@NotNull CompoundTag tag) { super.load(tag); tank.readFromNBT(tag.getCompound("tank")); inventory.deserializeNBT(tag.getCompound("inventory")); progress = tag.getInt("progress"); maxProgress = tag.getInt("maxProgress"); steamAccumulator = tag.getFloat("steamAccumulator"); lastRecipeId = tag.contains("lastRecipeId") ? ResourceLocation.tryParse(tag.getString("lastRecipeId")) : null; }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public @NotNull CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); tag.put("tank", tank.writeToNBT(new CompoundTag())); tag.put("inventory", inventory.serializeNBT()); tag.putInt("progress", progress); tag.putInt("maxProgress", maxProgress); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag) { super.handleUpdateTag(tag); tank.readFromNBT(tag.getCompound("tank")); inventory.deserializeNBT(tag.getCompound("inventory")); progress = tag.getInt("progress"); maxProgress = tag.getInt("maxProgress"); }
}