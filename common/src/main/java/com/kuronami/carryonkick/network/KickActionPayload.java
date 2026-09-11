package com.kuronami.carryonkick.network;

import com.kuronami.carryonkick.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record KickActionPayload(KickAction action) implements CustomPacketPayload {
    public static final Type<KickActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "kick_action"));
    public static final StreamCodec<ByteBuf, KickActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeByte(payload.action.ordinal()),
            buffer -> new KickActionPayload(KickAction.fromNetwork(buffer.readUnsignedByte())));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
