package com.kuronami.carryonkick.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.kuronami.carryonkick.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.PlayerModelPart;

/** プレイヤーのスキンで一人称の蹴り脚を描く。動作の時間と軌道は呼出側が指定する。 */
public final class KickLegRenderer {
    private static ModelPart leg;
    private static ModelPart trousers;

    private KickLegRenderer() {
    }

    /**
     * バニラと同じ脚の形状・スキンUVを、蹴りの姿勢で描く。
     * 専有モデルを使い、他プレイヤーや通常描画のModelPartを変更しない。
     *
     * @param forwardAngleRad 前へ振り出す角度（下向きの脚を0、前方水平を正のπ/2とする）
     * @param hipLift 腰の持ち上げ量（ブロック）
     */
    public static void render(PoseStack poseStack, MultiBufferSource buffer,
                              AbstractClientPlayer player, int light,
                              float forwardAngleRad, float hipLift) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getCameraEntity() != player || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.options.hideGui || player.isSpectator() || player.isInvisible()
                || player.isSleeping() || player.isVisuallySwimming() || player.isFallFlying()) {
            return;
        }
        if (Services.PLATFORM.isModLoaded("firstperson") || Services.PLATFORM.isModLoaded("firstpersonmod")) {
            // 全身表示MODでは三人称モデルの脚が既に描かれるため、追加の脚を重ねない。
            return;
        }
        if (!Float.isFinite(forwardAngleRad) || !Float.isFinite(hipLift)) {
            return;
        }
        if (leg == null) {
            ModelPart root = LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64).bakeRoot();
            leg = root.getChild("left_leg");
            trousers = root.getChild("left_pants");
        }

        leg.setPos(0.0F, 0.0F, 0.0F);
        leg.setRotation(-forwardAngleRad, 0.0F, 0.0F);
        trousers.copyFrom(leg);
        poseStack.pushPose();
        try {
            // バニラ脚の付根は地面から12/16、左右差は1.9/16ブロック。
            poseStack.translate(-1.9 / 16.0, 12.0 / 16.0 - player.getEyeHeight() + hipLift, 0.0);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            var texture = player.getSkin().texture();
            leg.render(poseStack, buffer.getBuffer(RenderType.entitySolid(texture)), light, OverlayTexture.NO_OVERLAY);
            if (player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG)) {
                trousers.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(texture)), light, OverlayTexture.NO_OVERLAY);
            }
        } finally {
            poseStack.popPose();
        }
    }
}
