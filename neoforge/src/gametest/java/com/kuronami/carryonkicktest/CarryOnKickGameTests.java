package com.kuronami.carryonkicktest;

import com.kuronami.carryonkick.kick.KickFlightController;
import com.kuronami.carryonkick.kick.KickLauncher;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.monster.EnderMan;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

import java.util.UUID;

@GameTestHolder("carryonkick")
public final class CarryOnKickGameTests {
    private CarryOnKickGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty3x3x3")
    public static void restoresEntityNbtAndRejectsDuplicateRelease(GameTestHelper helper) {
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

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty3x3x3")
    public static void blockedSpawnKeepsCarriedEntity(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        Cow cow = carriedCow(helper, player);
        UUID id = storeAndRemove(player, cow);
        helper.setBlock(new BlockPos(8, 2, 9), Blocks.STONE);

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

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty3x3x3")
    public static void spawnPositionDoesNotDependOnViewPitch(GameTestHelper helper) {
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
        EnderMan enderMan = EntityType.ENDERMAN.create(helper.getLevel());
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

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty3x3x3")
    public static void nonMobUsesNormalCarryOnReleasePath(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        ArmorStand stand = EntityType.ARMOR_STAND.create(helper.getLevel());
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

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty3x3x3", timeoutTicks = 100)
    public static void flightStopsOnLanding(GameTestHelper helper) {
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
        helper.runAfterDelay(1, () -> {
            if (!KickFlightController.isFlying(kicked)) {
                helper.fail("flight controller stopped before airborne movement");
            } else if (kicked.getZ() <= startingZ) {
                helper.fail("flight controller did not advance the kicked cow");
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

    @PrefixGameTestTemplate(false)
    @GameTest(template = "empty3x3x3", timeoutTicks = 100)
    public static void flightControlsMobWithTravelOverride(GameTestHelper helper) {
        ServerPlayer player = playerAt(helper, 0.0F);
        Allay allay = EntityType.ALLAY.create(helper.getLevel());
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
        helper.runAfterDelay(1, () -> {
            if (!KickFlightController.isFlying(kicked) || kicked.getZ() <= startingZ) {
                helper.fail("travel override bypassed kick flight");
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
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-mock-player"), ClientInformation.createDefault());
        BlockPos position = helper.absolutePos(new BlockPos(8, 1, 8));
        player.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        player.setYRot(0.0F);
        player.setXRot(pitch);
        return player;
    }

    private static Cow carriedCow(GameTestHelper helper, ServerPlayer player) {
        Cow cow = EntityType.COW.create(helper.getLevel());
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
