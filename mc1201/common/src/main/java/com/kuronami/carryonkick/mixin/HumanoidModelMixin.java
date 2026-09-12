package com.kuronami.carryonkick.mixin;

import com.kuronami.carryonkick.client.ClientKickState;
import com.kuronami.carryonkick.client.KickAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    @Shadow public ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void carryonkick$poseLeg(LivingEntity entity, float limbSwing, float limbAmount,
                                    float age, float headYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player) || entity.isPassenger() || entity.isSleeping()
                || entity.isVisuallySwimming() || entity.isFallFlying()) {
            return;
        }
        float partialTicks = Minecraft.getInstance().getFrameTime();
        var pose = KickAnimation.pose(ClientKickState.pose(entity.getId(), partialTicks));
        if (pose.visible()) {
            leftLeg.xRot = -pose.forwardAngleRad();
            leftLeg.yRot = 0;
            leftLeg.zRot = 0;
            leftLeg.y -= pose.hipLift() * 16;
        }
    }
}
