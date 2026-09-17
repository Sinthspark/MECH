package com.sinthspark.mech.blockentity;

import com.sinthspark.mech.energy.EnergyNetworkManager;
import com.sinthspark.mech.registration.EffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WireTile extends BlockEntity {
    private final int maxTransfer;
    private final int lossPerBlock;
    private final boolean insulated;
    private boolean energized = false;
    private long lastDamageTick;

    public WireTile(BlockPos pos, BlockState state, int maxTransfer, int lossPerBlock, boolean insulated) {
        super(ModBlockEntities.WIRE.get(), pos, state);
        this.maxTransfer = maxTransfer;
        this.lossPerBlock = lossPerBlock;
        this.insulated = insulated;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WireTile tile) {
        if (level.isClientSide) return;
        long currentTick = level.getGameTime();
        if (!tile.insulated && tile.energized && currentTick - tile.lastDamageTick >= 20) {
            tile.lastDamageTick = currentTick;
            AABB area = new AABB(pos).inflate(0.2);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity entity : entities) {
                if (entity instanceof Player player && player.isCreative()) continue;
                entity.hurt(level.damageSources().lightningBolt(), 1.0f);
                entity.addEffect(new MobEffectInstance(EffectRegistry.SHOCK.get(), 200, 0));
            }
        }
    }

    public void setEnergized(boolean energized) {
        if (this.energized == energized) return;
        this.energized = energized;
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean isEnergized() { return energized; }
    public int getMaxTransfer() { return maxTransfer; }
    public int getLossPerBlock() { return lossPerBlock; }
    public boolean isInsulated() { return insulated; }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide)
            EnergyNetworkManager.scheduleNetworkUpdate(level, worldPosition);
        if (level != null && !level.isClientSide) {
            EnergyNetworkManager.onWirePlaced(level, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        Level currentLevel = level;
        super.setRemoved();
        if (currentLevel != null && !currentLevel.isClientSide)
            EnergyNetworkManager.scheduleNetworkUpdate(currentLevel, worldPosition);
        if (level != null && !level.isClientSide) {
            EnergyNetworkManager.onWireRemoved(level, worldPosition);
        }
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("energized", energized);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.energized = tag.getBoolean("energized");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("energized", energized);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.energized = tag.getBoolean("energized");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}