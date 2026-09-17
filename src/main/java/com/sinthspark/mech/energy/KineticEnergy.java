package com.sinthspark.mech.energy;

public class KineticEnergy {
    private final float speed;
    private final float torque;
    private final boolean clockwise;

    public KineticEnergy(float speed, float torque, boolean clockwise) {
        this.speed = speed;
        this.torque = torque;
        this.clockwise = clockwise;
    }

    public float getSpeed() { return speed; }
    public float getTorque() { return torque; }
    public boolean isClockwise() { return clockwise; }

    public boolean isCompatibleWith(KineticEnergy other) {
        return this.clockwise == other.clockwise;
    }

    public float getPower() {
        return speed * torque * 0.1047f;
    }
}
