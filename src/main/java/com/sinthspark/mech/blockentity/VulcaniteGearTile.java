package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.energy.KineticNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class VulcaniteGearTile {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    public float rotationDirection = 1.0f;
    public float rotation = 0;
    private int rotationLogCounter = 0;
    public float perTick = 5;
    public static final float MOMENT_OF_INERTIA = 0.5f;
    public static final float PI = 3.14159f;
    public boolean isActive = false;
    public static final float EFFICIENCY = 0.9f;
    public static final float FRICTION = 2.0f;
    public float inputPower = 0;

    public boolean lubricated = false;
    public int lubricantTimer = 0;
    public static final float LUBRICANT_REDUCTION = 0.3f;

    public VulcaniteGearTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VULCANITE_GEAR.get(), pos, state);
    }

    public float getFriction() {
        if (lubricated) return FRICTION * (1 - LUBRICANT_REDUCTION);
        return FRICTION;
    }

    public void applyLubricant() {
        lubricated = true;
        lubricantTimer = 6000;
        setChanged();
        // Синк клиенту: Jade HUD и будущий рендер читают lubricated с клиента.
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if (!(be instanceof VulcaniteGearTile tile)) return;

        if (level.isClientSide) {
            if (tile.isActive) {
                tile.rotation = (tile.rotation + tile.perTick) % 360;
                if (tile.rotationLogCounter++ % 20 == 0) {
                    LOGGER.info("[GearClient] tick: rotation={} perTick={} isActive={}", tile.rotation, tile.perTick, tile.isActive);
                }
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

    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isActive", isActive);
        tag.putFloat("perTick", perTick);
        tag.putFloat("inputPower", inputPower);
        tag.putFloat("rotationDirection", rotationDirection);
        tag.putBoolean("lubricated", lubricated);
        tag.putInt("lubricantTimer", lubricantTimer);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.isActive = tag.getBoolean("isActive");
        this.perTick = tag.getFloat("perTick");
        this.inputPower = tag.getFloat("inputPower");
        this.rotationDirection = tag.getFloat("rotationDirection");
        this.lubricated = tag.getBoolean("lubricated");
        this.lubricantTimer = tag.getInt("lubricantTimer");

        // Сбрасываем активность при загрузке
        // Сеть пересчитает при следующем тике
        this.isActive = false;
        this.inputPower = 0;
        this.perTick = 0;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("isActive", isActive);
        tag.putFloat("perTick", perTick);
        tag.putFloat("inputPower", inputPower);
        tag.putFloat("rotationDirection", rotationDirection);
        tag.putBoolean("lubricated", lubricated);
        tag.putInt("lubricantTimer", lubricantTimer);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        LOGGER.info("[GearClient] handleUpdateTag isActive={} perTick={} client={}",
                tag.getBoolean("isActive"), tag.getFloat("perTick"), level == null ? "null" : level.isClientSide());
        super.handleUpdateTag(tag);
        this.isActive = tag.getBoolean("isActive");
        this.perTick = tag.getFloat("perTick");
        this.inputPower = tag.getFloat("inputPower");
        this.rotationDirection = tag.getFloat("rotationDirection");
        this.lubricated = tag.getBoolean("lubricated");
        this.lubricantTimer = tag.getInt("lubricantTimer");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public float getRotation(float partialTick) {
        if (isActive) {
            return (rotation + partialTick * perTick) % 360;
        }
        return rotation;
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