package com.kuronami.carryonkick.network;

import com.kuronami.carryonkick.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record KickActionPayload(KickAction action) implements CustomPacketPayload {
    public static final Type<KickActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "kick_action"));
    public static final StreamCodec<FriendlyByteBuf, KickActionPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> payload.write(buffer), KickActionPayload::read);

    public static KickActionPayload read(FriendlyByteBuf buffer) {
        return new KickActionPayload(KickAction.fromNetwork(buffer.readUnsignedByte()));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(action.ordinal());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
