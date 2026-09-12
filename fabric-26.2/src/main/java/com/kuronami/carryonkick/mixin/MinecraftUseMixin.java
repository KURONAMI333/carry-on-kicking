package com.kuronami.carryonkick.mixin;

import com.kuronami.carryonkick.client.ClientKickController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftUseMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void carryOnKick$interceptUse(CallbackInfo callback) {
        if (ClientKickController.interceptUse((Minecraft) (Object) this)) {
            callback.cancel();
        }
    }
}
