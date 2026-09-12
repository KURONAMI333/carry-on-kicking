package com.kuronami.carryonkick.client;

import com.kuronami.carryonkick.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.PlayerModelPart;

/** 26.2のSubmitNodeCollectorへ一人称の蹴り脚を送る。 */
public final class KickLegRenderer {
    private static ModelPart leg;
    private static ModelPart trousers;

    private KickLegRenderer() {
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector,
                              AbstractClientPlayer player, int light,
                              float forwardAngleRad, float hipLift) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() != player || !minecraft.options.getCameraType().isFirstPerson()
                || player.isSpectator() || player.isInvisible() || player.isSleeping()
                || player.isVisuallySwimming() || player.isFallFlying()
                || Services.PLATFORM.isModLoaded("firstperson") || Services.PLATFORM.isModLoaded("firstpersonmod")
                || !Float.isFinite(forwardAngleRad) || !Float.isFinite(hipLift)) {
            return;
        }
        if (leg == null) {
            ModelPart root = LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64).bakeRoot();
            leg = root.getChild("left_leg");
            trousers = root.getChild("left_pants");
        }
        leg.setPos(0.0F, 0.0F, 0.0F);
        leg.setRotation(-forwardAngleRad, 0.0F, 0.0F);
        trousers.loadPose(leg.storePose());
        poseStack.pushPose();
        try {
            poseStack.translate(-1.9 / 16.0, 12.0 / 16.0 - player.getEyeHeight() + hipLift, 0.0);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            var texture = player.getSkin().body().texturePath();
            collector.submitModelPart(leg, poseStack, RenderTypes.entitySolid(texture), light,
                    OverlayTexture.NO_OVERLAY, null);
            if (player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG)) {
                collector.submitModelPart(trousers, poseStack, RenderTypes.entityTranslucent(texture), light,
                        OverlayTexture.NO_OVERLAY, null);
            }
        } finally {
            poseStack.popPose();
        }
    }
}
