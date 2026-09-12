package com.kuronami.carryonkick.kick;

import com.kuronami.carryonkick.network.KickAction;
import com.kuronami.carryonkick.network.KickVisualEventPayload;
import com.kuronami.carryonkick.platform.Services;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class KickServerController {
    private static final ChargeSessionStore SESSIONS = new ChargeSessionStore();

    private KickServerController() {
    }

    public static void handle(ServerPlayer player, KickAction action) {
        switch (action) {
            case START -> start(player);
            case RELEASE -> release(player);
            case CANCEL -> cancel(player, true);
        }
    }

    public static void tick(ServerPlayer player) {
        if (!valid(player)) {
            cancel(player, true);
            return;
        }
        UUID current = CarryOnAccess.carriedEntityUuid(player);
        if (SESSIONS.cancelIfChanged(player.getUUID(), current, player.level().dimension())) {
            send(player, KickVisualEventPayload.cancel(player.getId()));
        }
    }

    public static void disconnect(ServerPlayer player) {
        SESSIONS.cancel(player.getUUID());
    }

    public static void lifecycleChange(ServerPlayer player) {
        cancel(player, true);
    }

    private static void start(ServerPlayer player) {
        if (!valid(player)) {
            cancel(player, true);
            return;
        }
        UUID entity = CarryOnAccess.carriedMobUuid(player);
        if (entity != null && SESSIONS.start(
                player.getUUID(), entity, player.level().getGameTime(), player.level().dimension())) {
            send(player, KickVisualEventPayload.start(player.getId()));
        }
    }

    private static void release(ServerPlayer player) {
        if (!valid(player)) {
            cancel(player, true);
            return;
        }
        ChargeSessionStore.Release release = SESSIONS.release(
                player.getUUID(), CarryOnAccess.carriedEntityUuid(player), player.level().getGameTime(),
                player.level().dimension());
        if (!release.valid()) {
            send(player, KickVisualEventPayload.cancel(player.getId()));
            return;
        }
        KickLauncher.Result result = KickLauncher.launch(player, release.chargeTicks());
        if (!result.success()) {
            send(player, KickVisualEventPayload.cancel(player.getId()));
            return;
        }
        if (release.chargeTicks() < KickProfile.MIN_KICK_TICKS) {
            send(player, KickVisualEventPayload.drop(player.getId()));
            return;
        }
        send(player, KickVisualEventPayload.kick(
                player.getId(), release.chargeTicks(), release.normalizedCharge(), result.velocity()));
    }

    private static void cancel(ServerPlayer player, boolean notify) {
        if (SESSIONS.cancel(player.getUUID()) && notify) {
            send(player, KickVisualEventPayload.cancel(player.getId()));
        }
    }

    private static void send(ServerPlayer player, KickVisualEventPayload payload) {
        Services.PLATFORM.sendToTrackingAndSelf(player, payload);
    }

    private static boolean valid(ServerPlayer player) {
        return player.isAlive() && !player.isRemoved();
    }
}
