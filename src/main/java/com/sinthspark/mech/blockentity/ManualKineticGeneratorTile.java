package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.energy.KineticNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ManualKineticGeneratorTile extends BlockEntity {
    public static final float EFFICIENCY = 0.8f;
    public static final float FRICTION = 20.0f;
    public float rotation = 0;
    public float perTick = 5.0f;
    public boolean isActive = false;
    public boolean isBeingCranked = false;
    private int crankTimer = 0;
    public boolean wasActivatedByPlayer = false;

    public ManualKineticGeneratorTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANUAL_KINETIC_GENERATOR.get(), pos, state);
    }

    public void startCranking() {
        System.out.println("[Generator] Крутят генератор!");
        this.isBeingCranked = true;
        this.wasActivatedByPlayer = true;
        setChanged();
    }

    public void continueCranking() {
        this.isBeingCranked = true;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if (!(be instanceof ManualKineticGeneratorTile tile)) return;

        if (level.isClientSide) {
            if (tile.isActive) {
                tile.rotation = (tile.rotation + tile.perTick) % 360;
            }
            return;
        }

        boolean wasActive = tile.isActive;

        if (tile.isBeingCranked) {
            System.out.println("[Generator] Обработка кручения на " + pos);
            Player nearestPlayer = level.getNearestPlayer(
                    pos.getX(), pos.getY(), pos.getZ(), 2.5, false
            );
            if (nearestPlayer != null &&
                    nearestPlayer.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 6.25) {
                tile.crankTimer = 20;
                tile.perTick = 5.0f;
                System.out.println("[Generator] Игрок рядом, crankTimer = 20");
            } else {
                System.out.println("[Generator] Игрок НЕ рядом");
            }
            tile.isBeingCranked = false;
        }

        if (tile.crankTimer > 0) {
            tile.isActive = true;
            tile.crankTimer--;
            tile.perTick = 5.0f;
        } else {
            tile.isActive = false;
            tile.perTick = 0;
        }

        if (wasActive != tile.isActive) {
            System.out.println("[Generator] Состояние: " + wasActive + " -> " + tile.isActive);
            tile.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            KineticNetworkManager.scheduleNetworkUpdate(level, pos);
        }
    }

    public float getRotation(float partialTick) {
        if (isActive) {
            return (rotation + partialTick * perTick) % 360;
        }
        return rotation;
    }

    public float getPower() {
        return perTick * EFFICIENCY * FRICTION;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isActive", isActive);
        tag.putFloat("perTick", perTick);
        tag.putInt("crankTimer", crankTimer);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.isActive = false; // Всегда сбрасываем при загрузке
        this.perTick = 0;
        this.crankTimer = 0;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("isActive", isActive);
        tag.putFloat("perTick", perTick);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.isActive = tag.getBoolean("isActive");
        this.perTick = tag.getFloat("perTick");
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