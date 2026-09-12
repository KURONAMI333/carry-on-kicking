package com.kuronami.carryonkicktest;

import com.kuronami.carryonkick.kick.KickFlightController;
import com.kuronami.carryonkick.kick.KickLauncher;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.monster.EnderMan;

import net.minecraft.world.entity.EntitySpawnReason;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

import java.util.List;
import java.util.UUID;


public final class CarryOnKickGameTests  {
    public CarryOnKickGameTests() {
    }
    @GameTest(maxTicks = 100)
    public void restoresEntityNbtAndRejectsDuplicateRelease(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        Cow original = carriedCow(helper, player);
        original.setCustomName(Component.literal("Kick Test"));
        original.setAge(-200);
        original.setHealth(7.0F);
        UUID id = storeAndRemove(player, original);

        helper.runAfterDelay(4, () -> {
            KickLauncher.Result first = KickLauncher.launch(player, 4);
            KickLauncher.Result duplicate = KickLauncher.launch(player, 4);
            if (!first.success() || duplicate.success()) {
                helper.fail("first release did not succeed exactly once");
                return;
            }

            Entity restored = helper.getLevel().getEntity(id);
            if (!(restored instanceof Cow cow)) {
                helper.fail("carried cow was not restored exactly once");
                return;
            }
            if (!Component.literal("Kick Test").equals(cow.getCustomName())) {
                helper.fail("custom name was not preserved");
                return;
            }
            if (cow.getAge() != -200 || cow.getHealth() != 7.0F) {
                helper.fail("age or health NBT was not preserved");
                return;
            }
            if (CarryOnDataManager.getCarryData(player).isCarrying()) {
                helper.fail("carry state remained after successful release");
                return;
            }
            long sameUuidCount = 0L;
            for (Entity entity : helper.getLevel().getAllEntities()) {
                if (entity.getUUID().equals(id)) {
                    sameUuidCount++;
                }
            }
            if (sameUuidCount != 1L) {
                helper.fail("duplicate release spawned " + sameUuidCount + " entities with the carried UUID");
                return;
            }
            helper.succeed();
        });
    }
    @GameTest(maxTicks = 100)
    public void blockedSpawnKeepsCarriedEntity(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        Cow cow = carriedCow(helper, player);
        UUID id = storeAndRemove(player, cow);
        helper.setBlock(new BlockPos(8, 2, 9), Blocks.STONE);
        helper.setBlock(new BlockPos(8, 3, 9), Blocks.STONE);

        KickLauncher.Result result = KickLauncher.launch(player, 20);
        if (result.success()) {
            helper.fail("kick succeeded through a blocking wall");
            return;
        }
        if (!CarryOnDataManager.getCarryData(player).isCarrying(CarryOnData.CarryType.ENTITY)) {
            helper.fail("failed spawn lost the carried entity");
            return;
        }
        if (helper.getLevel().getEntity(id) != null) {
            helper.fail("failed spawn left a duplicate entity in the level");
            return;
        }
        helper.succeed();
    }
    @GameTest(maxTicks = 100)
    public void spawnPositionDoesNotDependOnViewPitch(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, -90.0F);
        for (float pitch : new float[]{-90.0F, 0.0F, 90.0F}) {
            player.setXRot(pitch);
            Cow cow = carriedCow(helper, player);
            UUID id = storeAndRemove(player, cow);
            KickLauncher.Result result = KickLauncher.launch(player, 0);
            if (!result.success()) {
                helper.fail("safe drop failed at view pitch " + pitch);
                return;
            }
            Entity restored = helper.getLevel().getEntity(id);
            if (restored == null) {
                helper.fail("drop did not restore entity at view pitch " + pitch);
                return;
            }
            restored.discard();
        }

