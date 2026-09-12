package com.kuronami.carryonkick.network;

public enum KickAction {
    START,
    RELEASE,
    CANCEL;

    public static KickAction fromNetwork(int value) {
        return value >= 0 && value < values().length ? values()[value] : CANCEL;
    }
}
