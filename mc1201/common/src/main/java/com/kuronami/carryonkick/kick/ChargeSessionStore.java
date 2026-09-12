package com.kuronami.carryonkick.kick;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ChargeSessionStore {
    private final Map<UUID, Session> sessions = new HashMap<>();

    public boolean start(UUID playerId, UUID entityId, long gameTime) {
        return start(playerId, entityId, gameTime, null);
    }

    public boolean start(UUID playerId, UUID entityId, long gameTime, Object contextToken) {
        Session current = sessions.get(playerId);
        if (current != null && current.entityId().equals(entityId)) {
            return false;
        }
        sessions.put(playerId, new Session(entityId, gameTime, contextToken));
        return true;
    }

    public Release release(UUID playerId, UUID currentEntityId, long gameTime) {
        return release(playerId, currentEntityId, gameTime, null);
    }

    public Release release(UUID playerId, UUID currentEntityId, long gameTime, Object contextToken) {
        Session session = sessions.remove(playerId);
        if (session == null || !session.entityId().equals(currentEntityId)
                || !java.util.Objects.equals(session.contextToken(), contextToken)) {
            return Release.invalid();
        }
        long elapsed = Math.max(0L, gameTime - session.startGameTime());
        int ticks = (int) Math.min(elapsed, KickTuning.MAX_CHARGE_TICKS);
        return new Release(true, ticks, KickTuning.normalizedCharge(ticks));
    }

    public boolean cancel(UUID playerId) {
        return sessions.remove(playerId) != null;
    }

    public boolean cancelIfChanged(UUID playerId, UUID currentEntityId) {
        return cancelIfChanged(playerId, currentEntityId, null);
    }

    public boolean cancelIfChanged(UUID playerId, UUID currentEntityId, Object contextToken) {
        Session session = sessions.get(playerId);
        if (session == null || session.entityId().equals(currentEntityId)
                && java.util.Objects.equals(session.contextToken(), contextToken)) {
            return false;
        }
        sessions.remove(playerId);
        return true;
    }

    public boolean contains(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    record Session(UUID entityId, long startGameTime, Object contextToken) {
    }

    public record Release(boolean valid, int chargeTicks, float normalizedCharge) {
        static Release invalid() {
            return new Release(false, 0, 0.0F);
        }
    }
}
