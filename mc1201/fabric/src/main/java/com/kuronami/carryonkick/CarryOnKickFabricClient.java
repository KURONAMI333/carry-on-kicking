package com.kuronami.carryonkick;

import com.kuronami.carryonkick.client.ClientKickController;
import com.kuronami.carryonkick.client.ClientKickState;
import com.kuronami.carryonkick.network.KickActionPayload;
import com.kuronami.carryonkick.network.KickVisualEventPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public final class CarryOnKickFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientKickController.registerSender(payload -> {
            var buffer = PacketByteBufs.create();
            payload.write(buffer);
            ClientPlayNetworking.send(KickActionPayload.ID, buffer);
        });
        ClientTickEvents.END_CLIENT_TICK.register(ClientKickController::tick);
        ClientPlayNetworking.registerGlobalReceiver(KickVisualEventPayload.ID,
                (client, handler, buffer, responseSender) -> {
                    KickVisualEventPayload payload = KickVisualEventPayload.read(buffer);
                    client.execute(() -> ClientKickState.handleVisualEvent(payload));
                });
        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> ClientKickController.reset());
    }
}
