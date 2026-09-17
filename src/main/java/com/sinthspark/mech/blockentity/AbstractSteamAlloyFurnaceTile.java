package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.recipe.AlloyingRecipe;
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

/** Shared logic for the steam-alloy-furnace tier line (base / advanced / upgraded). */
public abstract class AbstractSteamAlloyFurnaceTile extends BlockEntity implements MenuProvider, IFluidHandler {
    private final float steamRateMultiplier;
    private final float timeMultiplier;
    public int progress = 0;
    public int maxProgress = 1;
    private final FluidTank tank;
    private final ItemStackHandler inventory;
    private final LazyOptional<IItemHandler> itemHandler;
    private final LazyOptional<IFluidHandler> fluidHandler;
    private LazyOptional<IItemHandler> inputOnlyHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> outputOnlyHandler = LazyOptional.empty();
    private ResourceLocation lastRecipeId;

    protected AbstractSteamAlloyFurnaceTile(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int tankCapacity,
            float steamRateMultiplier,
            float timeMultiplier,
            boolean ignoredRateFloor
    ) {
        super(type, pos, state);

        this.steamRateMultiplier = steamRateMultiplier;
        this.timeMultiplier = timeMultiplier;

        this.tank = new FluidTank(tankCapacity) {
            @Override
            protected void onContentsChanged() {
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
        };

        this.inventory = new ItemStackHandler(4) {
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
                return slot < 3;
            }
        };

        this.itemHandler = LazyOptional.of(() -> this.inventory);
        this.fluidHandler = LazyOptional.of(() -> this);
    }

    protected abstract BooleanProperty activeProperty();
    protected abstract String translationKey();

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractSteamAlloyFurnaceTile tile) {
        if (level.isClientSide) return;
        boolean wasActive = tile.isActive();
        boolean active = false;
        SimpleContainer container = new SimpleContainer(3);
        for (int i = 0; i < 3; i++) container.setItem(i, tile.inventory.getStackInSlot(i));
        AlloyingRecipe recipe = level.getRecipeManager().getRecipeFor(ModRecipes.ALLOYING_TYPE.get(), container, level).orElse(null);
        ResourceLocation recipeId = recipe == null ? null : recipe.getId();
        if (recipeId == null || !recipeId.equals(tile.lastRecipeId)) { tile.progress = 0; tile.lastRecipeId = recipeId; }
        if (recipe != null) {
            int cost = recipe.getSteamCost();
            int recipeTime = recipe.getProcessTime();
            if (recipeTime > 0) {
                float rawRate = (float) cost / recipeTime * tile.steamRateMultiplier;
                int steamPerTick = Math.max(1, (int) rawRate);
                int processTime = Math.max(1, (int) (recipeTime * tile.timeMultiplier));
                tile.maxProgress = processTime;
                FluidStack fluid = tile.tank.getFluid();
                if (!fluid.isEmpty() && steamPerTick > 0 && fluid.getAmount() >= steamPerTick) {
                    ItemStack output = tile.inventory.getStackInSlot(3);
                    ItemStack result = recipe.getResultItem(level.registryAccess());
                    int resultCount = result.getCount();
                    boolean outputFits = !result.isEmpty() && resultCount > 0 && resultCount <= result.getMaxStackSize() &&
                            (output.isEmpty() || (ItemStack.isSameItemSameTags(output, result) && output.getCount() + resultCount <= output.getMaxStackSize()));
                    if (outputFits) {
                        tile.tank.drain(steamPerTick, FluidAction.EXECUTE);
                        tile.progress++;
                        active = true;
                        if (tile.progress >= processTime) {
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
                            ItemStack updatedOutput = output.isEmpty() ? result.copy() : output.copy();
                            if (!output.isEmpty()) updatedOutput.grow(resultCount);
                            tile.inventory.setStackInSlot(3, updatedOutput);
                            tile.progress = 0;
                            active = false;
                        }
                    } else tile.progress = 0;
                } else tile.progress = 0;
            } else tile.progress = 0;
        } else tile.progress = 0;
        if (wasActive != active) level.setBlock(pos, state.setValue(tile.activeProperty(), active), 3);
        setChanged(level, pos, state);
    }

    public boolean isActive() { return progress > 0; }
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
                    @Override public @NotNull ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(3); }
                    @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return inventory.extractItem(3, amount, simulate); }
                    @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }
                    @Override public int getSlotLimit(int slot) { return 64; }
                    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
                });
                return outputOnlyHandler.cast();
            }
            if (!inputOnlyHandler.isPresent()) inputOnlyHandler = LazyOptional.of(() -> new IItemHandler() {
                @Override public int getSlots() { return 3; }
                @Override public @NotNull ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
                @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
                @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return inventory.insertItem(slot, stack, simulate); }
                @Override public int getSlotLimit(int slot) { return 64; }
                @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return slot < 3 && inventory.isItemValid(slot, stack); }
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
    @Override protected void saveAdditional(@NotNull CompoundTag tag) { super.saveAdditional(tag); tag.put("tank", tank.writeToNBT(new CompoundTag())); tag.put("inventory", inventory.serializeNBT()); tag.putInt("progress", progress); tag.putInt("maxProgress", maxProgress); if (lastRecipeId != null) tag.putString("lastRecipeId", lastRecipeId.toString()); }
    @Override public void load(@NotNull CompoundTag tag) { super.load(tag); tank.readFromNBT(tag.getCompound("tank")); inventory.deserializeNBT(tag.getCompound("inventory")); progress = tag.getInt("progress"); maxProgress = tag.getInt("maxProgress"); lastRecipeId = tag.contains("lastRecipeId") ? ResourceLocation.tryParse(tag.getString("lastRecipeId")) : null; }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public @NotNull CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); tag.put("tank", tank.writeToNBT(new CompoundTag())); tag.put("inventory", inventory.serializeNBT()); tag.putInt("progress", progress); tag.putInt("maxProgress", maxProgress); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag) { super.handleUpdateTag(tag); tank.readFromNBT(tag.getCompound("tank")); inventory.deserializeNBT(tag.getCompound("inventory")); progress = tag.getInt("progress"); maxProgress = tag.getInt("maxProgress"); }
}
