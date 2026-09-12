package com.kuronami.carryonkick.kick;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KickProfileTest {
    @Test
    void chargeFollowsBigWalkThresholdAndLinearImpulse() {
        assertEquals(0.0, KickTuning.launchSpeed(3), 1.0E-9);
        assertEquals(0.12, KickTuning.launchSpeed(4), 1.0E-8);
        assertEquals(0.3, KickTuning.launchSpeed(10), 1.0E-8);
        assertEquals(0.6, KickTuning.launchSpeed(20), 1.0E-9);
        assertEquals(0.6, KickTuning.launchSpeed(40), 1.0E-9);
    }

    @Test
    void angleCurveMatchesBigWalkHermiteSamples() {
        assertEquals(-44.9440, KickTuning.kickAngleDegrees(0.0F), 1.0E-3);
        assertEquals(-10.1150, KickTuning.kickAngleDegrees(-70.0F), 1.0E-3);
        assertEquals(-45.0008, KickTuning.kickAngleDegrees(80.0F), 1.0E-3);
    }

    @Test
    void flightUsesBigWalkGravityWithoutHorizontalDrag() {
        assertEquals(-0.024525, KickTuning.nextVerticalVelocity(0.0), 1.0E-9);
    }
}
