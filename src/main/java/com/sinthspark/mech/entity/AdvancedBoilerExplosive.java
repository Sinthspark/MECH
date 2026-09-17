package com.sinthspark.mech.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;

public class AdvancedBoilerExplosive extends PrimedTnt {

    private static final int FUSE = 0;

    public AdvancedBoilerExplosive(EntityType<? extends PrimedTnt> type, Level level) {
        super(type, level);
    }

    public AdvancedBoilerExplosive(Level level, double x, double y, double z) {
        super(level, x, y, z, null);
        this.setFuse(FUSE);
    }

    @Override
    @SuppressWarnings("resource")
    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(0, 0, 0);
        }

        if (this.getFuse() <= 0) {
            if (!this.level().isClientSide) {
                this.boilerExplode();
            }
            this.discard();
        } else {
            this.setFuse(this.getFuse() - 1);
        }
    }

    @SuppressWarnings("resource")
    private void boilerExplode() {
        this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                4.5f, true, Level.ExplosionInteraction.TNT);
    }
}
