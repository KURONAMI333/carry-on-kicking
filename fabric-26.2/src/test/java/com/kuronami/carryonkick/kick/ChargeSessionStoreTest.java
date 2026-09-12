package com.kuronami.carryonkick.kick;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChargeSessionStoreTest {
    @Test
    void duplicateStartCannotResetCharge() {
        ChargeSessionStore store = new ChargeSessionStore();
        UUID player = UUID.randomUUID();
        UUID entity = UUID.randomUUID();

        assertTrue(store.start(player, entity, 100L));
        assertFalse(store.start(player, entity, 115L));
        ChargeSessionStore.Release release = store.release(player, entity, 120L);

        assertTrue(release.valid());
        assertEquals(20, release.chargeTicks());
        assertEquals(1.0F, release.normalizedCharge());
    }

    @Test
    void releaseRejectsChangedEntityAndConsumesSession() {
        ChargeSessionStore store = new ChargeSessionStore();
        UUID player = UUID.randomUUID();
        store.start(player, UUID.randomUUID(), 50L);

        assertFalse(store.release(player, UUID.randomUUID(), 60L).valid());
        assertFalse(store.contains(player));
    }

    @Test
    void chargeIsServerTimedAndClamped() {
        ChargeSessionStore store = new ChargeSessionStore();
        UUID player = UUID.randomUUID();
        UUID entity = UUID.randomUUID();
        store.start(player, entity, 10L);

        ChargeSessionStore.Release release = store.release(player, entity, 200L);
        assertEquals(KickTuning.MAX_CHARGE_TICKS, release.chargeTicks());
        assertEquals(1.0F, release.normalizedCharge());
    }

    @Test
    void contextChangeInvalidatesCharge() {
        ChargeSessionStore store = new ChargeSessionStore();
        UUID player = UUID.randomUUID();
        UUID entity = UUID.randomUUID();
        store.start(player, entity, 10L, "overworld");

        assertFalse(store.release(player, entity, 15L, "nether").valid());
        assertFalse(store.contains(player));
    }
}
