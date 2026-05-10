package top.ribs.scguns.mixin.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunModifierHelper;

/**
 * Author: MrCrayfish
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
    @ModifyArg(method = "turnPlayer(D)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V", remap = false), index = 0, remap = false)
    private double scguns$scaleAimingYaw(double original)
    {
        return original * this.scguns$getAimingSensitivityMultiplier();
    }

    @ModifyArg(method = "turnPlayer(D)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V", remap = false), index = 1, remap = false)
    private double scguns$scaleAimingPitch(double original)
    {
        return original * this.scguns$getAimingSensitivityMultiplier();
    }

    private double scguns$getAimingSensitivityMultiplier()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.player.getMainHandItem().isEmpty() || mc.options.getCameraType() != CameraType.FIRST_PERSON)
        {
            return 1.0D;
        }

        ItemStack heldItem = mc.player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem gunItem) || !AimingHandler.get().isAiming() || ModSyncedDataKeys.RELOADING.getValue(mc.player))
        {
            return 1.0D;
        }

        double adsSensitivity = Config.CLIENT.controls.aimDownSightSensitivity.get();
        double multiplier = 1.0D - (1.0D - adsSensitivity) * AimingHandler.get().getNormalisedAdsProgress();

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(modifiedGun.getModules().getZoom() != null)
        {
            float modifier = Gun.getFovModifier(heldItem, modifiedGun);
            multiplier *= Mth.clamp(1.0F - (1.0F / modifier) / 10F, 0.0F, 1.0F);
            multiplier *= GunModifierHelper.getModifiedMouseSensitivity(heldItem, 1.0D);
        }
        return Math.max(0.01D, multiplier);
    }
}
