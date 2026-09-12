package com.kuronami.carryonkick.client;

import com.kuronami.carryonkick.kick.KickProfile;
import com.kuronami.carryonkick.network.KickVisualEventPayload;
import com.kuronami.carryonkick.network.KickVisualPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientKickState {
    private static final Map<Integer, VisualState> STATES = new ConcurrentHashMap<>();

    private ClientKickState() {
    }

    public static void handleVisualEvent(KickVisualEventPayload payload) {
        long gameTime = currentGameTime();
        if (payload.phase() == KickVisualPhase.CANCEL || payload.phase() == KickVisualPhase.DROP) {
            STATES.remove(payload.kickerEntityId());
            KickSounds.onPhase(payload.kickerEntityId(), payload.phase());
            return;
        }
        var level = Minecraft.getInstance().level;
        var entity = level == null ? null : level.getEntity(payload.kickerEntityId());
        if (entity == null) {
            return;
        }
        long startTime = payload.phase() == KickVisualPhase.KICK
                ? gameTime - payload.chargeTicks()
                : gameTime;
        long kickTime = payload.phase() == KickVisualPhase.KICK ? gameTime : -1L;
        STATES.put(payload.kickerEntityId(), new VisualState(
                entity.getUUID(), payload.phase(), startTime, kickTime, payload.chargeTicks(),
                payload.normalizedCharge(), payload.launchVelocity()));
        KickSounds.onPhase(payload.kickerEntityId(), payload.phase());
    }

    public static VisualState get(int entityId) {
        return STATES.get(entityId);
    }

    /** 描画されないプレイヤーの状態も、追跡終了と蹴り終了の時点で解放する。 */
    public static void tick() {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            clear();
            return;
        }
        long now = level.getGameTime();
        STATES.forEach((entityId, state) -> {
            var entity = level.getEntity(entityId);
            boolean ended = state.phase() == KickVisualPhase.KICK
                    && now - state.kickGameTime() >= 8;
            if (ended || entity == null || !entity.isAlive() || !entity.getUUID().equals(state.playerUuid())) {
                if (STATES.remove(entityId, state)) {
                    KickSounds.onPhase(entityId, KickVisualPhase.CANCEL);
                }
            }
        });
    }

    public static Pose pose(int entityId, float partialTicks) {
        VisualState state = STATES.get(entityId);
        if (state == null) {
            return Pose.NONE;
        }
        long gameTime = currentGameTime();
        float charge = state.phase() == KickVisualPhase.START
                ? KickProfile.normalizedCharge(Math.max(0.0, gameTime - state.startGameTime() + partialTicks))
                : state.normalizedCharge();
        float kickAgeSeconds = state.kickGameTime() < 0L
                ? -1.0F
                : (float) (Math.max(0.0, gameTime - state.kickGameTime() + partialTicks) / 20.0);
        if (kickAgeSeconds > 5.0F) {
            STATES.remove(entityId, state);
            return Pose.NONE;
        }
        return new Pose(charge, kickAgeSeconds, state.phase(), state.launchVelocity());
    }

    public static void clear() {
        KickSounds.clear();
        STATES.clear();
    }

    private static long currentGameTime() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }

    public record VisualState(
            UUID playerUuid,
            KickVisualPhase phase,
            long startGameTime,
            long kickGameTime,
            int chargeTicks,
            float normalizedCharge,
            Vec3 launchVelocity) {
    }

    public record Pose(float charge, float kickAgeSeconds, KickVisualPhase phase, Vec3 launchVelocity) {
        public static final Pose NONE = new Pose(0.0F, -1.0F, KickVisualPhase.CANCEL, Vec3.ZERO);
    }
}
