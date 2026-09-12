package com.kuronami.carryonkick.network;

import com.kuronami.carryonkick.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record KickActionPayload(KickAction action) {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "kick_action");

    public static KickActionPayload read(FriendlyByteBuf buffer) {
        return new KickActionPayload(KickAction.fromNetwork(buffer.readUnsignedByte()));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(action.ordinal());
    }
}
