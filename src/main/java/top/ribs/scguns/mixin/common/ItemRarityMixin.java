package top.ribs.scguns.mixin.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.util.RarityUtils;

@SuppressWarnings("ALL")
@Mixin(ItemStack.class)
public abstract class ItemRarityMixin {
    @Inject(method = "getRarity()Lnet/minecraft/world/item/Rarity;", at = @At("RETURN"), cancellable = true, remap = false)
    private void changeRarity(CallbackInfoReturnable<Rarity> ci) {
        ItemStack stack = (ItemStack) (Object) this;
        ci.setReturnValue(RarityUtils.GetRarityFromItem(stack.getItem(), ci.getReturnValue()));
    }
}
