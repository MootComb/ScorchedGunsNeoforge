package top.ribs.scguns.common.exosuit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import top.ribs.scguns.common.Gun;

/**
 * Helper class for managing ExoSuit upgrade data
 */
public class ExoSuitData {

    // NBT keys for storing upgrade data
    private static final String UPGRADES_TAG = "ExoSuitUpgrades";
    private static final String HELMET_UPGRADES = "HelmetUpgrades";
    private static final String CHEST_UPGRADES = "ChestUpgrades";
    private static final String LEG_UPGRADES = "LegUpgrades";
    private static final String BOOT_UPGRADES = "BootUpgrades";

    /**
     * Gets the upgrade data compound from an exosuit piece
     */
    public static CompoundTag getUpgradeData(ItemStack exosuitPiece) {
        if (exosuitPiece.isEmpty()) {
            return new CompoundTag();
        }
        return getCustomData(exosuitPiece).getCompound(UPGRADES_TAG);
    }

    /**
     * Sets upgrade data on an exosuit piece
     */
    public static void setUpgradeData(ItemStack exosuitPiece, CompoundTag upgradeData) {
        if (!exosuitPiece.isEmpty()) {
            CompoundTag root = getCustomData(exosuitPiece);
            root.put(UPGRADES_TAG, upgradeData);
            setCustomData(exosuitPiece, root);
        }
    }

    /**
     * Checks if an exosuit piece has any upgrades installed
     */
    public static boolean hasUpgrades(ItemStack exosuitPiece) {
        CompoundTag upgrades = getUpgradeData(exosuitPiece);
        if (upgrades.contains("Upgrades")) {
            ListTag upgradeList = upgrades.getList("Upgrades", 10);
            return !upgradeList.isEmpty();
        }
        return false;
    }

    /**
     * Gets an upgrade item from a specific slot
     */
    public static ItemStack getUpgradeInSlot(ItemStack exosuitPiece, int slot) {
        CompoundTag upgradeData = getUpgradeData(exosuitPiece);

        if (upgradeData.contains("Upgrades")) {
            ListTag upgradeList = upgradeData.getList("Upgrades", 10);

            for (int i = 0; i < upgradeList.size(); i++) {
                CompoundTag slotTag = upgradeList.getCompound(i);
                if (slotTag.getInt("Slot") == slot) {
                    if (slotTag.contains("Item")) {
                        return ItemStack.parseOptional(Gun.builtInRegistryProvider(), slotTag.getCompound("Item"));
                    }
                }
            }
        }

        return ItemStack.EMPTY;
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
