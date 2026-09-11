package com.kuronami.carryonkick.network;

import com.kuronami.carryonkick.Constants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record KickVisualEventPayload(
        int kickerEntityId,
        KickVisualPhase phase,
        int chargeTicks,
        float normalizedCharge,
        double launchX,
        double launchY,
        double launchZ) implements CustomPacketPayload {
    public static final Type<KickVisualEventPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "kick_visual"));
    public static final StreamCodec<ByteBuf, KickVisualEventPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeInt(payload.kickerEntityId);
                buffer.writeByte(payload.phase.ordinal());
                buffer.writeInt(payload.chargeTicks);
                buffer.writeFloat(payload.normalizedCharge);
                buffer.writeDouble(payload.launchX);
                buffer.writeDouble(payload.launchY);
                buffer.writeDouble(payload.launchZ);
            },
            buffer -> new KickVisualEventPayload(
                    buffer.readInt(), KickVisualPhase.fromNetwork(buffer.readUnsignedByte()),
                    buffer.readInt(), buffer.readFloat(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));

    public static KickVisualEventPayload start(int entityId) {
        return new KickVisualEventPayload(entityId, KickVisualPhase.START, 0, 0.0F, 0.0, 0.0, 0.0);
    }

    public static KickVisualEventPayload cancel(int entityId) {
        return new KickVisualEventPayload(entityId, KickVisualPhase.CANCEL, 0, 0.0F, 0.0, 0.0, 0.0);
    }

    public static KickVisualEventPayload drop(int entityId) {
        return new KickVisualEventPayload(entityId, KickVisualPhase.DROP, 0, 0.0F, 0.0, 0.0, 0.0);
    }

    public static KickVisualEventPayload kick(int entityId, int chargeTicks, float normalizedCharge, Vec3 launch) {
        return new KickVisualEventPayload(entityId, KickVisualPhase.KICK, chargeTicks, normalizedCharge,
                launch.x, launch.y, launch.z);
    }

    public Vec3 launchVelocity() {
        return new Vec3(launchX, launchY, launchZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
