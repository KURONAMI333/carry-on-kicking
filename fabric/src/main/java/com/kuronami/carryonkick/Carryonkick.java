package com.kuronami.carryonkick;

import com.kuronami.carryonkick.kick.KickServerController;
import com.kuronami.carryonkick.network.KickActionPayload;
import com.kuronami.carryonkick.network.KickVisualEventPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class Carryonkick implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonClass.init();
        PayloadTypeRegistry.playC2S().register(KickActionPayload.TYPE, KickActionPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(KickVisualEventPayload.TYPE, KickVisualEventPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KickActionPayload.TYPE,
                (payload, context) -> context.server().execute(
                        () -> KickServerController.handle(context.player(), payload.action())));
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers()
                .forEach(KickServerController::tick));
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> KickServerController.disconnect(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> KickServerController.lifecycleChange(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
                (player, origin, destination) -> KickServerController.lifecycleChange(player));
    }
}
