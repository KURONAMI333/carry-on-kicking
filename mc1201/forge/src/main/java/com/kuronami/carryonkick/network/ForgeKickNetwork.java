package com.kuronami.carryonkick.network;

import com.kuronami.carryonkick.Constants;
import com.kuronami.carryonkick.kick.KickServerController;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeKickNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "main"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);

    private ForgeKickNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(KickActionPayload.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(KickActionPayload::write)
                .decoder(KickActionPayload::read)
                .consumerMainThread((payload, context) -> {
                    ServerPlayer sender = context.get().getSender();
                    if (sender != null) {
                        KickServerController.handle(sender, payload.action());
                    }
                })
                .add();
        CHANNEL.messageBuilder(KickVisualEventPayload.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(KickVisualEventPayload::write)
                .decoder(KickVisualEventPayload::read)
                .consumerMainThread((payload, context) -> ForgeKickClientPackets.handle(payload))
                .add();
    }
}
