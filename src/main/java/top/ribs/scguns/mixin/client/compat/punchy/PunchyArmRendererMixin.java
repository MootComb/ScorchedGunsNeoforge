package top.ribs.scguns.mixin.client.compat.punchy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.compat.OptionalClientCompat;

@Mixin(targets = "punchy.client.render.PunchyArmRenderer", remap = false)
@Pseudo
public abstract class PunchyArmRendererMixin {
    @Inject(method = "renderFirstPerson(Lnet/minecraft/client/renderer/ItemInHandRenderer;Lnet/minecraft/client/player/LocalPlayer;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void scguns$skipPunchyFirstPersonArmsForGuns(ItemInHandRenderer renderer, LocalPlayer player, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (OptionalClientCompat.isScorchedGunHeldByPlayer(player)) {
            ci.cancel();
        }
    }
}
