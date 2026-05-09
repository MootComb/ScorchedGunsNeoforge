package top.ribs.scguns.common.exosuit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.SulfurVentBlock;
import top.ribs.scguns.item.exosuit.GasMaskModuleItem;

/**
 * Handles gas mask functionality for ExoSuit helmets
 */
@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ExoSuitGasMaskHandler {

    private static final int CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        Player player = event.getEntity();

        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }

        handleGasMask(player);
    }

    private static void handleGasMask(Player player) {
        if (!hasGasMaskModule(player)) {
            return;
        }

        boolean inGasArea = isPlayerInGasArea(player);

        if (!inGasArea) {
            return;
        }

        ItemStack gasMaskUpgrade = findGasMaskModule(player);
        if (!gasMaskUpgrade.isEmpty() && gasMaskUpgrade.getItem() instanceof GasMaskModuleItem gasMaskModule) {
            if (!gasMaskModule.canFunctionWithoutPower()) {
                ExoSuitPowerManager.consumeEnergyForUpgrade(player, "breathing", gasMaskUpgrade);
            }
        }

    }
    private static boolean hasGasMaskModule(Player player) {
        return !findGasMaskModule(player).isEmpty();
    }

    private static ItemStack findGasMaskModule(Player player) {
        // Get helmet
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof top.ribs.scguns.item.animated.ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.HELMET) {

                // Check for breathing upgrade (gas mask uses same slot as rebreather)
                for (int slot = 0; slot < 4; slot++) {
                    ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                    if (!upgradeItem.isEmpty()) {
                        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                        if (upgrade != null && upgrade.getType().equals("breathing") &&
                                upgradeItem.getItem() instanceof GasMaskModuleItem) {
                            return upgradeItem;
                        }
                    }
                }
                break;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isPlayerInGasArea(Player player) {
        BlockPos playerPos = player.blockPosition();

        int checkRadius = SulfurVentBlock.EFFECT_RADIUS + 2;

        for (BlockPos checkPos : BlockPos.betweenClosed(
                playerPos.offset(-checkRadius, -checkRadius, -checkRadius),
                playerPos.offset(checkRadius, checkRadius, checkRadius))) {

            BlockState state = player.level().getBlockState(checkPos);
            if (state.getBlock() instanceof SulfurVentBlock) {
                boolean isActive = state.getValue(SulfurVentBlock.ACTIVE);
                boolean isBaseVent = state.getValue(SulfurVentBlock.VENT_TYPE) == SulfurVentBlock.SulfurVentType.BASE;

                if (isActive && isBaseVent) {
                    double distanceSquared = playerPos.distSqr(checkPos);
                    if (distanceSquared <= SulfurVentBlock.EFFECT_RADIUS_SQUARED) {
                        return true;
                    }
                }
            }
        }

        // TODO: Could also check for gas grenade effects

        return false;
    }

    public static boolean hasProtection(Player player) {
        if (!hasGasMaskModule(player)) {
            return false;
        }

        ItemStack gasMaskUpgrade = findGasMaskModule(player);
        if (!gasMaskUpgrade.isEmpty() && gasMaskUpgrade.getItem() instanceof GasMaskModuleItem gasMaskModule) {
            if (gasMaskModule.canFunctionWithoutPower()) {
                return true;
            }

            return ExoSuitPowerManager.canUpgradeFunction(player, "breathing");
        }

        return false;
    }
}
