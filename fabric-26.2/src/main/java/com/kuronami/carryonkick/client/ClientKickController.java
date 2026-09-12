package com.kuronami.carryonkick.client;

import com.kuronami.carryonkick.network.KickAction;
import com.kuronami.carryonkick.network.KickActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

import java.util.UUID;
import java.util.function.Consumer;

public final class ClientKickController {
    private static final UseInputState INPUT = new UseInputState();
    private static Consumer<KickActionPayload> sender = payload -> {
    };
    private static Object observedLevel;
    private static UUID cachedEntity;
    private static boolean cachedMob;

    private ClientKickController() {
    }

    public static void registerSender(Consumer<KickActionPayload> packetSender) {
        sender = packetSender;
    }

    public static boolean interceptUse(Minecraft minecraft) {
        UUID carriedEntity = carriedEntityUuid(minecraft);
        UseInputState.Output output = INPUT.onStartUse(minecraft.options.keyUse.isDown(), carriedEntity);
        output.action().ifPresent(ClientKickController::send);
        return output.consumeUse();
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level != observedLevel) {
            INPUT.cancel().ifPresent(ClientKickController::send);
            ClientKickState.clear();
            INPUT.reset();
            clearMobCache();
            observedLevel = minecraft.level;
            if (minecraft.player != null && minecraft.level != null) {
                INPUT.onTick(minecraft.options.keyUse.isDown(), carriedEntityUuid(minecraft));
            }
            return;
        }
        ClientKickState.tick();
        if (minecraft.player == null || minecraft.level == null || minecraft.gui.screen() != null) {
            INPUT.cancel().ifPresent(ClientKickController::send);
            return;
        }
        UseInputState.Output output = INPUT.onTick(
                minecraft.options.keyUse.isDown(), carriedEntityUuid(minecraft));
        output.action().ifPresent(ClientKickController::send);
    }

    public static void reset() {
        INPUT.reset();
        ClientKickState.clear();
        observedLevel = null;
        clearMobCache();
    }

    private static void send(KickAction action) {
        sender.accept(new KickActionPayload(action));
    }

    private static UUID carriedEntityUuid(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) {
            return null;
        }
        CarryOnData carry = CarryOnDataManager.getCarryData(minecraft.player);
        if (!carry.isCarrying(CarryOnData.CarryType.ENTITY)) {
            clearMobCache();
            return null;
        }
        CompoundTag entity = carry.getContentNbt();
        if (entity == null) {
            clearMobCache();
            return null;
        }
        UUID uuid = entity.read("UUID", UUIDUtil.CODEC).orElse(null);
        if (uuid == null) {
            clearMobCache();
            return null;
        }
        if (!uuid.equals(cachedEntity)) {
            Entity decoded = EntityType.create(
                    net.minecraft.world.level.storage.TagValueInput.create(
                            ProblemReporter.DISCARDING, minecraft.level.registryAccess(), entity),
                    minecraft.level, new EntitySpawnRequest(EntitySpawnReason.BUCKET, false)).orElse(null);
            cachedEntity = uuid;
            cachedMob = decoded instanceof Mob;
        }
        return cachedMob ? uuid : null;
    }

    private static void clearMobCache() {
        cachedEntity = null;
        cachedMob = false;
    }
}
