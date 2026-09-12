package com.kuronami.carryonkick.network;

import com.kuronami.carryonkick.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
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
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "kick_visual"));
    public static final StreamCodec<FriendlyByteBuf, KickVisualEventPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> payload.write(buffer), KickVisualEventPayload::read);

    public static KickVisualEventPayload read(FriendlyByteBuf buffer) {
        return new KickVisualEventPayload(
                buffer.readInt(), KickVisualPhase.fromNetwork(buffer.readUnsignedByte()),
                buffer.readInt(), buffer.readFloat(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(kickerEntityId);
        buffer.writeByte(phase.ordinal());
        buffer.writeInt(chargeTicks);
        buffer.writeFloat(normalizedCharge);
        buffer.writeDouble(launchX);
        buffer.writeDouble(launchY);
        buffer.writeDouble(launchZ);
    }

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
