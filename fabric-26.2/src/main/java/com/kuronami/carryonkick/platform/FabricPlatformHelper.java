package com.kuronami.carryonkick.platform;

import com.kuronami.carryonkick.network.KickVisualEventPayload;
import com.kuronami.carryonkick.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

public final class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void sendToTrackingAndSelf(ServerPlayer player, KickVisualEventPayload payload) {
        for (ServerPlayer observer : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(observer, payload);
        }
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public boolean canPlaceMob(Mob mob, ServerLevel level) {
        return true;
    }
}
