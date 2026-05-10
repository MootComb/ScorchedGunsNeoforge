package top.ribs.scguns.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.ribs.scguns.entity.player.PlayerGunProgression;

public class GunProgressionEventHandler {
    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        update(event.getPlayer(), event.getOriginalStack(), true);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        update(event.getEntity(), event.getCrafting(), true);
    }

    @SubscribeEvent
    public static void onPlayerEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() == EquipmentSlot.MAINHAND && event.getEntity() instanceof Player player) {
            update(player, event.getTo(), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        for (ItemStack stack : player.getInventory().items) {
            update(player, stack, false);
        }
    }

    private static void update(Player player, ItemStack stack, boolean notify) {
        PlayerGunProgression before = PlayerGunProgression.get(player);
        if (PlayerGunProgression.updateAndSave(player, stack) && notify) {
            PlayerGunProgression after = PlayerGunProgression.get(player);
            if (after.getCurrentTier() != before.getCurrentTier()) {
                sendTierUnlockedMessage(player, after.getCurrentTier());
            }
        }
    }

    public static void sendTierUnlockedMessage(Player player, PlayerGunProgression.GunTier tier) {
        player.displayClientMessage(Component.translatable("message.scguns.gun_tier_unlocked", tier.name().toLowerCase()), true);
    }
}
