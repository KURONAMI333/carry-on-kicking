package com.kuronami.carryonkick.kick;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import com.kuronami.carryonkick.Constants;
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
        UUID uuid = tag.read("UUID", UUIDUtil.CODEC).orElse(null);
        if (uuid == null) {
            return null;
        }
        Entity decoded = decodeEntity(tag, player.level());
        return decoded instanceof Mob ? uuid : null;
    }

    /** 保持データを変更せず検査する。本家 getEntity は復元失敗時に保持を消す。 */
    public static Entity decodeEntity(CompoundTag tag, Level level) {
        try (var problems = new ProblemReporter.ScopedCollector(Constants.LOG)) {
            return EntityType.create(TagValueInput.create(problems, level.registryAccess(), tag),
                    level, EntitySpawnReason.BUCKET).orElse(null);
        }
    }
}
