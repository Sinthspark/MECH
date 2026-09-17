package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.OpenHearthFluidHatchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenHearthFluidHatchTile extends BlockEntity implements IFluidHandler {

    private static final int TANK_CAPACITY = 16000;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> this);

    public OpenHearthFluidHatchTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPEN_HEARTH_FLUID_HATCH.get(), pos, state);
    }

    public void onDestroyed() {
        // Контроллеры удаляются извне
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override public int getTanks() { return tank.getTanks(); }
    @Override public @NotNull FluidStack getFluidInTank(int i) { return tank.getFluidInTank(i); }
    @Override public int getTankCapacity(int i) { return tank.getTankCapacity(i); }
    @Override public boolean isFluidValid(int i, @NotNull FluidStack stack) { return tank.isFluidValid(i, stack); }
    @Override public int fill(FluidStack resource, FluidAction action) { return tank.fill(resource, action); }
    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return tank.drain(resource, action); }
    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return tank.drain(maxDrain, action); }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            Direction back = getBlockState().getValue(OpenHearthFluidHatchBlock.FACING);
            if (side == back) {
                return fluidHandler.cast();
            }
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); fluidHandler.invalidate(); }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        tank.readFromNBT(tag.getCompound("tank"));
    }
}
