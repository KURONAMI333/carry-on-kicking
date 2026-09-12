package com.kuronami.carryonkick.mixin;

import com.kuronami.carryonkick.client.ClientKickState;
import com.kuronami.carryonkick.client.KickAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    @Shadow public ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void carryonkick$poseLeg(net.minecraft.client.renderer.entity.state.HumanoidRenderState state,
                                     CallbackInfo ci) {
        if (!(state instanceof AvatarRenderState player) || player.isPassenger
                || player.hasPose(net.minecraft.world.entity.Pose.SLEEPING)
                || player.isVisuallySwimming || player.isFallFlying) {
            return;
        }
        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        var pose = KickAnimation.pose(ClientKickState.pose(player.id, partialTicks));
        if (pose.visible()) {
            leftLeg.xRot = -pose.forwardAngleRad();
            leftLeg.yRot = 0.0F;
            leftLeg.zRot = 0.0F;
            leftLeg.y -= pose.hipLift() * 16.0F;
        }
    }
}
