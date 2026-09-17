package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.recipe.ExtractingRecipe;
import com.sinthspark.mech.recipe.ModRecipes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

/** Shared logic for the steam-extractor tier line (base / advanced / upgraded). */
public abstract class AbstractSteamExtractorTile extends BlockEntity implements MenuProvider, IFluidHandler {
    private final float steamRateMultiplier;
    private final float timeMultiplier;
    private float steamAccumulator;
    public int progress = 0;
    public int maxProgress = 1;
    private final FluidTank steamTank;
    private final FluidTank outputTank;
    private final ItemStackHandler inventory;

    private final LazyOptional<IItemHandler> itemHandler;
    private final LazyOptional<IFluidHandler> steamHandler;
    private final LazyOptional<IFluidHandler> outputFluidHandler;

    private LazyOptional<IItemHandler> inputOnlyHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> outputOnlyHandler = LazyOptional.empty();

    private ResourceLocation lastRecipeId;

    protected AbstractSteamExtractorTile(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int steamTankCapacity,
            int outputTankCapacity,
            float steamRateMultiplier,
            float timeMultiplier
    ) {
        super(type, pos, state);

        this.steamRateMultiplier = steamRateMultiplier;
        this.timeMultiplier = timeMultiplier;

        this.steamTank = new FluidTank(steamTankCapacity) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };

