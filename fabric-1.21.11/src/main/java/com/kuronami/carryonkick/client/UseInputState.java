package com.kuronami.carryonkick.client;

import com.kuronami.carryonkick.network.KickAction;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class UseInputState {
    private boolean useButtonDown;
    private UUID chargingEntity;

    public Output onStartUse(boolean buttonDown, UUID carriedEntity) {
        boolean consume = carriedEntity != null;
        Optional<KickAction> action = Optional.empty();
        if (consume && chargingEntity == null && !useButtonDown) {
            chargingEntity = carriedEntity;
            action = Optional.of(KickAction.START);
        }
        useButtonDown = buttonDown;
        return new Output(consume, action);
    }

    public Output onTick(boolean buttonDown, UUID carriedEntity) {
        Optional<KickAction> action = Optional.empty();
        if (chargingEntity != null) {
            if (!Objects.equals(chargingEntity, carriedEntity)) {
                chargingEntity = null;
                action = Optional.of(KickAction.CANCEL);
            } else if (!buttonDown) {
                chargingEntity = null;
                action = Optional.of(KickAction.RELEASE);
            }
        }
        useButtonDown = buttonDown;
        return new Output(carriedEntity != null, action);
    }

    public Optional<KickAction> cancel() {
        if (chargingEntity == null) {
            return Optional.empty();
        }
        chargingEntity = null;
        return Optional.of(KickAction.CANCEL);
    }

    public void reset() {
        useButtonDown = false;
        chargingEntity = null;
    }

    public boolean isCharging() {
        return chargingEntity != null;
    }

    public record Output(boolean consumeUse, Optional<KickAction> action) {
    }
}
