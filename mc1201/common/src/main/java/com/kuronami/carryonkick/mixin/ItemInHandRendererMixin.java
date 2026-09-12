package com.kuronami.carryonkick.mixin;

import com.kuronami.carryonkick.client.ClientKickState;
import com.kuronami.carryonkick.client.KickAnimation;
import com.kuronami.carryonkick.client.KickLegRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    @Inject(method = "renderHandsWithItems", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
    private void carryonkick$renderLeg(float partialTicks, PoseStack poses,
                                      MultiBufferSource.BufferSource buffers, LocalPlayer player,
                                      int light, CallbackInfo ci) {
        var pose = KickAnimation.pose(ClientKickState.pose(player.getId(), partialTicks));
        if (pose.visible()) {
            KickLegRenderer.render(poses, buffers, player, light, pose.forwardAngleRad(), pose.hipLift());
        }
    }
}
