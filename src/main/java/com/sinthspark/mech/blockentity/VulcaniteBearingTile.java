package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.energy.KineticNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class VulcaniteBearingTile extends BlockEntity {
    public static final float MOMENT_OF_INERTIA = 0.05f;
    public static final float PI = 3.14159f;
    public static final float BEARING_EFFICIENCY = 0.9f;
    public static final float BEARING_FRICTION = 8.0f;
    public float inputPower = 0;
    public float outputPower = 0;
    public float rotation = 0;
    public float perTick = 5.0f;
    public boolean isActive = false;
    public boolean lubricated = false;
    public int lubricantTimer = 0;
    public static final float LUBRICANT_REDUCTION = 0.3f;

    public VulcaniteBearingTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VULCANITE_BEARING.get(), pos, state);
    }

    public float getFriction() {
        if (lubricated) return BEARING_FRICTION * (1 - LUBRICANT_REDUCTION);
        return BEARING_FRICTION;
    }

    public void applyLubricant() {
        lubricated = true;
        lubricantTimer = 6000;
        setChanged();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if (!(be instanceof VulcaniteBearingTile tile)) return;

        if (level.isClientSide) {
            if (tile.isActive) {
                tile.rotation = (tile.rotation + tile.perTick) % 360;
            }
            return;
        }

        if (tile.lubricated) {
            tile.lubricantTimer--;
            if (tile.lubricantTimer <= 0) {
                tile.lubricated = false;
                tile.setChanged();
            }
        }
    }

    public float getRotation(float partialTick) {
        if (isActive) {
            return (rotation + partialTick * perTick) % 360;
        }
        return rotation;
    }

    public float getPower() {
        if (inputPower <= 0) return 0;
        float friction = getFriction();
        return Math.max(0, inputPower * BEARING_EFFICIENCY - friction);
    }

    public float getSpeed() {
        if (inputPower <= 0) return 0;
        float friction = getFriction();
        float numerator = 60 * inputPower;
        float denominator = BEARING_EFFICIENCY * MOMENT_OF_INERTIA * PI * friction;
        if (denominator <= 0) return 0;
        return (float) Math.sqrt(numerator / denominator);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isActive", isActive);
        tag.putFloat("perTick", perTick);
        tag.putFloat("inputPower", inputPower);
        tag.putFloat("outputPower", outputPower);
        tag.putBoolean("lubricated", lubricated);
        tag.putInt("lubricantTimer", lubricantTimer);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.isActive = tag.getBoolean("isActive");
        this.perTick = tag.getFloat("perTick");
        this.inputPower = tag.getFloat("inputPower");
        this.outputPower = tag.getFloat("outputPower");
        this.lubricated = tag.getBoolean("lubricated");
        this.lubricantTimer = tag.getInt("lubricantTimer");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("isActive", isActive);
        tag.putFloat("perTick", perTick);
        tag.putFloat("inputPower", inputPower);
        tag.putFloat("outputPower", outputPower);
        tag.putBoolean("lubricated", lubricated);
        tag.putInt("lubricantTimer", lubricantTimer);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.isActive = tag.getBoolean("isActive");
        this.perTick = tag.getFloat("perTick");
        this.inputPower = tag.getFloat("inputPower");
        this.outputPower = tag.getFloat("outputPower");
        this.lubricated = tag.getBoolean("lubricated");
        this.lubricantTimer = tag.getInt("lubricantTimer");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
