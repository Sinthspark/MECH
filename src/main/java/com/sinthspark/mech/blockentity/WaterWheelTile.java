package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.energy.KineticNetworkManager;
import com.sinthspark.mech.registration.FluidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public class WaterWheelTile extends BlockEntity {
    public static final float WHEEL_EFFICIENCY = 0.8f;
    public static final float WHEEL_FRICTION = 0.25f;
    public static final float PI = 3.14159f;
    public float power = 0;
    public float rotation = 0;
    public float perTick = 0;
    public boolean isActive = false;

    public WaterWheelTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_WHEEL.get(), pos, state);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if (be instanceof WaterWheelTile tile) {
            if (!level.isClientSide) {
                boolean wasActive = tile.isActive;
                tile.checkWaterAndActivate(level);

                if (wasActive != tile.isActive) {
                    tile.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);

                    KineticNetworkManager.scheduleNetworkUpdate(level, pos);
                }
            } else {
                if (tile.isActive) {
                    tile.rotation = (tile.rotation + tile.perTick) % 360;
                }
            }
        }
    }

    private void checkWaterAndActivate(Level level) {
        boolean hasFlow = false;
        boolean hasSupport = false;
        float flowDirection = 0;

        BlockState state = getBlockState();
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos checkPos = worldPosition.offset(dx, -2, dz);
                if (isWheelFluid(level.getFluidState(checkPos).getType())) {
                    hasSupport = true;
                    break;
                }
            }
            if (hasSupport) break;
        }

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;

            BlockPos checkPos = worldPosition.relative(dir, 2);
            FluidState fluidState = level.getFluidState(checkPos);

            if (isWheelFluid(fluidState.getType())) {
                hasFlow = true;
                float FluidEff = getFluidEfficiency(fluidState.getType());

                if (facing.getAxis() == Direction.Axis.Z) {
                    flowDirection = (dir == Direction.EAST) ? -FluidEff : FluidEff;
                } else {
                    flowDirection = (dir == Direction.SOUTH) ? -FluidEff : FluidEff;
                }
                break;
            }
        }

        this.isActive = hasFlow && hasSupport;

        if (this.isActive) {
            this.perTick = flowDirection * (getSpeed() / 20.0f);
            this.power = getPower();
        } else {
            this.perTick = 0;
            this.power = 0;
        }
    }

    public float getRotation(float partialTick) {
        if (isActive) {
            return (rotation + partialTick * perTick) % 360;
        }
        return rotation;
    }

    public float getSpeed() {
        float FluidEff = getFluidVelocity(level);
        if (FluidEff <= 0) return 0;
        return (120 * FluidEff) / PI;
    }

    public float getPower() {
        float FluidEff = getFluidVelocity(level);
        if (FluidEff <= 0) return 0;
        return (120 * FluidEff * WHEEL_EFFICIENCY) / (PI * WHEEL_FRICTION);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isActive", isActive);
        tag.putFloat("perTick", perTick);
        tag.putFloat("power", power);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.isActive = tag.getBoolean("isActive");
        this.perTick = tag.getFloat("perTick");
        this.power = tag.getFloat("power");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("isActive", isActive);
        tag.putFloat("perTick", perTick);
        tag.putFloat("power", power);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.isActive = tag.getBoolean("isActive");
        this.perTick = tag.getFloat("perTick");
        this.power = tag.getFloat("power");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private boolean isWheelFluid(Fluid fluid) {
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER ||
                fluid == FluidRegistry.CEMENT.source.get() || fluid == FluidRegistry.CEMENT.flowing.get() ||
                fluid == FluidRegistry.CONCRETE.source.get() || fluid == FluidRegistry.CONCRETE.flowing.get();
    }

    private float getFluidEfficiency(Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) return 1.0f;
        if (fluid == FluidRegistry.CEMENT.source.get() || fluid == FluidRegistry.CEMENT.flowing.get()) return 0.5f;
        if (fluid == FluidRegistry.CONCRETE.source.get() || fluid == FluidRegistry.CONCRETE.flowing.get()) return 0.3f;
        return 0.0f;
    }

    public float getFluidVelocity(Level level) {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;
            BlockPos checkPos = worldPosition.relative(dir, 2);
            FluidState fluidState = level.getFluidState(checkPos);

            if (isWheelFluid(fluidState.getType())) {
                return getFluidEfficiency(fluidState.getType());
            }
        }
        return 0;
    }

    @Override
    public void onLoad() {
        super.onLoad();
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
}