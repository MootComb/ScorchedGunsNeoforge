package top.ribs.scguns.common.exosuit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.ExoSuitItem;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ExoSuitFlightHandler {

    private static final int JETPACK_ENERGY_INTERVAL = 20;
    private static final int JETPACK_FALL_PROTECTION_GRACE_TICKS = 30;
    private static final Map<UUID, Long> LAST_JETPACK_FLIGHT_TICK = new ConcurrentHashMap<>();
    private static final Set<UUID> SERVER_ACTIVE_JETPACKS = ConcurrentHashMap.newKeySet();

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

        boolean jetpackActive = isJetpackFlightActive(player) || isServerJetpackFlightActive(player);
        if (jetpackActive) {
            markJetpackFlight(player);
            player.resetFallDistance();

            if (player.onGround()) {
                return;
            }

            if (player.isSprinting()) {
                player.setSprinting(false);
            }

            if (!player.level().isClientSide &&
                    ExoSuitPowerManager.canConsumeEnergy(player, "utility", JETPACK_ENERGY_INTERVAL) &&
                    !consumeJetpackEnergy(player)) {
                setJetpackFlightActive(player, false);
                disableFlight(player);
            }
        } else if (player.onGround()) {
            clearExpiredJetpackFlight(player);
        }
    }

    public static boolean isJetpackFlightActive(Player player) {
        return player.getAbilities().flying && !player.isCreative() && !player.isSpectator() && isUsingJetpack(player);
    }

    public static void setJetpackFlightActive(Player player, boolean active) {
        if (player.level().isClientSide) {
            return;
        }

        if (active && isUsingJetpack(player)) {
            SERVER_ACTIVE_JETPACKS.add(player.getUUID());
            markJetpackFlight(player);
            player.resetFallDistance();
        } else {
            SERVER_ACTIVE_JETPACKS.remove(player.getUUID());
            player.resetFallDistance();
        }
    }

    public static void cleanupPlayerData(UUID playerId) {
        SERVER_ACTIVE_JETPACKS.remove(playerId);
        LAST_JETPACK_FLIGHT_TICK.remove(playerId);
    }

    private static boolean isServerJetpackFlightActive(Player player) {
        if (player.level().isClientSide || player.isCreative() || player.isSpectator()) {
            return false;
        }

        boolean active = SERVER_ACTIVE_JETPACKS.contains(player.getUUID()) && isUsingJetpack(player);
        if (!active) {
            SERVER_ACTIVE_JETPACKS.remove(player.getUUID());
        }
        return active;
    }

    public static void markJetpackFlight(Player player) {
        LAST_JETPACK_FLIGHT_TICK.put(player.getUUID(), player.level().getGameTime());
    }

    public static boolean wasRecentlyJetpackFlying(Player player) {
        Long lastJetpackFlightTick = LAST_JETPACK_FLIGHT_TICK.get(player.getUUID());
        if (lastJetpackFlightTick == null) {
            return false;
        }

        long elapsedTicks = player.level().getGameTime() - lastJetpackFlightTick;
        if (elapsedTicks <= JETPACK_FALL_PROTECTION_GRACE_TICKS && isUsingJetpack(player)) {
            return true;
        }

        LAST_JETPACK_FLIGHT_TICK.remove(player.getUUID());
        return false;
    }

    private static void clearExpiredJetpackFlight(Player player) {
        Long lastJetpackFlightTick = LAST_JETPACK_FLIGHT_TICK.get(player.getUUID());
        if (lastJetpackFlightTick == null) {
            return;
        }

        if (player.level().getGameTime() - lastJetpackFlightTick > JETPACK_FALL_PROTECTION_GRACE_TICKS) {
            LAST_JETPACK_FLIGHT_TICK.remove(player.getUUID());
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
        markJetpackFlight(player);
        player.resetFallDistance();
        if (!player.level().isClientSide) {
            SERVER_ACTIVE_JETPACKS.remove(player.getUUID());
        }
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.getAbilities().setFlyingSpeed(0.05f);
        player.onUpdateAbilities();
    }
}
