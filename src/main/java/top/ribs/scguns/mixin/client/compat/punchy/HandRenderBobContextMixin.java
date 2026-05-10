package top.ribs.scguns.mixin.client.compat.punchy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.compat.OptionalClientCompat;

@Mixin(targets = "punchy.client.render.HandRenderBobContext", remap = false)
@Pseudo
public abstract class HandRenderBobContextMixin {
    @Shadow(remap = false)
    private static float lastWalkDist;
    @Shadow(remap = false)
    private static float lastBob;
    @Shadow(remap = false)
    private static boolean hasBobSample;

    @Inject(method = "updateBobSample(FF)V", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void scguns$ignorePunchyBobSampleForGuns(float walkDist, float bob, CallbackInfo ci) {
        if (!OptionalClientCompat.isScorchedGunHeldByLocalPlayer()) {
            return;
        }
        lastWalkDist = 0.0F;
        lastBob = 0.0F;
        hasBobSample = false;
        ci.cancel();
    }

    @Inject(method = "hasBobSample()Z", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void scguns$hidePunchyBobSampleForGuns(CallbackInfoReturnable<Boolean> cir) {
        if (OptionalClientCompat.isScorchedGunHeldByLocalPlayer()) {
            cir.setReturnValue(false);
        }
    }
}
