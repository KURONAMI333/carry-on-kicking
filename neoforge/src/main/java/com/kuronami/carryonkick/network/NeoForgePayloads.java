package com.kuronami.carryonkick.network;

import com.kuronami.carryonkick.client.ClientKickState;
import com.kuronami.carryonkick.kick.KickServerController;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NeoForgePayloads {
    private static final String PROTOCOL_VERSION = "1";

    private NeoForgePayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(KickActionPayload.TYPE, KickActionPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        KickServerController.handle(player, payload.action());
                    }
                });
        registrar.playToClient(KickVisualEventPayload.TYPE, KickVisualEventPayload.STREAM_CODEC,
                (payload, context) -> ClientKickState.handleVisualEvent(payload));
    }
}
