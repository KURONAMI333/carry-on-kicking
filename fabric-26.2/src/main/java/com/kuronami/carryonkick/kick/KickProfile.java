package com.kuronami.carryonkick.kick;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Big Walk由来の操作値と、Minecraft物理への変換を集約する。 */
public final class KickProfile {
    // 20tickと4tickの閾値はBig Walk build 24982892の実測値。
    public static final int MAX_CHARGE_TICKS = KickTuning.MAX_CHARGE_TICKS;
    public static final int MIN_KICK_TICKS = KickTuning.MIN_KICK_TICKS;
    // Big Walkの基準Prop（mass=1、launchMultiplier=1）への12m/s impulseを20tick/sへ換算。
    public static final double MAX_SPEED = KickTuning.MAX_SPEED;
    public static final double GRAVITY_PER_TICK = KickTuning.GRAVITY_PER_TICK;
    public static final double SPAWN_DISTANCE = 1.0;

    private KickProfile() {
    }

    public static float normalizedCharge(double chargeTicks) {
        return KickTuning.normalizedCharge(chargeTicks);
    }

    public static Vec3 launchVelocity(float viewPitchDegrees, float viewYawDegrees, long chargeTicks) {
        double speed = launchSpeed(chargeTicks);
        if (speed == 0.0) {
            return Vec3.ZERO;
        }
        float launchPitch = viewPitchDegrees + kickAngleDegrees(viewPitchDegrees);
        float pitchRadians = launchPitch * Mth.DEG_TO_RAD;
        float yawRadians = -viewYawDegrees * Mth.DEG_TO_RAD;
        float horizontal = Mth.cos(pitchRadians);
        Vec3 direction = new Vec3(
                Mth.sin(yawRadians) * horizontal,
                -Mth.sin(pitchRadians),
                Mth.cos(yawRadians) * horizontal);
        return direction.scale(speed);
    }

    public static double launchSpeed(long chargeTicks) {
        return KickTuning.launchSpeed(chargeTicks);
    }

    public static float kickAngleDegrees(float viewPitchDegrees) {
        return KickTuning.kickAngleDegrees(viewPitchDegrees);
    }

    public static double nextVerticalVelocity(double currentVelocity) {
        return KickTuning.nextVerticalVelocity(currentVelocity);
    }
}
