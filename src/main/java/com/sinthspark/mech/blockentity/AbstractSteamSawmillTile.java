package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.recipe.SawingRecipe;
import com.sinthspark.mech.registration.ItemRegistry;
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
import net.minecraft.world.item.Items;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.tags.ItemTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Shared logic for the steam-sawmill tier line (base / advanced / upgraded). */
public abstract class AbstractSteamSawmillTile extends BlockEntity implements MenuProvider, IFluidHandler {
    private final int steamPerTick;
    private final float speedMultiplier;
    public int progress = 0;
    public int maxProgress = 1;
    private final FluidTank tank;
    private final ItemStackHandler inventory;
    private ResourceLocation lastRecipeId;

    private final LazyOptional<IFluidHandler> fluidHandler;
    private final LazyOptional<IItemHandler> itemHandler;

    private LazyOptional<IItemHandler> inputOnlyHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> outputOnlyHandler = LazyOptional.empty();

    protected AbstractSteamSawmillTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int tankCapacity, int steamPerTick, float speedMultiplier) {
        super(type, pos, state);
        this.steamPerTick = steamPerTick;
        this.speedMultiplier = speedMultiplier;
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
    protected abstract BooleanProperty activeProperty();
    protected abstract String translationKey();

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractSteamSawmillTile tile) {
        if (level.isClientSide) return;
        boolean wasActive = tile.isActive();
        boolean active = false;
        ItemStack input = tile.inventory.getStackInSlot(0);
        ItemStack result = tile.getResult(level, input);
        ResourceLocation recipeId = tile.getRecipeId(level, input);
        if (!java.util.Objects.equals(recipeId, tile.lastRecipeId)) { tile.progress = 0; tile.lastRecipeId = recipeId; }
        if (!result.isEmpty() && tile.canFitOutput(result)) {
            FluidStack fluid = tile.tank.getFluid();
            int processTime = tile.getProcessTime();
            if (!fluid.isEmpty() && fluid.getAmount() >= tile.steamPerTick && processTime > 0) {
                tile.tank.drain(tile.steamPerTick, FluidAction.EXECUTE);
                tile.progress++;
                active = true;
                tile.maxProgress = processTime;
                if (tile.progress >= processTime) { tile.craftItem(level, result); tile.progress = 0; active = false; }
            } else tile.progress = 0;
        } else tile.progress = 0;
        if (wasActive != active) level.setBlock(pos, state.setValue(tile.activeProperty(), active), 3);
        setChanged(level, pos, state);
    }

    private ResourceLocation getRecipeId(Level level, ItemStack input) {
        if (input.isEmpty()) return null;
        for (SawingRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.SAWING_TYPE.get())) {
            if (recipe.matches(new SimpleContainer(input), level)) return recipe.getId();
        }
        return null;
    }
    private int getProcessTime() { return (int) (1800 / speedMultiplier); }
    private ItemStack getResult(Level level, ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;
        if (input.is(ItemTags.LOGS)) {
            ItemStack result = resolveBySuffix(input, "_log", 6); if (!result.isEmpty()) return result;
            result = resolveBySuffix(input, "_wood", 6); if (!result.isEmpty()) return result;
            result = resolveBySuffix(input, "_stem", 6); if (!result.isEmpty()) return result;
            result = resolveBySuffix(input, "_hyphae", 6); if (!result.isEmpty()) return result;
        }
        if (input.is(ItemTags.PLANKS)) { ItemStack result = resolveBySuffix(input, "_planks", 2); if (!result.isEmpty()) return result; }
        if (input.is(Items.STICK) && input.getCount() >= 4) return new ItemStack(ItemRegistry.WHEEL_ARC_WITHOUT_STICK.get(), 1);
        if (input.is(ItemTags.WOODEN_SLABS)) return new ItemStack(Items.STICK, 4);
        for (SawingRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.SAWING_TYPE.get())) if (recipe.matches(new SimpleContainer(input), level)) return recipe.assemble(new SimpleContainer(input), level.registryAccess());
        return ItemStack.EMPTY;
    }
    private ResourceLocation resolveKey(ItemStack input) { var key = ForgeRegistries.ITEMS.getKey(input.getItem()); return key; }
    private ItemStack resolveBySuffix(ItemStack input, String suffix, int count) {
        var key = resolveKey(input); if (key == null) return ItemStack.EMPTY; String path = key.getPath(); String name = null;
        if (path.endsWith(suffix)) name = path.substring(0, path.length() - suffix.length()) + "_planks";
        if ("_planks".equals(suffix) && path.endsWith("_planks")) name = path.substring(0, path.length() - 7) + "_slab";
        if (name == null) return ItemStack.EMPTY; var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key.getNamespace(), name));
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }
    private boolean canFitOutput(ItemStack result) { ItemStack output = inventory.getStackInSlot(1); return output.isEmpty() || (ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize()); }
    private void craftItem(Level level, ItemStack result) {
        ItemStack input = inventory.getStackInSlot(0); ItemStack output = inventory.getStackInSlot(1); if (!canFitOutput(result)) return;
        if (output.isEmpty()) inventory.setStackInSlot(1, result.copy()); else { ItemStack updated = output.copy(); updated.grow(result.getCount()); inventory.setStackInSlot(1, updated); }
        int consumed = input.is(Items.STICK) ? 4 : 1; inventory.extractItem(0, consumed, false);
    }
    public boolean isActive() { return progress > 0; }
    public int getFluidAmount() { return tank.getFluidAmount(); }
    public int getTankCapacity() { return tank.getCapacity(); }
    public IItemHandler getInventory() { return inventory; }
    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandler.cast();
            if (side == Direction.DOWN) { if (!outputOnlyHandler.isPresent()) outputOnlyHandler = LazyOptional.of(() -> new IItemHandler() {
                public int getSlots(){return 1;} public @NotNull ItemStack getStackInSlot(int s){return inventory.getStackInSlot(1);} public @NotNull ItemStack extractItem(int s,int a,boolean sim){return inventory.extractItem(1,a,sim);} public @NotNull ItemStack insertItem(int s,@NotNull ItemStack st,boolean sim){return st;} public int getSlotLimit(int s){return 64;} public boolean isItemValid(int s,@NotNull ItemStack st){return false;}
            }); return outputOnlyHandler.cast(); }
            if (!inputOnlyHandler.isPresent()) inputOnlyHandler = LazyOptional.of(() -> new IItemHandler() {
                public int getSlots(){return 1;} public @NotNull ItemStack getStackInSlot(int s){return inventory.getStackInSlot(0);} public @NotNull ItemStack extractItem(int s,int a,boolean sim){return ItemStack.EMPTY;} public @NotNull ItemStack insertItem(int s,@NotNull ItemStack st,boolean sim){return inventory.insertItem(0,st,sim);} public int getSlotLimit(int s){return 64;} public boolean isItemValid(int s,@NotNull ItemStack st){return true;}
            }); return inputOnlyHandler.cast();
        }
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();itemHandler.invalidate();fluidHandler.invalidate();inputOnlyHandler.invalidate();outputOnlyHandler.invalidate();}
    @Override public @NotNull Component getDisplayName(){return Component.translatable("block.mech."+translationKey());}
    @Override public int getTanks(){return tank.getTanks();} @Override public @NotNull FluidStack getFluidInTank(int i){return tank.getFluidInTank(i);} @Override public int getTankCapacity(int i){return tank.getTankCapacity(i);} @Override public boolean isFluidValid(int i,@NotNull FluidStack s){return tank.isFluidValid(i,s);} @Override public int fill(FluidStack r,FluidAction a){return tank.fill(r,a);} @Override public @NotNull FluidStack drain(FluidStack r,FluidAction a){return tank.drain(r,a);} @Override public @NotNull FluidStack drain(int m,FluidAction a){return tank.drain(m,a);}
    @Override protected void saveAdditional(@NotNull CompoundTag tag){super.saveAdditional(tag);tag.put("tank",tank.writeToNBT(new CompoundTag()));tag.put("inventory",inventory.serializeNBT());tag.putInt("progress",progress);tag.putInt("maxProgress",maxProgress);if(lastRecipeId!=null)tag.putString("lastRecipeId",lastRecipeId.toString());}
    @Override public void load(@NotNull CompoundTag tag){super.load(tag);tank.readFromNBT(tag.getCompound("tank"));inventory.deserializeNBT(tag.getCompound("inventory"));progress=tag.getInt("progress");maxProgress=tag.getInt("maxProgress");lastRecipeId=tag.contains("lastRecipeId")?ResourceLocation.tryParse(tag.getString("lastRecipeId")):null;}
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public @NotNull CompoundTag getUpdateTag(){CompoundTag tag=super.getUpdateTag();tag.put("tank",tank.writeToNBT(new CompoundTag()));tag.put("inventory",inventory.serializeNBT());tag.putInt("progress",progress);tag.putInt("maxProgress",maxProgress);return tag;}
    @Override public void handleUpdateTag(CompoundTag tag){super.handleUpdateTag(tag);tank.readFromNBT(tag.getCompound("tank"));inventory.deserializeNBT(tag.getCompound("inventory"));progress=tag.getInt("progress");maxProgress=tag.getInt("maxProgress");}
}
