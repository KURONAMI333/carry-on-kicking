package com.kuronami.carryonkick;


import com.kuronami.carryonkick.network.NeoForgePayloads;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public final class Carryonkick {

    public Carryonkick(IEventBus eventBus, Dist dist) {
        CommonClass.init();
        eventBus.addListener(NeoForgePayloads::register);
        if (dist.isClient()) {
            ClientSetup.initialize();
        }
    }

    private static final class ClientSetup {
        private static void initialize() {
            com.kuronami.carryonkick.client.ClientKickController.registerSender(
                    net.neoforged.neoforge.network.PacketDistributor::sendToServer);
        }
    }
}
