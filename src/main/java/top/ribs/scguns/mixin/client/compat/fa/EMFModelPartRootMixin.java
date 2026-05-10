package top.ribs.scguns.mixin.client.compat.fa;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.compat.OptionalClientCompat;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

import java.util.Map;

@Mixin(targets = "traben.entity_model_features.models.parts.EMFModelPartRoot", remap = false)
@Pseudo
public abstract class EMFModelPartRootMixin {
    @Shadow(remap = false)
    @Final
    private Map<String, ModelPart> allVanillaParts;

    @Inject(method = "animate", at = @At("TAIL"), require = 0, remap = false)
    private void scguns$reapplyGunPoseAfterEntityModelFeatures(CallbackInfo ci) {
        try {
            if (OptionalClientCompat.isEntityModelFeaturesFirstPersonHand()) {
                return;
            }

            Object contextEntity = OptionalClientCompat.getEntityModelFeaturesContextEntity();
            if (!(contextEntity instanceof Player player)) {
                return;
            }

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof GunItem gunItem)) {
                return;
            }

            ModelPart rightArm = allVanillaParts.get("right_arm");
            ModelPart leftArm = allVanillaParts.get("left_arm");
            ModelPart head = allVanillaParts.get("head");
            if (rightArm == null || leftArm == null || head == null) {
                return;
            }

            rightArm.resetPose();
            leftArm.resetPose();
            float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
            float aimProgress = AimingHandler.get().getAimProgress(player, partialTick);
            Gun gun = gunItem.getModifiedGun(stack);
            gun.getGeneral()
                    .getGripType(stack)
                    .heldAnimation()
                    .applyPlayerModelRotation(player, rightArm, leftArm, head, InteractionHand.MAIN_HAND, aimProgress);

            copyPose(rightArm, allVanillaParts.get("right_sleeve"));
            copyPose(leftArm, allVanillaParts.get("left_sleeve"));
            copyPose(head, allVanillaParts.get("hat"));
            copyPose(head, allVanillaParts.get("headwear"));
        } catch (Throwable ignored) {
        }
    }

    private static void copyPose(ModelPart source, ModelPart target) {
        if (source != null && target != null) {
            target.copyFrom(source);
        }
    }
}