        this.outputTank = new FluidTank(outputTankCapacity) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };

        this.inventory = new ItemStackHandler(2) {
            @Override
            protected void onContentsChanged(int slot) {
                if (slot == 0 && getStackInSlot(0).isEmpty()) {
                    progress = 0;
                    steamAccumulator = 0;

                    if (level != null && !level.isClientSide) {
                        level.setBlock(
                                worldPosition,
                                getBlockState().setValue(activeProperty(), false),
                                3
                        );
                    }
                }

                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot == 0;
            }
        };

        this.itemHandler = LazyOptional.of(() -> this.inventory);
        this.steamHandler = LazyOptional.of(() -> this.steamTank);
        this.outputFluidHandler = LazyOptional.of(() -> this.outputTank);
    }

    protected abstract BooleanProperty activeProperty();
    protected abstract String translationKey();

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractSteamExtractorTile tile) {
        if (level.isClientSide) return;
        boolean wasActive = tile.isActive();
        boolean active = false;
        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, tile.inventory.getStackInSlot(0));
        ExtractingRecipe recipe = level.getRecipeManager().getRecipeFor(ModRecipes.EXTRACTING_TYPE.get(), container, level).orElse(null);
        ResourceLocation recipeId = recipe == null ? null : recipe.getId();
        if (recipeId == null || !recipeId.equals(tile.lastRecipeId)) {
            tile.progress = 0;
            tile.steamAccumulator = 0;
            tile.lastRecipeId = recipeId;
        }
        if (recipe != null) {
            int cost = recipe.getSteamCost();
            int recipeTime = recipe.getProcessTime();
            if (cost > 0 && recipeTime > 0) {
                float rawRate = (float) cost / recipeTime * tile.steamRateMultiplier;
                int processTime = Math.max(1, (int) (recipeTime * tile.timeMultiplier));
                tile.maxProgress = processTime;
                ItemStack output = tile.inventory.getStackInSlot(1);
                ItemStack result = recipe.getResult();
                FluidStack fluidResult = recipe.getFluidResult();
                boolean canFitItem = result.isEmpty() ||
                        (result.getCount() > 0 && result.getCount() <= result.getMaxStackSize() &&
                         (output.isEmpty() ||
                          (ItemStack.isSameItemSameTags(output, result) &&
                           output.getCount() + result.getCount() <= output.getMaxStackSize())));
                boolean canFitFluid = fluidResult.isEmpty() || tile.outputTank.fill(fluidResult, FluidAction.SIMULATE) >= fluidResult.getAmount();
                if (canFitItem && canFitFluid) {
                    tile.steamAccumulator += rawRate;
                    int steamToConsume = Math.min((int) tile.steamAccumulator, tile.steamTank.getFluidAmount());
                    if (steamToConsume > 0) {
                        tile.steamTank.drain(steamToConsume, FluidAction.EXECUTE);
                        tile.steamAccumulator -= steamToConsume;
                        tile.progress++;
                        active = true;
                        if (tile.progress >= processTime) {
                            tile.inventory.extractItem(0, 1, false);
                            if (!result.isEmpty()) {
                                ItemStack updatedOutput = output.isEmpty() ? result.copy() : output.copy();
                                if (!output.isEmpty()) updatedOutput.grow(result.getCount());
                                tile.inventory.setStackInSlot(1, updatedOutput);
                            }
                            if (!fluidResult.isEmpty()) tile.outputTank.fill(fluidResult.copy(), FluidAction.EXECUTE);
                            tile.progress = 0;
                            tile.steamAccumulator = 0;
                            active = false;
                        }
                    }
                }
            } else {
                tile.progress = 0;
                tile.steamAccumulator = 0;
                tile.maxProgress = 1;
            }
        } else {
            tile.progress = 0;
            tile.steamAccumulator = 0;
        }
        if (wasActive != active) {
            level.setBlock(pos, state.setValue(tile.activeProperty(), active), 3);
            tile.setChanged();
        }
    }

    public boolean isActive() { return progress > 0; }
    public int getSteamAmount() { return steamTank.getFluidAmount(); }
    public FluidStack getOutputFluid() { return outputTank.getFluid(); }
    public int getOutputFluidAmount() { return outputTank.getFluidAmount(); }
    public int getOutputTankCapacity() { return outputTank.getCapacity(); }
    public IItemHandler getInventory() { return inventory; }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return (side == Direction.DOWN ? outputFluidHandler : steamHandler).cast();
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
    @Override public void invalidateCaps() { super.invalidateCaps(); itemHandler.invalidate(); steamHandler.invalidate(); outputFluidHandler.invalidate(); inputOnlyHandler.invalidate(); outputOnlyHandler.invalidate(); }
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech." + translationKey()); }
    @Override public int getTanks() { return steamTank.getTanks(); }
    @Override public @NotNull FluidStack getFluidInTank(int i) { return steamTank.getFluidInTank(i); }
    @Override public int getTankCapacity(int i) { return steamTank.getTankCapacity(i); }
    @Override public boolean isFluidValid(int i, @NotNull FluidStack stack) { return steamTank.isFluidValid(i, stack); }
    @Override public int fill(FluidStack resource, FluidAction action) { return steamTank.fill(resource, action); }
    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return steamTank.drain(resource, action); }
    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return steamTank.drain(maxDrain, action); }
    @Override protected void saveAdditional(@NotNull CompoundTag tag) { super.saveAdditional(tag); tag.put("steamTank", steamTank.writeToNBT(new CompoundTag())); tag.put("outputTank", outputTank.writeToNBT(new CompoundTag())); tag.put("inventory", inventory.serializeNBT()); tag.putInt("progress", progress); tag.putInt("maxProgress", maxProgress); tag.putFloat("steamAccumulator", steamAccumulator); if (lastRecipeId != null) tag.putString("lastRecipeId", lastRecipeId.toString()); }
    @Override public void load(@NotNull CompoundTag tag) { super.load(tag); steamTank.readFromNBT(tag.getCompound("steamTank")); outputTank.readFromNBT(tag.getCompound("outputTank")); inventory.deserializeNBT(tag.getCompound("inventory")); progress = tag.getInt("progress"); maxProgress = tag.getInt("maxProgress"); steamAccumulator = tag.getFloat("steamAccumulator"); lastRecipeId = tag.contains("lastRecipeId") ? ResourceLocation.tryParse(tag.getString("lastRecipeId")) : null; }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public @NotNull CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); tag.put("steamTank", steamTank.writeToNBT(new CompoundTag())); tag.put("outputTank", outputTank.writeToNBT(new CompoundTag())); tag.put("inventory", inventory.serializeNBT()); tag.putInt("progress", progress); tag.putInt("maxProgress", maxProgress); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag) { super.handleUpdateTag(tag); steamTank.readFromNBT(tag.getCompound("steamTank")); outputTank.readFromNBT(tag.getCompound("outputTank")); inventory.deserializeNBT(tag.getCompound("inventory")); progress = tag.getInt("progress"); maxProgress = tag.getInt("maxProgress"); }
}
