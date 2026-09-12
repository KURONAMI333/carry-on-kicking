package com.kuronami.carryonkick.network;

import com.kuronami.carryonkick.client.ClientKickState;

/** Only invoked for S2C packets on the physical client. */
public final class ForgeKickClientPackets {
    private ForgeKickClientPackets() {
    }

    public static void handle(KickVisualEventPayload payload) {
        ClientKickState.handleVisualEvent(payload);
    }
}
