package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.blocks.SteamTurbineBlock;
import com.sinthspark.mech.registration.FluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
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

public class SteamTurbineTile extends BlockEntity implements IFluidHandler {

    private static final int STEAM_TANK_CAPACITY = 8000;
    private static final int MAX_RPM = 3600;
    private static final int STEAM_PER_TICK = 15;

    public long totalTicks = 0;
    public float rotationAngle = 0;

    private final FluidTank steamTank = new FluidTank(STEAM_TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public int rpm = 0;
    public float torque = 0;
    private int warmUp = 0;

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> this);

    public SteamTurbineTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_TURBINE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamTurbineTile tile) {
        if (level.isClientSide) return;

        boolean wasActive = tile.getBlockState().getValue(SteamTurbineBlock.ACTIVE);
        boolean isActive = false;
        tile.totalTicks++;

        FluidStack fluid = tile.steamTank.getFluid();
        float multiplier = 1.0f;
        if (!fluid.isEmpty() && fluid.getFluid() == FluidRegistry.DRY_STEAM.source.get()) {
            multiplier = 1.5f; // сухой пар крутит на 50% быстрее
        }

        if (tile.steamTank.getFluidAmount() >= STEAM_PER_TICK) {
            if (tile.warmUp < 100) {
                tile.warmUp++;
            }
            tile.steamTank.drain(STEAM_PER_TICK, FluidAction.EXECUTE);
            tile.torque = 1.0f;
            isActive = true;
        } else {
            if (tile.warmUp > 0) {
                tile.warmUp--;
            }
            tile.torque = 0;
        }

        if (tile.warmUp > 0) {
            tile.rpm = (int) (MAX_RPM * (tile.warmUp / 100f) * multiplier);
        } else {
            tile.rpm = 0;
        }

        if (wasActive != isActive) {
            level.setBlock(pos, state.setValue(SteamTurbineBlock.ACTIVE, isActive), 3);
        }

        setChanged(level, pos, state);
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public boolean isRunning() { return warmUp > 0; }

    @Override public int getTanks() { return 1; }
    @Override public @NotNull FluidStack getFluidInTank(int tank) { return steamTank.getFluid(); }
    @Override public int getTankCapacity(int tank) { return STEAM_TANK_CAPACITY; }
    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return stack.getFluid() == FluidRegistry.STEAM.source.get()
                || stack.getFluid() == FluidRegistry.DRY_STEAM.source.get();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.getFluid() != FluidRegistry.STEAM.source.get()
                && resource.getFluid() != FluidRegistry.DRY_STEAM.source.get()) {
            return 0;
        }
        return steamTank.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            Direction front = getBlockState().getValue(SteamTurbineBlock.FACING).getOpposite();
            if (side == front) {
                return fluidHandler.cast();
            }
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() { super.invalidateCaps(); fluidHandler.invalidate(); }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("steam", steamTank.writeToNBT(new CompoundTag()));
        tag.putInt("rpm", rpm);
        tag.putFloat("torque", torque);
        tag.putInt("warmUp", warmUp);
        tag.putLong("totalTicks", totalTicks);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        steamTank.readFromNBT(tag.getCompound("steam"));
        rpm = tag.getInt("rpm");
        torque = tag.getFloat("torque");
        warmUp = tag.getInt("warmUp");
        totalTicks = tag.getLong("totalTicks");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("steam", steamTank.writeToNBT(new CompoundTag()));
        tag.putInt("rpm", rpm);
        tag.putFloat("torque", torque);
        tag.putInt("warmUp", warmUp);
        tag.putLong("totalTicks", totalTicks);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        steamTank.readFromNBT(tag.getCompound("steam"));
        rpm = tag.getInt("rpm");
        torque = tag.getFloat("torque");
        warmUp = tag.getInt("warmUp");
        totalTicks = tag.getLong("totalTicks");
    }
}