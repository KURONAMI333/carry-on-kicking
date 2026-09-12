package com.kuronami.carryonkick.kick;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/** Big Walkの空中弾道を、Minecraftのmob AIと空気抵抗から独立して進める。 */
public final class KickFlightController {
    private static final int MAX_FLIGHT_TICKS = 20 * 20;
    private static final Map<LivingEntity, Flight> FLIGHTS = new WeakHashMap<>();

    private KickFlightController() {
    }

    public static void begin(Entity entity, Vec3 velocity) {
        if (entity instanceof LivingEntity living && !velocity.equals(Vec3.ZERO) && !entity.level().isClientSide()) {
            FLIGHTS.put(living, new Flight(velocity, 0));
        }
    }

    /** @return vanillaのtravelを取り消した時だけtrue */
    public static boolean travel(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return false;
        }
        Flight flight = FLIGHTS.get(entity);
        if (flight == null) {
            return false;
        }
        if (!entity.isAlive() || entity.isPassenger() || entity.isVehicle()
                || entity.isInWater() || entity.isInLava()
                || flight.ageTicks() >= MAX_FLIGHT_TICKS) {
            stop(entity, false);
            return false;
        }

        Vec3 velocity = flight.velocity();
        entity.setDeltaMovement(velocity);
        entity.move(MoverType.SELF, velocity);
        if (entity.horizontalCollision || entity.verticalCollision || entity.onGround()) {
            stop(entity, true);
            return true;
        }

        Vec3 nextVelocity = new Vec3(velocity.x, KickProfile.nextVerticalVelocity(velocity.y), velocity.z);
        entity.setDeltaMovement(nextVelocity);
        FLIGHTS.put(entity, new Flight(nextVelocity, flight.ageTicks() + 1));
        return true;
    }

    public static boolean isFlying(Entity entity) {
        return !entity.level().isClientSide()
                && entity instanceof LivingEntity living && FLIGHTS.containsKey(living);
    }

    private static void stop(LivingEntity entity, boolean collided) {
        FLIGHTS.remove(entity);
        if (collided) {
            entity.setDeltaMovement(Vec3.ZERO);
        }
    }

    record Flight(Vec3 velocity, int ageTicks) {
    }
}
