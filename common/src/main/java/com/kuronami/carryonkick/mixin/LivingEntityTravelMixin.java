package com.kuronami.carryonkick.mixin;

import com.kuronami.carryonkick.kick.KickFlightController;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTravelMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void carryOnKick$applyFlight(Vec3 travelVector, CallbackInfo callback) {
        if (KickFlightController.travel((LivingEntity) (Object) this)) {
            callback.cancel();
        }
    }
}
