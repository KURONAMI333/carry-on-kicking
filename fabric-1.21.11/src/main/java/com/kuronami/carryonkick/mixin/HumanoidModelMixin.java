package com.kuronami.carryonkick.mixin;

import com.kuronami.carryonkick.client.ClientKickState;
import com.kuronami.carryonkick.client.KickAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    @Shadow public ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void carryonkick$poseLeg(HumanoidRenderState state, CallbackInfo ci) {
        if (!(state instanceof AvatarRenderState avatar) || state.isPassenger
                || state.pose == Pose.SLEEPING || state.isVisuallySwimming || state.isFallFlying) {
            return;
        }
        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        var pose = KickAnimation.pose(ClientKickState.pose(avatar.id, partialTicks));
        if (pose.visible()) {
            leftLeg.xRot = -pose.forwardAngleRad();
            leftLeg.yRot = 0;
            leftLeg.zRot = 0;
            leftLeg.y -= pose.hipLift() * 16;
        }
    }
}
