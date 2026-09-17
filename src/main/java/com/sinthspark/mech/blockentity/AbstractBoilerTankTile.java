package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.api.IBoilerTank;
import com.sinthspark.mech.api.IHeatedBlock;
import com.sinthspark.mech.api.IHeatSource;
import com.sinthspark.mech.registration.FluidRegistry;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Shared implementation for all boiler tiers. */
public abstract class AbstractBoilerTankTile extends BlockEntity implements IHeatedBlock, IFluidHandler, IBoilerTank {
    private float steamAccum;
    public int temperature;
    public WorkingFluid currentWorkingFluid = WorkingFluid.WATER;
    private final FluidTank waterTank;
    private final FluidTank steamTank;
    private final LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> new WaterInputHandler());
    private final LazyOptional<IFluidHandler> steamOutputCapability = LazyOptional.of(() -> new SteamOutputHandler());

    public FluidTank getWaterTank() {
        return waterTank;
    }

    public FluidTank getSteamTank() {
        return steamTank;
    }

    protected AbstractBoilerTankTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int tankCapacity) {
        super(type, pos, state);
        this.waterTank = createTank(tankCapacity);
        this.steamTank = createTank(tankCapacity);
    }

    private FluidTank createTank(int capacity) {
        return new FluidTank(capacity) {
            @Override protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        };
    }

    public enum WorkingFluid {
        WATER(100, 1.0f, 100), CREOSOTE(200, 1.5f, 150);
        public final int boilingTemp;
        public final float energyMultiplier;
        public final int steamPerMb;
        WorkingFluid(int boilingTemp, float energyMultiplier, int steamPerMb) {
            this.boilingTemp = boilingTemp;
            this.energyMultiplier = energyMultiplier;
            this.steamPerMb = steamPerMb;
        }
    }

    protected abstract int getMaxSteamPerTick();
    protected abstract float getExplosionPower();
    protected boolean damagesPlayersOnExplosion() { return false; }

    private final class WaterInputHandler implements IFluidHandler {
        @Override public int getTanks() { return 1; }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return waterTank.getFluid(); }
        @Override public int getTankCapacity(int tank) { return waterTank.getCapacity(); }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return isBoilerInput(stack); }
        @Override public int fill(FluidStack resource, FluidAction action) { return AbstractBoilerTankTile.this.fill(resource, action); }
        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }

    private final class SteamOutputHandler implements IFluidHandler {
        @Override public int getTanks() { return 1; }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return steamTank.getFluid(); }
        @Override public int getTankCapacity(int tank) { return steamTank.getCapacity(); }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !resource.isFluidEqual(steamTank.getFluid())) return FluidStack.EMPTY;
            return steamTank.drain(resource.getAmount(), action);
        }
        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return steamTank.drain(maxDrain, action); }
    }

    private boolean isBoilerInput(FluidStack stack) {
        return stack.getFluid() == Fluids.WATER || stack.getFluid() == FluidRegistry.CREOSOTE.source.get();
    }

    public static void tick(Level level, BlockPos pos, AbstractBoilerTankTile tile) {
        if (level.isClientSide) return;
        if (tile.temperature >= 100) {
            AABB aabb = new AABB(pos).inflate(0.1);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                if (entity.hurtTime == 0) entity.hurt(level.damageSources().hotFloor(), 2.0f);
            }
        }
        BlockEntity below = level.getBlockEntity(pos.below());
        if (!(below instanceof IHeatSource heatSource)) return;
        tile.temperature = heatSource.getTemperature();
        tile.currentWorkingFluid = tile.detectWorkingFluid();
        if (tile.temperature >= tile.currentWorkingFluid.boilingTemp) {
            int steamPerTick = Math.min((int) ((tile.temperature - tile.currentWorkingFluid.boilingTemp) / 200f) + 1, tile.getMaxSteamPerTick());
            FluidStack input = tile.waterTank.getFluid();
            if (!input.isEmpty() && tile.steamTank.getFluidAmount() < tile.steamTank.getCapacity()) {
                FluidStack drained = tile.waterTank.drain(Math.min(steamPerTick, input.getAmount()), IFluidHandler.FluidAction.EXECUTE);
                if (!drained.isEmpty()) {
                    tile.steamAccum += drained.getAmount() * tile.currentWorkingFluid.energyMultiplier;
                    int steamAmount = Math.min((int) tile.steamAccum, tile.steamTank.getCapacity() - tile.steamTank.getFluidAmount());
                    if (steamAmount > 0) {
                        int filled = tile.steamTank.fill(new FluidStack(FluidRegistry.STEAM.source.get(), steamAmount), IFluidHandler.FluidAction.EXECUTE);
                        tile.steamAccum -= filled;
                        if (filled > 0) tile.setChanged();
                    }
                }
            }
        }
        if (tile.steamTank.getFluidAmount() >= tile.steamTank.getCapacity() && tile.temperature >= tile.currentWorkingFluid.boilingTemp && !tile.waterTank.isEmpty()) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tile.getExplosionPower(), true, Level.ExplosionInteraction.TNT);
            if (tile.damagesPlayersOnExplosion()) {
                double radius = 4.0;
                for (Player player : level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(radius))) {
                    double dist = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    if (dist < radius * radius) player.hurt(level.damageSources().explosion(null), 3.0f);
                }
            }
            level.destroyBlock(pos, false);
            level.destroyBlock(pos.below(), false);
        }
    }

    private WorkingFluid detectWorkingFluid() {
        FluidStack fluid = waterTank.getFluid();
        if (fluid.isEmpty() || fluid.getFluid() == Fluids.WATER) return WorkingFluid.WATER;
        if (fluid.getFluid() == FluidRegistry.CREOSOTE.source.get()) return WorkingFluid.CREOSOTE;
        return WorkingFluid.WATER;
    }

    @Override public float getTemperature() { return temperature; }
    @Override public int fillWater(FluidStack resource, FluidAction action) { return fill(resource, action); }
    @Override public boolean hasWater() { return !waterTank.isEmpty(); }
    public int drainSteam(int maxAmount) {
        int amount = Math.min(maxAmount, steamTank.getFluidAmount());
        if (amount > 0) { steamTank.drain(amount, IFluidHandler.FluidAction.EXECUTE); setChanged(); }
        return amount;
    }
    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == Direction.DOWN) return steamOutputCapability.cast();
            return fluidCapability.cast();
        }
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); fluidCapability.invalidate(); steamOutputCapability.invalidate(); }
    @Override public int getTanks() { return 1; }
    @Override public @NotNull FluidStack getFluidInTank(int tank) { return steamTank.getFluid(); }
    @Override public int getTankCapacity(int tank) { return steamTank.getCapacity(); }
    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return isBoilerInput(stack); }
    @Override public int fill(FluidStack resource, FluidAction action) { if (!isBoilerInput(resource)) return 0; int filled = waterTank.fill(resource, action); if (filled > 0) currentWorkingFluid = detectWorkingFluid(); return filled; }
    @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { if (!resource.isFluidEqual(steamTank.getFluid())) return FluidStack.EMPTY; return steamTank.drain(resource.getAmount(), action); }
    @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return steamTank.drain(maxDrain, action); }
    @Override public @NotNull CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); tag.put("water", waterTank.writeToNBT(new CompoundTag())); tag.put("steam", steamTank.writeToNBT(new CompoundTag())); tag.putInt("temperature", temperature); tag.putFloat("steamAccum", steamAccum); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag) { super.handleUpdateTag(tag); waterTank.readFromNBT(tag.getCompound("water")); steamTank.readFromNBT(tag.getCompound("steam")); temperature = tag.getInt("temperature"); steamAccum = tag.getFloat("steamAccum"); currentWorkingFluid = detectWorkingFluid(); }
    @Override protected void saveAdditional(@NotNull CompoundTag tag) { super.saveAdditional(tag); tag.put("water", waterTank.writeToNBT(new CompoundTag())); tag.put("steam", steamTank.writeToNBT(new CompoundTag())); tag.putInt("temperature", temperature); tag.putFloat("steamAccum", steamAccum); }
    @Override public void load(@NotNull CompoundTag tag) { super.load(tag); waterTank.readFromNBT(tag.getCompound("water")); steamTank.readFromNBT(tag.getCompound("steam")); temperature = tag.getInt("temperature"); steamAccum = tag.getFloat("steamAccum"); currentWorkingFluid = detectWorkingFluid(); }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
