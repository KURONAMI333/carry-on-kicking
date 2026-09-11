package com.kuronami.carryonkick;

import com.kuronami.carryonkick.platform.Services;

public final class CommonClass {

    private CommonClass() {
    }

    public static void init() {
        if (!Services.PLATFORM.isModLoaded("carryon")) {
            throw new IllegalStateException("Carry On Kick requires Carry On");
        }
        Constants.LOG.info("Carry On Kick initialized on {}", Services.PLATFORM.getPlatformName());
    }
}
