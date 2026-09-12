package com.kuronami.carryonkick;

import com.kuronami.carryonkick.kick.KickServerController;
import com.kuronami.carryonkick.network.KickActionPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class Carryonkick implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonClass.init();
        ServerPlayNetworking.registerGlobalReceiver(KickActionPayload.ID,
                (server, player, handler, buffer, responseSender) -> {
                    KickActionPayload payload = KickActionPayload.read(buffer);
                    server.execute(() -> KickServerController.handle(player, payload.action()));
                });
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
