package com.kuronami.carryonkick.kick;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

import java.util.UUID;

public final class CarryOnAccess {
    private CarryOnAccess() {
    }

    public static UUID carriedEntityUuid(Player player) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if (!carry.isCarrying(CarryOnData.CarryType.ENTITY)) {
            return null;
        }
        CompoundTag entity = carry.getContentNbt();
        return entity == null ? null : entity.read("UUID", UUIDUtil.CODEC).orElse(null);
    }

    public static UUID carriedMobUuid(Player player) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if (!carry.isCarrying(CarryOnData.CarryType.ENTITY)) {
            return null;
        }
        CompoundTag tag = carry.getContentNbt();
        if (tag == null) {
            return null;
        }
        Entity decoded = EntityType.create(
                net.minecraft.world.level.storage.TagValueInput.create(
                        ProblemReporter.DISCARDING, player.level().registryAccess(), tag),
                player.level(), new EntitySpawnRequest(EntitySpawnReason.BUCKET, true)).orElse(null);
        return decoded instanceof Mob ? tag.read("UUID", UUIDUtil.CODEC).orElse(null) : null;
    }
}
