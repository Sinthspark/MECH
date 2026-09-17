package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.OpenHearthControllerBlock;
import com.sinthspark.mech.blocks.OpenHearthFluidHatchBlock;
import com.sinthspark.mech.registration.BlockRegistry;
import com.sinthspark.mech.registration.ItemRegistry;
import com.sinthspark.mech.screen.OpenHearthMenu;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenHearthControllerTile extends BlockEntity implements MenuProvider {

    private static final int COOK_TIME = 3600;
    private static final int STEAM_PER_TICK = 25;

    private boolean structureValid = false;

    public int progress = 0;
    public int maxProgress = COOK_TIME;
    public int totalTankCapacity = 0;
    public int currentSteam = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot < 4 && getStackInSlot(0).isEmpty()) {
                progress = 0;
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.getItem() == ItemRegistry.CAST_IRON_INGOT.get();
                case 1 -> stack.is(Items.IRON_INGOT) || stack.is(Items.IRON_NUGGET) || stack.is(Items.IRON_BLOCK);
                case 2 -> stack.is(Items.IRON_ORE) || stack.is(Items.DEEPSLATE_IRON_ORE) || stack.is(Items.RAW_IRON);
                case 3 -> stack.getItem() == BlockRegistry.LIMESTONE.get().asItem();
                default -> false;
            };
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> inventory);

    public OpenHearthControllerTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPEN_HEARTH_CONTROLLER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OpenHearthControllerTile tile) {
        if (level.isClientSide) return;

        if (level.getGameTime() % 100 == 0 || !tile.structureValid) {
            tile.structureValid = tile.checkStructure();
        }

        tile.currentSteam = tile.collectSteam();
        tile.totalTankCapacity = tile.getTotalCapacity();

        boolean wasActive = tile.getBlockState().getValue(OpenHearthControllerBlock.ACTIVE);
        boolean isActive = tile.progress > 0 && tile.structureValid;

        if (!tile.structureValid) {
            tile.progress = 0;
            if (wasActive) {
                level.setBlock(pos, tile.getBlockState().setValue(OpenHearthControllerBlock.ACTIVE, false), 3);
            }
            return;
        }

        ItemStack castIron = tile.inventory.getStackInSlot(0);
        ItemStack scrap = tile.inventory.getStackInSlot(1);
        ItemStack ore = tile.inventory.getStackInSlot(2);
        ItemStack limestone = tile.inventory.getStackInSlot(3);

        if (!castIron.isEmpty() && !scrap.isEmpty() && !ore.isEmpty() && !limestone.isEmpty()) {
            if (tile.currentSteam >= STEAM_PER_TICK) {
                ItemStack steelSlot = tile.inventory.getStackInSlot(4);
                ItemStack slagSlot = tile.inventory.getStackInSlot(5);

                if ((steelSlot.isEmpty() || steelSlot.getCount() < steelSlot.getMaxStackSize())
                        && (slagSlot.isEmpty() || slagSlot.getCount() < slagSlot.getMaxStackSize())) {

                    tile.drainSteamFromHatches();
                    tile.progress++;
                    isActive = true;

                    if (tile.progress >= COOK_TIME) {
                        tile.inventory.extractItem(0, 1, false);
                        tile.inventory.extractItem(1, 1, false);
                        tile.inventory.extractItem(2, 1, false);
                        tile.inventory.extractItem(3, 1, false);

                        if (steelSlot.isEmpty()) {
                            tile.inventory.setStackInSlot(4, new ItemStack(ItemRegistry.STEEL_INGOT.get(), 1));
                        } else {
                            steelSlot.grow(1);
                        }

                        if (slagSlot.isEmpty()) {
                            tile.inventory.setStackInSlot(5, new ItemStack(ItemRegistry.SLAG.get(), 1));
                        } else {
                            slagSlot.grow(1);
                        }

                        tile.progress = 0;
                        isActive = false;
                    }
                } else {
                    tile.progress = 0;
                }
            }
        } else {
            tile.progress = 0;
        }

        if (wasActive != isActive) {
            level.setBlock(pos, tile.getBlockState().setValue(OpenHearthControllerBlock.ACTIVE, isActive), 3);
        }

        setChanged(level, pos, state);
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private List<OpenHearthFluidHatchTile> findHatches() {
        List<OpenHearthFluidHatchTile> hatches = new ArrayList<>();

        if (level == null) {
            return hatches;
        }

        Direction facing = getBlockState().getValue(OpenHearthControllerBlock.FACING);
        Direction forward = facing.getOpposite();
        Direction right = facing.getCounterClockWise();

        for (int dy = -1; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = 0; dz <= 2; dz++) {
                    BlockPos checkPos = worldPosition.offset(
                            dx * right.getStepX() + dz * forward.getStepX(),
                            dy,
                            dx * right.getStepZ() + dz * forward.getStepZ()
                    );
                    if (checkPos.equals(worldPosition)) continue;

                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be instanceof OpenHearthFluidHatchTile hatch) {
                        hatches.add(hatch);
                    }
                }
            }
        }
        if (hatches.size() > 4) {
            return new ArrayList<>(hatches.subList(0, 4));
        }
        return hatches;
    }

    private int collectSteam() {
        int total = 0;
        for (OpenHearthFluidHatchTile hatch : findHatches()) {
            total += hatch.getTank().getFluidAmount();
        }
        return total;
    }

    private int getTotalCapacity() {
        int total = 0;
        for (OpenHearthFluidHatchTile hatch : findHatches()) {
            total += hatch.getTank().getCapacity();
        }
        return total;
    }

    private void drainSteamFromHatches() {
        int remaining = OpenHearthControllerTile.STEAM_PER_TICK;
        for (OpenHearthFluidHatchTile hatch : findHatches()) {
            if (remaining <= 0) break;
            FluidStack drained = hatch.getTank().drain(remaining, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                remaining -= drained.getAmount();
            }
        }
    }

    private boolean checkStructure() {

        if (level == null) {
            return false;
        }

        Direction facing = getBlockState().getValue(OpenHearthControllerBlock.FACING);
        BlockPos controllerPos = worldPosition;
        Direction forward = facing.getOpposite();
        Direction right = facing.getCounterClockWise();
        int hatchCount = 0;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = 0; dz <= 2; dz++) {
                    BlockPos checkPos = controllerPos.offset(
                            dx * right.getStepX() + dz * forward.getStepX(),
                            dy,
                            dx * right.getStepZ() + dz * forward.getStepZ()
                    );

                    if (checkPos.equals(controllerPos)) continue;

                    boolean isBrick = level.getBlockState(checkPos).getBlock() == BlockRegistry.OPEN_HEARTH_BRICKS.get();
                    boolean isHatch = level.getBlockState(checkPos).getBlock() instanceof OpenHearthFluidHatchBlock;
                    boolean isAir = level.getBlockState(checkPos).isAir();

                    boolean isInterior = (dx >= -1 && dx <= 1) && (dy >= 0 && dy <= 1) && (dz == 1);

                    if (isInterior) {
                        if (!isAir) return false;
                    } else {
                        if (!(isBrick || isHatch)) return false;
                    }

                    if (isHatch) {
                        hatchCount++;

                        Direction hatchFront = level.getBlockState(checkPos).getValue(OpenHearthFluidHatchBlock.FACING);
                        BlockPos frontPos = checkPos.relative(hatchFront);

                        boolean frontIsInside = false;
                        for (int dy2 = -1; dy2 <= 2; dy2++) {
                            for (int dx2 = -2; dx2 <= 2; dx2++) {
                                for (int dz2 = 0; dz2 <= 2; dz2++) {
                                    BlockPos structPos = controllerPos.offset(
                                            dx2 * right.getStepX() + dz2 * forward.getStepX(),
                                            dy2,
                                            dx2 * right.getStepZ() + dz2 * forward.getStepZ()
                                    );
                                    if (frontPos.equals(structPos)) {
                                        frontIsInside = true;
                                        break;
                                    }
                                }
                                if (frontIsInside) break;
                            }
                            if (frontIsInside) break;
                        }

                        if (frontIsInside) return false;
                    }
                }
            }
        }

        return hatchCount <= 4;
    }

    public boolean isStructureValid() { return structureValid; }
    public void onDestroyed() { structureValid = false; }
    public IItemHandler getInventory() { return inventory; }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return itemHandler.cast();
            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> new IItemHandler() {
                    @Override public int getSlots() { return 2; }
                    @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(s + 4); }
                    @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return inventory.extractItem(s + 4, a, sim); }
                    @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return stack; }
                    @Override public int getSlotLimit(int s) { return 64; }
                    @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return false; }
                }).cast();
            }
            return LazyOptional.of(() -> new IItemHandler() {
                @Override public int getSlots() { return 4; }
                @Override public @NotNull ItemStack getStackInSlot(int s) { return inventory.getStackInSlot(s); }
                @Override public @NotNull ItemStack extractItem(int s, int a, boolean sim) { return ItemStack.EMPTY; }
                @Override public @NotNull ItemStack insertItem(int s, @NotNull ItemStack stack, boolean sim) { return inventory.insertItem(s, stack, sim); }
                @Override public int getSlotLimit(int s) { return 64; }
                @Override public boolean isItemValid(int s, @NotNull ItemStack stack) { return s < 4; }
            }).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); itemHandler.invalidate(); }
    @Override public @NotNull Component getDisplayName() { return Component.translatable("block.mech.open_hearth_controller"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return new OpenHearthMenu(id, inv, this); }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putBoolean("structureValid", structureValid);
        tag.putInt("maxProgress", maxProgress);
        tag.putInt("totalTankCapacity", totalTankCapacity);
        tag.putInt("currentSteam", currentSteam);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        structureValid = tag.getBoolean("structureValid");
        maxProgress = tag.getInt("maxProgress");
        totalTankCapacity = tag.getInt("totalTankCapacity");
        currentSteam = tag.getInt("currentSteam");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putBoolean("structureValid", structureValid);
        tag.putInt("totalTankCapacity", totalTankCapacity);
        tag.putInt("currentSteam", currentSteam);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
        structureValid = tag.getBoolean("structureValid");
        totalTankCapacity = tag.getInt("totalTankCapacity");
        currentSteam = tag.getInt("currentSteam");
    }
}