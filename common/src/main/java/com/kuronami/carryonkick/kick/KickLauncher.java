package com.kuronami.carryonkick.kick;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptEffects;
import com.kuronami.carryonkick.platform.Services;

import java.util.Optional;

public final class KickLauncher {
    private KickLauncher() {
    }

    public static Result launch(ServerPlayer player, int chargeTicks) {
        CarryOnData carry = CarryOnDataManager.getCarryData(player);
        if (!carry.isCarrying(CarryOnData.CarryType.ENTITY)) {
            return Result.failed();
        }
        CompoundTag entityTag = carry.getContentNbt();
        if (entityTag == null) {
            return Result.failed();
        }
        ServerLevel level = player.serverLevel();
        Optional<Entity> decoded = EntityType.create(entityTag, level);
        if (decoded.isEmpty()) {
            return Result.failed();
        }

        Entity entity = decoded.get();
        if (!(entity instanceof Mob mob)) {
            return Result.failed();
        }
        Vec3 velocity = KickProfile.launchVelocity(player.getXRot(), player.getYRot(), chargeTicks);
        float yawRadians = -player.getYRot() * net.minecraft.util.Mth.DEG_TO_RAD;
        Vec3 horizontalFacing = new Vec3(
                net.minecraft.util.Mth.sin(yawRadians), 0.0, net.minecraft.util.Mth.cos(yawRadians));
        double clearance = player.getBbWidth() * 0.5 + entity.getBbWidth() * 0.5 + 0.05;
        Vec3 horizontalOrigin = player.getEyePosition()
                .add(horizontalFacing.scale(Math.max(KickProfile.SPAWN_DISTANCE, clearance)));
        double originY = Math.max(player.getY() + 0.05, player.getEyeY() - entity.getBbHeight() * 0.55);
        Vec3 origin = new Vec3(horizontalOrigin.x, originY, horizontalOrigin.z);
        entity.setPos(origin);
        if (!safeSpawn(level, player, entity, origin)) {
            return Result.failed();
        }
        if (!Services.PLATFORM.canPlaceMob(mob, level)) {
            return Result.failed();
        }
        entity.setDeltaMovement(velocity);
        entity.setOnGround(false);
        entity.fallDistance = 0.0F;
        entity.hasImpulse = true;

        CarryOnData backup = carry.clone();
        if (!level.addFreshEntity(entity)) {
            return Result.failed();
        }
        try {
            runPlaceCommand(player, carry);
            carry.clear();
            CarryOnDataManager.setCarryData(player, carry);
        } catch (RuntimeException exception) {
            entity.discard();
            CarryOnDataManager.setCarryData(player, backup);
            throw exception;
        }

        if (!player.isCreative() || tschipp.carryon.Constants.COMMON_CONFIG.settings.slownessInCreative) {
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        if (chargeTicks < KickProfile.MIN_KICK_TICKS) {
            player.swing(InteractionHand.MAIN_HAND, true);
        }
        KickFlightController.begin(entity, velocity);
        return new Result(true, velocity);
    }

    private static void runPlaceCommand(ServerPlayer player, CarryOnData carry) {
        carry.getActiveScript().ifPresent(script -> {
            ScriptEffects effects = script.scriptEffects();
            String command = effects.commandPlace();
            if (!command.isEmpty()) {
                player.getServer().getCommands().performPrefixedCommand(
                        player.getServer().createCommandSourceStack(),
                        "/execute as " + player.getGameProfile().getName() + " run " + command);
            }
        });
    }

    private static boolean safeSpawn(ServerLevel level, ServerPlayer player, Entity entity, Vec3 origin) {
        if (!level.noCollision(entity, entity.getBoundingBox())
                || entity.getBoundingBox().intersects(player.getBoundingBox())) {
            return false;
        }
        return level.clip(new ClipContext(
                player.getEyePosition(), origin, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                .getType() == HitResult.Type.MISS;
    }

    public record Result(boolean success, Vec3 velocity) {
        static Result failed() {
            return new Result(false, Vec3.ZERO);
        }
    }
}
