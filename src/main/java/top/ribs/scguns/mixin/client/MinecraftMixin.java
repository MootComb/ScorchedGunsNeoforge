package top.ribs.scguns.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.ribs.scguns.item.GunItem;

/**
 * Author: MrCrayfish
 */
@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Redirect(method = "startUseItem()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;itemUsed(Lnet/minecraft/world/InteractionHand;)V"), remap = false)
    private void beforeItemUsed(ItemInHandRenderer renderer, InteractionHand hand)
    {
        Minecraft minecraft = (Minecraft) (Object) this;
        if(minecraft.player != null && minecraft.player.getItemInHand(hand).getItem() instanceof GunItem)
        {
            return;
        }
        renderer.itemUsed(hand);
    }
}
