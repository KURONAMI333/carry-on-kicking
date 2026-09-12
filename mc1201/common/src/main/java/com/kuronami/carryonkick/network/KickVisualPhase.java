package com.kuronami.carryonkick.network;

public enum KickVisualPhase {
    START,
    CANCEL,
    DROP,
    KICK;

    public static KickVisualPhase fromNetwork(int value) {
        return value >= 0 && value < values().length ? values()[value] : CANCEL;
    }
}
