package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ExoSuitItem;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ExoSuitFlightHandler {

    private static final int JETPACK_ENERGY_INTERVAL = 20;

    static boolean consumeJetpackEnergy(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        if (!(chestplate.getItem() instanceof ExoSuitItem)) {
            return false;
        }
        ItemStack jetpackUpgrade = findJetpackModule(chestplate);
        if (jetpackUpgrade.isEmpty()) {
            return false;
        }
        return ExoSuitPowerManager.consumeEnergyForUpgrade(player, "utility", jetpackUpgrade);
    }

    private static ItemStack findJetpackModule(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("utility") && upgrade.getEffects().hasFlight()) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (player.getAbilities().flying && !player.isCreative() && !player.isSpectator() && isUsingJetpack(player)) {
            if (player.isSprinting()) {
                player.setSprinting(false);
            }

            if (ExoSuitPowerManager.canConsumeEnergy(player, "utility", JETPACK_ENERGY_INTERVAL) &&
                    !consumeJetpackEnergy(player)) {
                disableFlight(player);
            }
        }
    }

    private static boolean isUsingJetpack(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        if (!(chestplate.getItem() instanceof ExoSuitItem)) {
            return false;
        }

        ExoSuitUpgrade.Effects totalEffects = ExoSuitEffectsHandler.getTotalEffects(player);
        boolean hasFlightCapability = totalEffects.hasFlight();
        boolean utilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "utility");
        boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "utility");

        return hasFlightCapability && utilityEnabled && canFunction;
    }

    static void enableFlight(Player player, float flightSpeed) {
        player.getAbilities().mayfly = true;
        player.getAbilities().setFlyingSpeed(flightSpeed);
        player.onUpdateAbilities();
    }

    static void disableFlight(Player player) {
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.getAbilities().setFlyingSpeed(0.05f);
        player.onUpdateAbilities();
    }
}
