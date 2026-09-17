package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.energy.KineticNetworkManager;
import com.sinthspark.mech.recipe.ModRecipes;
import com.sinthspark.mech.recipe.SawingRecipe;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.screen.SawmillMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SawmillTile extends BlockEntity implements MenuProvider {
    public static final float SAWMILL_EFFICIENCY = 0.9f;
    public static final float SAWMILL_FRICTION = 5.0f;
    public float inputPower = 0;
    public float minPower = 50.0f;
    public float maxPower = 80.0f;
    public float rotationDirection = 1.0f;
    public float sawRotation = 0;
    public float renderAngle = 0;
    public boolean lubricated = false;
    public int lubricantTimer = 0;
    public static final float LUBRICANT_REDUCTION = 0.3f;

    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
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

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public float progress = 0;
    public int maxProgress = 1800;
    public float perTick = 0;

    public SawmillTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SAWMILL.get(), pos, state);
    }

    public boolean isActive() { return progress > 0; }

    public float getFriction() {
        if (lubricated) return SAWMILL_FRICTION * (1 - LUBRICANT_REDUCTION);
        return SAWMILL_FRICTION;
    }

    public void applyLubricant() {
        lubricated = true;
        lubricantTimer = 6000;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SawmillTile tile) {
        if (level.isClientSide) return;

        // inputPower и perTick устанавливаются КИНЕТИЧЕСКОЙ СЕТЬЮ
        // Не обнуляем и не ищем соседей здесь!

        boolean hasValidPower = tile.inputPower >= tile.minPower &&
                tile.inputPower <= tile.maxPower;

        boolean canWork = hasValidPower &&
                !tile.itemHandler.getStackInSlot(0).isEmpty() &&
                tile.canFitOutput() &&
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
            level.sendBlockUpdated(pos, state, state, 3);

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
        float numerator = 60 * inputPower;
        float denominator = SAWMILL_EFFICIENCY * 0.5f * 3.14159f * friction;
        if (denominator <= 0) return 0;
        float result = (float) Math.sqrt(numerator / denominator);
        return Float.isNaN(result) ? 0 : result;
    }

    private void craftItem() {
        if (level == null) return;
        ItemStack input = itemHandler.getStackInSlot(0);
        ItemStack output = itemHandler.getStackInSlot(1);

        if (input.is(ItemTags.LOGS)) {
            var key = ForgeRegistries.ITEMS.getKey(input.getItem());
            if (key == null) return;
            String plankName = key.getPath()
                    .replace("_log", "_planks").replace("_wood", "_planks")
                    .replace("_stem", "_planks").replace("_hyphae", "_planks")
                    .replace("stripped_", "");
            var plankItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key.getNamespace(), plankName));
            if (plankItem != null) {
                ItemStack result = new ItemStack(plankItem, 6);
                if (output.isEmpty()) { itemHandler.setStackInSlot(1, result); input.shrink(1); }
                else if (output.getItem() == result.getItem() && output.getCount() + 6 <= 64) { output.grow(6); input.shrink(1); }
                return;
            }
        }

        if (input.is(ItemTags.PLANKS)) {
            var key = ForgeRegistries.ITEMS.getKey(input.getItem());
            if (key == null) return;
            String slabName = key.getPath().replace("_planks", "_slab");
            var slabItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key.getNamespace(), slabName));
            if (slabItem != null) {
                ItemStack result = new ItemStack(slabItem, 2);
                if (output.isEmpty()) { itemHandler.setStackInSlot(1, result); input.shrink(1); }
                else if (output.getItem() == slabItem && output.getCount() + 2 <= 64) { output.grow(2); input.shrink(1); }
                return;
            }
        }

        if (input.is(Items.STICK) && input.getCount() >= 4) {
            ItemStack arc = new ItemStack(ItemRegistry.WHEEL_ARC_WITHOUT_STICK.get(), 1);
            if (output.isEmpty()) { itemHandler.setStackInSlot(1, arc); input.shrink(4); }
            else if (output.getItem() == ItemRegistry.WHEEL_ARC_WITHOUT_STICK.get() && output.getCount() + 1 <= 64) { output.grow(1); input.shrink(4); }
            return;
        }

        if (input.is(ItemTags.WOODEN_SLABS)) {
            ItemStack sticks = new ItemStack(Items.STICK, 4);
            if (output.isEmpty()) { itemHandler.setStackInSlot(1, sticks); input.shrink(1); }
            else if (output.getItem() == Items.STICK && output.getCount() + 4 <= 64) { output.grow(4); input.shrink(1); }
            return;
        }

        var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.SAWING_TYPE.get());
        for (SawingRecipe recipe : recipes) {
            if (recipe.matches(new SimpleContainer(input), level)) {
                ItemStack result = recipe.assemble(new SimpleContainer(input), level.registryAccess());
                if (output.isEmpty()) { itemHandler.setStackInSlot(1, result); input.shrink(1); }
                else if (ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize()) {
                    output.grow(result.getCount()); input.shrink(1);
                }
                break;
            }
        }
    }

    private boolean canFitOutput() {
        ItemStack input = itemHandler.getStackInSlot(0);
        ItemStack output = itemHandler.getStackInSlot(1);
        if (output.isEmpty()) return isValidInput(input);
        return isOutputCompatible(input, output);
    }

    private boolean isValidInput(ItemStack input) {
        if (input.is(ItemTags.LOGS)) return true;
        if (input.is(ItemTags.PLANKS)) return true;
        if (input.is(ItemTags.WOODEN_SLABS)) return true;
        if (input.is(Items.STICK) && input.getCount() >= 4) return true;
        if (level == null) return false;
        var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.SAWING_TYPE.get());
        for (SawingRecipe recipe : recipes) {
            if (recipe.matches(new SimpleContainer(input), level)) return true;
        }
        return false;
    }

    private boolean isOutputCompatible(ItemStack input, ItemStack output) {
        if (input.is(ItemTags.LOGS)) {
            var key = ForgeRegistries.ITEMS.getKey(input.getItem());
            if (key == null) return false;
            String plankName = key.getPath().replace("_log", "_planks").replace("_wood", "_planks")
                    .replace("_stem", "_planks").replace("_hyphae", "_planks").replace("stripped_", "");
            var plankItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key.getNamespace(), plankName));
            return plankItem != null && output.getItem() == plankItem && output.getCount() < 64;
        }
        if (input.is(ItemTags.PLANKS)) {
            var key = ForgeRegistries.ITEMS.getKey(input.getItem());
            if (key == null) return false;
            String slabName = key.getPath().replace("_planks", "_slab");
            var slabItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key.getNamespace(), slabName));
            return slabItem != null && output.getItem() == slabItem && output.getCount() < 64;
        }
        if (input.is(ItemTags.WOODEN_SLABS)) {
            return output.getItem() == Items.STICK && output.getCount() < 64;
        }
        if (input.is(Items.STICK) && input.getCount() >= 4) {
            return output.getItem() == ItemRegistry.WHEEL_ARC_WITHOUT_STICK.get() && output.getCount() < 64;
        }
        if (level == null) return false;
        var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.SAWING_TYPE.get());
        for (SawingRecipe recipe : recipes) {
            if (recipe.matches(new SimpleContainer(input), level)) {
                ItemStack result = recipe.assemble(new SimpleContainer(input), level.registryAccess());
                return output.getItem() == result.getItem() && output.getCount() < output.getMaxStackSize();
            }
        }
        return false;
    }

    public void drops() {
        if (level == null) return;
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }

    private boolean willOutputFit() {
        ItemStack input = itemHandler.getStackInSlot(0);
        ItemStack output = itemHandler.getStackInSlot(1);
        int resultCount = getResultCount(input);
        return output.isEmpty() || (output.getCount() + resultCount <= output.getMaxStackSize());
    }

    private int getResultCount(ItemStack input) {
        if (input.is(ItemTags.LOGS)) return 6;
        if (input.is(ItemTags.PLANKS)) return 2;
        if (input.is(ItemTags.WOODEN_SLABS)) return 4;
        return 1;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.mech.sawmill");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new SawmillMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putFloat("progress", progress);
        tag.putFloat("perTick", perTick);
        tag.putFloat("inputPower", inputPower);
        tag.putFloat("rotationDirection", rotationDirection);
        tag.putBoolean("lubricated", lubricated);
        tag.putInt("lubricantTimer", lubricantTimer);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        this.progress = tag.getFloat("progress");
        this.perTick = tag.getFloat("perTick");
        this.inputPower = tag.getFloat("inputPower");
        this.rotationDirection = tag.getFloat("rotationDirection");
        this.lubricated = tag.getBoolean("lubricated");
        this.lubricantTimer = tag.getInt("lubricantTimer");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putFloat("progress", progress);
        tag.putFloat("perTick", perTick);
        tag.putFloat("inputPower", inputPower);
        tag.putFloat("rotationDirection", rotationDirection);
        tag.putBoolean("lubricated", lubricated);
        tag.putInt("lubricantTimer", lubricantTimer);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        this.progress = tag.getFloat("progress");
        this.perTick = tag.getFloat("perTick");
        this.inputPower = tag.getFloat("inputPower");
        this.rotationDirection = tag.getFloat("rotationDirection");
        this.lubricated = tag.getBoolean("lubricated");
        this.lubricantTimer = tag.getInt("lubricantTimer");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return lazyItemHandler.cast();

            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> new IItemHandler() {
                    @Override public int getSlots() { return 1; }
                    @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(1); }
                    @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return itemHandler.extractItem(1, amount, simulate); }
                    @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }
                    @Override public int getSlotLimit(int slot) { return 64; }
                    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
                }).cast();
            }

            return LazyOptional.of(() -> new IItemHandler() {
                @Override public int getSlots() { return 1; }
                @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(0); }
                @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
                @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return itemHandler.insertItem(0, stack, simulate); }
                @Override public int getSlotLimit(int slot) { return 64; }
                @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return true; }
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
}