        player.setXRot(90.0F);
        EnderMan enderMan = EntityType.ENDERMAN.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (enderMan == null) {
            helper.fail("enderman type did not create an entity");
            return;
        }
        enderMan.setPos(player.position());
        helper.getLevel().addFreshEntity(enderMan);
        UUID tallMobId = storeAndRemove(player, enderMan);
        if (!KickLauncher.launch(player, 0).success()) {
            helper.fail("safe drop embedded a tall mob in the floor while looking down");
            return;
        }
        Entity restoredTallMob = helper.getLevel().getEntity(tallMobId);
        if (!(restoredTallMob instanceof EnderMan)) {
            helper.fail("safe drop did not restore the tall mob");
            return;
        }
        restoredTallMob.discard();
        helper.succeed();
    }
    @GameTest(maxTicks = 100)
    public void releasesAdultMobAtEveryYawWithoutPlayerOverlap(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        List<EntityType<? extends Mob>> mobTypes = List.of(
                EntityType.POLAR_BEAR, EntityType.SPIDER, EntityType.COW, EntityType.PIG);
        int[] yaws = {45, 0, 15, 30, 60, 75, 90, 105, 120, 135, 150, 165,
                180, 195, 210, 225, 240, 255, 270, 285, 300, 315, 330, 345};
        for (EntityType<? extends Mob> mobType : mobTypes) {
            for (int chargeTicks : new int[]{0, 20}) {
                for (int yaw : yaws) {
                    player.setYRot(yaw);
                    Mob original = mobType.create(helper.getLevel(), EntitySpawnReason.COMMAND);
                    if (original == null) {
                        helper.fail("mob type did not create at yaw " + yaw);
                        return;
                    }
                    boolean expectedAdultAge = original instanceof AgeableMob;
                    Component name = Component.literal(mobType + " yaw " + yaw + " charge " + chargeTicks);
                    original.setCustomName(name);
                    original.setHealth(5.0F);
                    if (original instanceof AgeableMob originalAgeable) {
                        originalAgeable.setAge(0);
                    }
                    original.setPos(player.position());
                    helper.getLevel().addFreshEntity(original);
                    UUID id = storeAndRemove(player, original);

                    KickLauncher.Result result = KickLauncher.launch(player, chargeTicks);
                    if (!result.success()) {
                        helper.fail("open-space release failed for " + mobType
                                + " at yaw " + yaw + " with charge " + chargeTicks);
                        return;
                    }
                    Entity restored = helper.getLevel().getEntity(id);
                    if (restored == null || restored.getType() != mobType || !id.equals(restored.getUUID())) {
                        helper.fail("release changed or lost " + mobType + " at yaw " + yaw);
                        return;
                    }
                    if (!name.equals(restored.getCustomName())
                            || !(restored instanceof Mob restoredMob)
                            || restoredMob.getHealth() != 5.0F) {
                        helper.fail("release did not preserve " + mobType + " NBT at yaw " + yaw);
                        return;
                    }
                    if (expectedAdultAge
                            && (!(restored instanceof AgeableMob restoredAgeable) || restoredAgeable.getAge() != 0)) {
                        helper.fail("release did not preserve adult age for " + mobType + " at yaw " + yaw);
                        return;
                    }
                    if (restored.getBoundingBox().intersects(player.getBoundingBox())) {
                        helper.fail("released " + mobType + " intersects the player at yaw " + yaw);
                        return;
                    }
                    restored.discard();
                }
            }
        }
        helper.succeed();
    }
    @GameTest(maxTicks = 100)
    public void nonMobUsesNormalCarryOnReleasePath(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        ArmorStand stand = EntityType.ARMOR_STAND.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (stand == null) {
            helper.fail("armor stand type did not create an entity");
            return;
        }
        stand.setPos(player.position());
        helper.getLevel().addFreshEntity(stand);
        UUID id = storeAndRemove(player, stand);

        if (KickLauncher.launch(player, 20).success()) {
            helper.fail("non-mob entity was intercepted as a kick target");
            return;
        }
        if (!CarryOnDataManager.getCarryData(player).isCarrying(CarryOnData.CarryType.ENTITY)
                || helper.getLevel().getEntity(id) != null) {
            helper.fail("non-mob rejection changed Carry On state");
            return;
        }
        helper.succeed();
    }
    @GameTest(maxTicks = 100)
    public void flightStopsOnLanding(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        Cow cow = carriedCow(helper, player);
        UUID id = storeAndRemove(player, cow);
        KickLauncher.Result result = KickLauncher.launch(player, 4);
        if (!result.success()) {
            helper.fail("kick could not start in open space");
            return;
        }
        Entity restored = helper.getLevel().getEntity(id);
        if (!(restored instanceof Cow kicked)) {
            helper.fail("kicked cow missing");
            return;
        }
        double startingZ = kicked.getZ();
        // Fabric の test tick 1 は entity tick より先に呼ばれる場合がある。
        helper.runAfterDelay(3, () -> {
            if (!KickFlightController.isFlying(kicked)) {
                helper.fail("flight controller stopped before airborne movement");
            } else if (kicked.getZ() <= startingZ) {
                helper.fail("flight controller did not advance the kicked cow: age=" + kicked.tickCount
                        + " z=" + kicked.getZ() + " start=" + startingZ
                        + " velocity=" + kicked.getDeltaMovement());
            }
        });
        helper.runAfterDelay(80, () -> {
            if (KickFlightController.isFlying(kicked)) {
                helper.fail("flight controller did not release the cow after landing");
                return;
            }
            if (!kicked.isAlive()) {
                helper.fail("kicked cow did not survive the landing test");
                return;
            }
            if (!kicked.onGround()) {
                helper.fail("flight ended without returning the cow to ground movement");
                return;
            }
            helper.succeed();
        });
    }
    @GameTest(maxTicks = 100)
    public void flightControlsMobWithTravelOverride(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        Allay allay = EntityType.ALLAY.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (allay == null) {
            helper.fail("allay type did not create an entity");
            return;
        }
        allay.setPos(player.position());
        helper.getLevel().addFreshEntity(allay);
        UUID id = storeAndRemove(player, allay);
        if (!KickLauncher.launch(player, 4).success()) {
            helper.fail("allay kick could not start");
            return;
        }
        Entity restored = helper.getLevel().getEntity(id);
        if (!(restored instanceof Allay kicked)) {
            helper.fail("kicked allay missing");
            return;
        }
        double startingZ = kicked.getZ();
        helper.runAfterDelay(3, () -> {
            if (!KickFlightController.isFlying(kicked) || kicked.getZ() <= startingZ) {
                helper.fail("travel override bypassed kick flight: age=" + kicked.tickCount
                        + " z=" + kicked.getZ() + " start=" + startingZ
                        + " flying=" + KickFlightController.isFlying(kicked)
                        + " velocity=" + kicked.getDeltaMovement());
            }
        });
        helper.runAfterDelay(80, () -> {
            if (KickFlightController.isFlying(kicked)) {
                helper.fail("allay kick flight did not terminate");
                return;
            }
            helper.succeed();
        });
    }

    private static ServerPlayer playerAt(GameTestHelper helper, float pitch) {
        // Fabric の空構造は外側を barrier で塞ぐため、このテスト用の開けた空間を明示する。
        for (int x = 4; x <= 12; x++) {
            for (int z = 4; z <= 12; z++) {
                for (int y = 2; y <= 7; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-mock-player"), ClientInformation.createDefault());
        // Fabric の空構造は Y=-56 に土台を置く。足を一段上へ出す。
        BlockPos position = helper.absolutePos(new BlockPos(8, 2, 8));
        player.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        player.setYRot(0.0F);
        player.setXRot(pitch);
        return player;
    }

    private static Cow carriedCow(GameTestHelper helper, ServerPlayer player) {
        Cow cow = EntityType.COW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (cow == null) {
            throw new IllegalStateException("cow type did not create an entity");
        }
        cow.setPos(player.position());
        helper.getLevel().addFreshEntity(cow);
        return cow;
    }

    private static UUID storeAndRemove(ServerPlayer player, Entity entity) {
        UUID id = entity.getUUID();
        CarryOnData carry = new CarryOnData(new CompoundTag());
        carry.setEntity(entity);
        CarryOnDataManager.setCarryData(player, carry);
        entity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        return id;
    }
}
