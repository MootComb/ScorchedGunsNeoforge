package top.ribs.scguns.mixin.client.compat.fa;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.client.compat.OptionalClientCompat;

@Mixin(value = PlayerRenderer.class, remap = false)
public abstract class PlayerRendererMixin {
    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void scguns$cancelFallbackFirstPersonHandForGun(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        OptionalClientCompat.cancelFallbackFirstPersonHandWhenGunHeld(player, ci);
    }
}
