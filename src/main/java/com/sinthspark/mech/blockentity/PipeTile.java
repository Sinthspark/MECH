package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.api.IFluidPipe;
import com.sinthspark.mech.api.IInsulatablePipe;
import com.sinthspark.mech.fluid.FluidNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PipeTile extends BlockEntity implements IFluidHandler, IFluidPipe, IInsulatablePipe {

    private static final int LEAK_RATE = 7;
    private static final int LEAK_INTERVAL = 20;

    private final int capacity;
    private final int transferRate;
    private final float condenseRate;
    private final FluidTank tank;

    private boolean north, south, east, west, up, down;
    private boolean insulated = false;
    private boolean hasFluid = false;
    private float condenseAccum = 0;

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> this);

    public PipeTile(BlockPos pos, BlockState state, int capacity, int transferRate, float condenseRate) {
        super(ModBlockEntities.PIPE.get(), pos, state);
        this.capacity = capacity;
        this.transferRate = transferRate;
        this.condenseRate = condenseRate;
        this.tank = new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
        };
    }

    public static void tick(Level level, BlockPos pos, PipeTile pipe) {
        if (level.isClientSide) return;

        // Урон от касания (пар горячий)
        if (!pipe.isInsulated() && pipe.hasFluid && level.getGameTime() % 20 == 0) {
            AABB blockBounds = new AABB(pos).inflate(0.1);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, blockBounds);
            for (LivingEntity entity : entities) {
                if (entity instanceof Player player && player.isCreative()) continue;
                entity.hurt(level.damageSources().hotFloor(), 2.0f);
            }
        }

        // Конденсация
        if (!pipe.isInsulated() && pipe.tank.getFluidAmount() > 0) {
            pipe.condenseSteam();
        }

        // Утечка из тупика
        if (pipe.tank.getFluidAmount() > 0 && level.getGameTime() % LEAK_INTERVAL == 0) {
            boolean hasAnyConnection = pipe.north || pipe.south || pipe.east || pipe.west || pipe.up || pipe.down;
            if (!hasAnyConnection) {
                int leak = Math.min(LEAK_RATE, pipe.tank.getFluidAmount());
                pipe.tank.drain(leak, FluidAction.EXECUTE);
            }
        }
    }

    private void condenseSteam() {
        FluidStack fluid = tank.getFluid();
        if (fluid.isEmpty()) return;
        int temp = fluid.getFluid().getFluidType().getTemperature();
        if (temp < 373) return;

        condenseAccum += condenseRate / 20f;
        int loss = (int) condenseAccum;
        if (loss > 0) {
            int drained = Math.min(loss, tank.getFluidAmount());
            if (drained > 0) {
                tank.drain(drained, FluidAction.EXECUTE);
                condenseAccum -= drained;
            }
        }
    }

    public void setHasFluid(boolean hasFluid) {
        this.hasFluid = hasFluid;
        setChanged();
    }

    public boolean hasFluid() { return hasFluid; }

    public void updateConnections(boolean n, boolean s, boolean e, boolean w, boolean u, boolean d) {
        this.north = n;
        this.south = s;
        this.east = e;
        this.west = w;
        this.up = u;
        this.down = d;
        setChanged();
    }

    @Override public boolean isInsulated() { return insulated; }
    @Override public void setInsulated(boolean insulated) {
        this.insulated = insulated;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // Добавленные геттеры
    @Override public int getFluidAmount() { return tank.getFluidAmount(); }
    @Override public int getCapacity() { return capacity; }
    public int getTransferRate() { return transferRate; }  // ← ВОТ ЭТОТ МЕТОД БЫЛ ОТСУТСТВУЕТ
    public float getCondenseRate() { return condenseRate; }

    @Override public int getTanks() { return 1; }
    @Override public @NotNull FluidStack getFluidInTank(int tankIndex) { return tank.getFluid(); }
    @Override public int getTankCapacity(int tankIndex) { return capacity; }
    @Override public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) { return tank.isFluidValid(stack); }

    @Override public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        int filled = tank.fill(resource, action);
        if (action == FluidAction.EXECUTE && filled > 0) {
            hasFluid = true;
            setChanged();
        }
        return filled;
    }

    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || tank.getFluidAmount() == 0) return FluidStack.EMPTY;
        if (!tank.getFluid().isFluidEqual(resource)) return FluidStack.EMPTY;
        FluidStack drained = tank.drain(resource.getAmount(), action);
        if (action == FluidAction.EXECUTE && tank.getFluidAmount() == 0) {
            hasFluid = false;
        }
        return drained;
    }

    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || tank.getFluidAmount() == 0) return FluidStack.EMPTY;
        FluidStack drained = tank.drain(maxDrain, action);
        if (action == FluidAction.EXECUTE && tank.getFluidAmount() == 0) {
            hasFluid = false;
        }
        return drained;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putBoolean("north", north);
        tag.putBoolean("south", south);
        tag.putBoolean("east", east);
        tag.putBoolean("west", west);
        tag.putBoolean("up", up);
        tag.putBoolean("down", down);
        tag.putBoolean("insulated", insulated);
        tag.putBoolean("hasFluid", hasFluid);
        tag.putFloat("condenseAccum", condenseAccum);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        north = tag.getBoolean("north");
        south = tag.getBoolean("south");
        east = tag.getBoolean("east");
        west = tag.getBoolean("west");
        up = tag.getBoolean("up");
        down = tag.getBoolean("down");
        insulated = tag.getBoolean("insulated");
        hasFluid = tag.getBoolean("hasFluid");
        condenseAccum = tag.getFloat("condenseAccum");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putBoolean("north", north);
        tag.putBoolean("south", south);
        tag.putBoolean("east", east);
        tag.putBoolean("west", west);
        tag.putBoolean("up", up);
        tag.putBoolean("down", down);
        tag.putBoolean("insulated", insulated);
        tag.putBoolean("hasFluid", hasFluid);
        tag.putFloat("condenseAccum", condenseAccum);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        tank.readFromNBT(tag.getCompound("tank"));
        north = tag.getBoolean("north");
        south = tag.getBoolean("south");
        east = tag.getBoolean("east");
        west = tag.getBoolean("west");
        up = tag.getBoolean("up");
        down = tag.getBoolean("down");
        insulated = tag.getBoolean("insulated");
        hasFluid = tag.getBoolean("hasFluid");
        condenseAccum = tag.getFloat("condenseAccum");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            FluidNetworkManager.scheduleNetworkUpdate(level, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            FluidNetworkManager.scheduleNetworkUpdate(level, worldPosition);
        }
    }
}