package top.ribs.scguns.mixin.client.compat.punchy;

import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.compat.OptionalClientCompat;

@Mixin(targets = "punchy.client.state.MovementStateMachine", remap = false)
@Pseudo
public abstract class MovementStateMachineMixin {
    @Inject(method = "applyToMatrix(Lorg/joml/Matrix4f;FLnet/minecraft/world/entity/HumanoidArm;)V",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void scguns$skipArmWalkMatrixForGuns(Matrix4f matrix, float partialTick, HumanoidArm arm, CallbackInfo ci) {
        if (shouldBypassPunchyMovement()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyRootSpaceMotion(Lorg/joml/Matrix4f;FLnet/minecraft/world/entity/HumanoidArm;)V",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void scguns$skipRootWalkMatrixForGuns(Matrix4f matrix, float partialTick, HumanoidArm arm, CallbackInfo ci) {
        if (shouldBypassPunchyMovement()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyCameraLookToRoot(Lorg/joml/Matrix4f;)V",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void scguns$skipCameraLookRootMatrixForGuns(Matrix4f matrix, CallbackInfo ci) {
        if (shouldBypassPunchyMovement()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyCameraYawLagToArm(Lorg/joml/Matrix4f;)V",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void scguns$skipCameraYawLagForGuns(Matrix4f matrix, CallbackInfo ci) {
        if (shouldBypassPunchyMovement()) {
            ci.cancel();
        }
    }

    @Inject(method = "isSprintSwingActive()Z",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void scguns$disableSprintSwingForGuns(CallbackInfoReturnable<Boolean> cir) {
        if (shouldBypassPunchyMovement()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isWalkSwingActive()Z",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void scguns$disableWalkSwingForGuns(CallbackInfoReturnable<Boolean> cir) {
        if (shouldBypassPunchyMovement()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getSprintBlendAlpha()F",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void scguns$zeroSprintBlendForGuns(CallbackInfoReturnable<Float> cir) {
        if (shouldBypassPunchyMovement()) {
            cir.setReturnValue(0.0F);
        }
    }

    private static boolean shouldBypassPunchyMovement() {
        return OptionalClientCompat.isScorchedGunHeldByLocalPlayer();
    }
}
