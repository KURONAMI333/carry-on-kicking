package com.kuronami.carryonkick.client;

import net.minecraft.util.Mth;

/** 溜めと蹴りを、Minecraftの一節の脚へ置き換えた姿勢。 */
public final class KickAnimation {
    public static final float DURATION_SECONDS = 0.4F;

    // 参照動作の腰から足へ向かう角度を50ms間隔で測った値。原作のモデル・アニメーションは含めない。
    private static final float[] KICK_DEGREES = {21, 75, 102, 113, 123, 116, 71, 14, 0};
    private static final float[] HIP_LIFT = {0.10F, 0.21F, 0.25F, 0.23F, 0.18F, 0.12F, 0.06F, 0.02F, 0};

    private KickAnimation() {
    }

    /** 通信済みの姿勢から、左脚の角度と腰の持ち上げ量を求める。 */
    public static LegPose pose(ClientKickState.Pose state) {
        float charge = Mth.clamp(state.charge(), 0, 1);
        return switch (state.phase()) {
            case START -> new LegPose(-67 * Mth.DEG_TO_RAD * charge, 0.06F * charge, true);
            case KICK -> {
                float age = state.kickAgeSeconds();
                if (age < 0 || age >= DURATION_SECONDS) {
                    yield LegPose.NONE;
                }
                // 蹴りの強さは発射力だけに使い、成立した蹴りの再生速度や振幅は変えない。
                yield new LegPose(sample(KICK_DEGREES, age) * Mth.DEG_TO_RAD,
                        sample(HIP_LIFT, age), true);
            }
            case CANCEL, DROP -> LegPose.NONE;
        };
    }

    private static float sample(float[] values, float seconds) {
        float cursor = Mth.clamp(seconds / DURATION_SECONDS, 0, 1) * (values.length - 1);
        int index = Math.min((int) cursor, values.length - 2);
        float t = cursor - index;
        float previous = values[Math.max(0, index - 1)];
        float start = values[index];
        float end = values[index + 1];
        float next = values[Math.min(values.length - 1, index + 2)];
        return 0.5F * (2 * start + (-previous + end) * t
                + (2 * previous - 5 * start + 4 * end - next) * t * t
                + (-previous + 3 * start - 3 * end + next) * t * t * t);
    }

    public record LegPose(float forwardAngleRad, float hipLift, boolean visible) {
        public static final LegPose NONE = new LegPose(0, 0, false);
    }
}
