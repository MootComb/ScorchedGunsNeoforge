package top.ribs.scguns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * Author: MrCrayfish
 */
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @Inject(method = "renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void renderArmWithItemHead(LivingEntity entity, ItemStack stack, ItemDisplayContext display, HumanoidArm arm, PoseStack poseStack, MultiBufferSource source, int light, CallbackInfo ci) {
        if (entity instanceof Player player) {
            InteractionHand hand = entity.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            if (hand == InteractionHand.OFF_HAND) {
                if (stack.getItem() instanceof GunItem) {
                    ci.cancel();
                    return;
                }

                if (entity.getMainHandItem().getItem() instanceof GunItem gunItem) {
                    Gun modifiedGun = gunItem.getModifiedGun(entity.getMainHandItem());
                    if (!modifiedGun.getGeneral().getGripType(stack).heldAnimation().canRenderOffhandItem()) {
                        ci.cancel();
                        return;
                    }
                }
            }
            if (stack.getItem() instanceof GunItem gunItem) {
                ci.cancel();
                ItemInHandLayer<?, ?> layer = (ItemInHandLayer<?, ?>) (Object) this;
                mrCrayfishGunMod$renderArmWithGun(layer, player, stack, gunItem, display, hand, arm, poseStack, source, light, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
            }
        } else if (stack.getItem() instanceof GunItem gunItem) {
            ci.cancel();
            InteractionHand hand = entity.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemInHandLayer<?, ?> layer = (ItemInHandLayer<?, ?>) (Object) this;
            mrCrayfishGunMod$renderArmWithGun(layer, entity, stack, gunItem, display, hand, arm, poseStack, source, light, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        }
    }

    @Unique
    private static void mrCrayfishGunMod$renderArmWithGun(ItemInHandLayer<?, ?> layer, LivingEntity entity, ItemStack stack, GunItem item, ItemDisplayContext display, InteractionHand hand, HumanoidArm arm, PoseStack poseStack, MultiBufferSource source, int light, float deltaTicks) {
        poseStack.pushPose();
        layer.getParentModel().translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180F));
        poseStack.translate(((float) (arm == HumanoidArm.LEFT ? -1 : 1) / 16F), 0.125, -0.625);
        GunRenderingHandler.get().applyWeaponScale(stack, poseStack);
        Gun gun = item.getModifiedGun(stack);
        if (entity instanceof Player player) {
            gun.getGeneral().getGripType(stack).heldAnimation().applyHeldItemTransforms(player, hand, AimingHandler.get().getAimProgress(player, deltaTicks), poseStack, source);
        }
        if (stack.getItem() instanceof AnimatedGunItem) {
            poseStack.scale(1.0F, 1.0F, 1.0F);
        }
        GunRenderingHandler.get().renderWeapon(entity, stack, display, poseStack, source, light, deltaTicks);
        poseStack.popPose();
    }
}
