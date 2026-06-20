package top.ribs.scguns.client.handler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
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
    public static void onComputeFOV(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getMainHandItem();

        boolean holdingGun = mainHand.getItem() instanceof GunItem;
        boolean isReloading = ModSyncedDataKeys.RELOADING.getValue(player);

        if (!holdingGun) {
            wasReloading = false;
            reloadEndCooldown = 0;
            return;
        }

        if (holdingGun) {
            if (isReloading) {
                if (!wasReloading) {
                    preReloadFOV = event.getNewFovModifier();
                    wasReloading = true;
                    reloadEndCooldown = 0;
                }
                event.setNewFovModifier(preReloadFOV);
            } else if (wasReloading) {
                wasReloading = false;
                reloadEndCooldown = COOLDOWN_DURATION;
                event.setNewFovModifier(preReloadFOV);
            } else if (reloadEndCooldown > 0) {
                event.setNewFovModifier(preReloadFOV);
            }
        } else {
            reloadEndCooldown = 0;
        }
    }
}
