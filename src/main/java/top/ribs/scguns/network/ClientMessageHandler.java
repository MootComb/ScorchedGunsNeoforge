package top.ribs.scguns.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunModifierHelper;
@OnlyIn(Dist.CLIENT)
public class ClientMessageHandler {
    public static boolean handleUpdateAmmo(int ammoCount) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem) {
                CompoundTag tag = getCustomData(heldItem);
                tag.putInt("AmmoCount", ammoCount);
                int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(heldItem, ((GunItem)heldItem.getItem()).getModifiedGun(heldItem));
                if (ammoCount >= maxAmmo && tag.getBoolean("scguns:IsReloading")) {
                    tag.putString("scguns:ReloadState", "STOPPING");
                    tag.putBoolean("scguns:IsPlayingReloadStop", true);
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    tag.remove("scguns:IsReloading");
                }
                setCustomData(heldItem, tag);
            }
        }
        return true;
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
