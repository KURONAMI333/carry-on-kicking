package com.kuronami.carryonkick.platform;

import com.kuronami.carryonkick.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    @Override
    public boolean canPlaceMob(Mob mob, ServerLevel level) {
        MobSpawnEvent.PositionCheck event = new MobSpawnEvent.PositionCheck(mob, level, MobSpawnType.EVENT, null);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult() != MobSpawnEvent.PositionCheck.Result.FAIL;
    }
}
