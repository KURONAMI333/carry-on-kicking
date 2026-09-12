package com.kuronami.carryonkick.mixin;

import com.kuronami.carryonkick.kick.KickFlightController;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** LivingEntity.travelを上書きするvanilla Mobにも同じ弾道処理を適用する。 */
@Mixin({
        FlyingMob.class,
        AbstractFish.class,
        Dolphin.class,
        Squid.class,
        Turtle.class,
        Allay.class,
        Axolotl.class,
        Camel.class,
        Frog.class,
        Drowned.class,
        Guardian.class
})
public abstract class VanillaMobTravelMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void carryOnKick$applyFlight(Vec3 travelVector, CallbackInfo callback) {
        if (KickFlightController.travel((LivingEntity) (Object) this)) {
            callback.cancel();
        }
    }
}
