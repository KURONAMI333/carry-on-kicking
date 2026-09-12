package com.kuronami.carryonkick;

import com.kuronami.carryonkick.network.ForgeKickNetwork;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public final class Carryonkick {
    public Carryonkick() {
        CommonClass.init();
        ForgeKickNetwork.register();
    }
}
