package com.kuronami.carryonkick.mixin;

import com.kuronami.carryonkick.kick.KickFlightController;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** LivingEntity.travelを上書きするvanilla Mobにも同じ弾道処理を適用する。 */
@Mixin({
        Ghast.class,
        Phantom.class,
        HappyGhast.class,
        Squid.class,
        Allay.class,
        Camel.class
})
public abstract class VanillaMobTravelMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void carryOnKick$applyFlight(Vec3 travelVector, CallbackInfo callback) {
        if (KickFlightController.travel((LivingEntity) (Object) this)) {
            callback.cancel();
        }
    }
}
