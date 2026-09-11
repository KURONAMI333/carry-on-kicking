package com.kuronami.carryonkick.kick;

import net.minecraft.nbt.CompoundTag;
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
        return entity != null && entity.hasUUID("UUID") ? entity.getUUID("UUID") : null;
    }

    public static UUID carriedMobUuid(Player player) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if (!carry.isCarrying(CarryOnData.CarryType.ENTITY)) {
            return null;
        }
        CompoundTag tag = carry.getContentNbt();
        if (tag == null || !tag.hasUUID("UUID")) {
            return null;
        }
        Entity decoded = EntityType.create(tag, player.level()).orElse(null);
        return decoded instanceof Mob ? tag.getUUID("UUID") : null;
    }
}
