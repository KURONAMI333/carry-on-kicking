package com.kuronami.carryonkick.kick;

/** Minecraftクラスに依存しないBig Walk実測値と補間。 */
public final class KickTuning {
    private static final CurveKey[] ANGLE_CURVE = {
            new CurveKey(-90.057014, -20.001236, 1.6944224),
            new CurveKey(-82.72839, 0.055137634, -0.007750664),
            new CurveKey(-78.613075, -10.082471, -0.009460482),
            new CurveKey(-47.332146, -10.161989, -0.014554806),
            new CurveKey(0.43534088, -44.95631, -0.008886235),
            new CurveKey(89.99768, -44.993217, 0.0)
    };

    public static final int MAX_CHARGE_TICKS = 20;
    public static final int MIN_KICK_TICKS = 4;
    public static final double MAX_SPEED = 0.6;
    public static final double GRAVITY_PER_TICK = 9.81 / 400.0;

    private KickTuning() {
    }

    public static float normalizedCharge(double chargeTicks) {
        return (float) Math.max(0.0, Math.min(chargeTicks / MAX_CHARGE_TICKS, 1.0));
    }

    public static double launchSpeed(long chargeTicks) {
        return chargeTicks < MIN_KICK_TICKS ? 0.0 : normalizedCharge(chargeTicks) * MAX_SPEED;
    }

    public static float kickAngleDegrees(float viewPitchDegrees) {
        if (viewPitchDegrees <= ANGLE_CURVE[0].time()) {
            return (float) ANGLE_CURVE[0].value();
        }
        CurveKey last = ANGLE_CURVE[ANGLE_CURVE.length - 1];
        if (viewPitchDegrees >= last.time()) {
            return (float) last.value();
        }
        for (int index = 1; index < ANGLE_CURVE.length; index++) {
            CurveKey right = ANGLE_CURVE[index];
            if (viewPitchDegrees <= right.time()) {
                CurveKey left = ANGLE_CURVE[index - 1];
                double duration = right.time() - left.time();
                double position = (viewPitchDegrees - left.time()) / duration;
                double squared = position * position;
                double cubed = squared * position;
                double h00 = 2.0 * cubed - 3.0 * squared + 1.0;
                double h10 = cubed - 2.0 * squared + position;
                double h01 = -2.0 * cubed + 3.0 * squared;
                double h11 = cubed - squared;
                return (float) (h00 * left.value() + h10 * duration * left.tangent()
                        + h01 * right.value() + h11 * duration * right.tangent());
            }
        }
        throw new IllegalStateException("Angle curve has no matching segment");
    }

    public static double nextVerticalVelocity(double currentVelocity) {
        return currentVelocity - GRAVITY_PER_TICK;
    }

    private record CurveKey(double time, double value, double tangent) {
    }
}
