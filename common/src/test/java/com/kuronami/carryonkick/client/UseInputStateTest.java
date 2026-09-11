package com.kuronami.carryonkick.client;

import com.kuronami.carryonkick.network.KickAction;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseInputStateTest {
    @Test
    void pickupHeldButtonRequiresReleaseAndRepress() {
        UseInputState state = new UseInputState();
        UUID entity = UUID.randomUUID();

        assertFalse(state.onStartUse(true, null).consumeUse());
        assertTrue(state.onTick(true, entity).action().isEmpty());
        assertTrue(state.onTick(false, entity).action().isEmpty());

        UseInputState.Output repress = state.onStartUse(true, entity);
        assertTrue(repress.consumeUse());
        assertEquals(KickAction.START, repress.action().orElseThrow());
    }

    @Test
    void repeatedStartUseSendsOneStartAndReleaseSendsOneRelease() {
        UseInputState state = new UseInputState();
        UUID entity = UUID.randomUUID();

        assertEquals(KickAction.START, state.onStartUse(true, entity).action().orElseThrow());
        assertTrue(state.onStartUse(true, entity).action().isEmpty());
        assertTrue(state.onTick(true, entity).action().isEmpty());
        assertEquals(KickAction.RELEASE, state.onTick(false, entity).action().orElseThrow());
        assertTrue(state.onTick(false, entity).action().isEmpty());
    }

    @Test
    void carriedEntityChangeCancelsAndWaitsForNextPress() {
        UseInputState state = new UseInputState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        state.onStartUse(true, first);
        assertEquals(KickAction.CANCEL, state.onTick(true, second).action().orElseThrow());
        assertTrue(state.onStartUse(true, second).action().isEmpty());
        state.onTick(false, second);
        assertEquals(KickAction.START, state.onStartUse(true, second).action().orElseThrow());
    }
}
