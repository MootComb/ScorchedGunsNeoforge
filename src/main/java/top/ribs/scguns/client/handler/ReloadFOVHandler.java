package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ReloadFOVHandler {

    private static float preReloadFOV = 1.0f;
    private static boolean wasReloading = false;
    private static int reloadEndCooldown = 0;
    private static final int COOLDOWN_DURATION = 3;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (reloadEndCooldown > 0) {
            reloadEndCooldown--;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeFOV(ViewportEvent.ComputeFov event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean holdingGun = (mainHand.getItem() instanceof GunItem) || (offHand.getItem() instanceof GunItem);
        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);

        if (wasReloading && !holdingGun) {
            wasReloading = false;
            reloadEndCooldown = 0;
            return;
        }

        if (holdingGun) {
            if (isReloading) {
                if (!wasReloading) {
                    preReloadFOV = (float) event.getFOV();
                    wasReloading = true;
                    reloadEndCooldown = 0;
                }
                event.setFOV(preReloadFOV);
            } else if (wasReloading) {
                wasReloading = false;
                reloadEndCooldown = COOLDOWN_DURATION;
                event.setFOV(preReloadFOV);
            } else if (reloadEndCooldown > 0) {
                event.setFOV(preReloadFOV);
            }
        } else {
            reloadEndCooldown = 0;
        }
    }
}
