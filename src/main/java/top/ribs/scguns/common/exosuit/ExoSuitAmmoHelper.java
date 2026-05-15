// Add this helper class to integrate exo suit storage with ammo systems
package top.ribs.scguns.common.exosuit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.ItemStackHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.item.animated.ExoSuitItem;

import java.util.ArrayList;
import java.util.List;

public class ExoSuitAmmoHelper {

    /**
     * Check exo suit pouches for specific ammo item
     */
    public static ItemStack findAmmoInExoSuit(Player player, Item ammoItem) {
        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack pouchUpgrade = findPouchUpgrade(chestplate);
        if (pouchUpgrade.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null) {
            return ItemStack.EMPTY;
        }

        String pouchId = getPouchId(pouchUpgrade);
        ItemStackHandler pouchInventory = getPouchInventory(chestplate, pouchId, upgrade.getDisplay().getStorageSize());

        for (int i = 0; i < pouchInventory.getSlots(); i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ammoItem) {
                    return stack;
                }

                if (stack.getItem() instanceof AmmoBoxItem) {
                    try {
                        List<ItemStack> ammoBoxContents = AmmoBoxItem.getContents(stack).toList();
                        for (ItemStack ammoStack : ammoBoxContents) {
                            if (!ammoStack.isEmpty()) {
                               if (ammoStack.getItem() == ammoItem) {
                                    return ammoStack;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error reading AmmoBox contents: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        System.out.println("No ammo found in exo suit pouches");
        return ItemStack.EMPTY;
    }

    /**
     * Get all ammo stacks of a specific type from exo suit pouches
     */
    public static List<ItemStack> findAllAmmoInExoSuit(Player player, Item ammoItem) {
        List<ItemStack> ammoStacks = new ArrayList<>();

        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return ammoStacks;
        }

        ItemStack pouchUpgrade = findPouchUpgrade(chestplate);
        if (pouchUpgrade.isEmpty()) {
            return ammoStacks;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null) {
            return ammoStacks;
        }

        String pouchId = getPouchId(pouchUpgrade);
        ItemStackHandler pouchInventory = getPouchInventory(chestplate, pouchId, upgrade.getDisplay().getStorageSize());

        // Collect all direct ammo stacks
        for (int i = 0; i < pouchInventory.getSlots(); i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ammoItem) {
                ammoStacks.add(stack);
            }
        }

        // Collect ammo from ammo boxes inside the pouch
        for (int i = 0; i < pouchInventory.getSlots(); i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AmmoBoxItem) {
                try {
                    List<ItemStack> ammoBoxContents = AmmoBoxItem.getContents(stack).toList();
                    for (ItemStack ammoStack : ammoBoxContents) {
                        if (!ammoStack.isEmpty() && ammoStack.getItem() == ammoItem) {
                            ammoStacks.add(ammoStack);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error reading AmmoBox contents in findAllAmmoInExoSuit: " + e.getMessage());
                }
            }
        }

        return ammoStacks;
    }

    /**
     * Count total ammo of specific type in exo suit pouches
     */
    public static int getAmmoCountInExoSuit(Player player, Item ammoItem) {
        int totalCount = 0;

        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return totalCount;
        }

        ItemStack pouchUpgrade = findPouchUpgrade(chestplate);
        if (pouchUpgrade.isEmpty()) {
            return totalCount;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null) {
            return totalCount;
        }

        String pouchId = getPouchId(pouchUpgrade);
        ItemStackHandler pouchInventory = getPouchInventory(chestplate, pouchId, upgrade.getDisplay().getStorageSize());

        for (int i = 0; i < pouchInventory.getSlots(); i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ammoItem) {
                totalCount += stack.getCount();
            }
        }

        for (int i = 0; i < pouchInventory.getSlots(); i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AmmoBoxItem) {
                try {
                    List<ItemStack> ammoBoxContents = AmmoBoxItem.getContents(stack).toList();
                    for (ItemStack ammoStack : ammoBoxContents) {
                        if (!ammoStack.isEmpty() && ammoStack.getItem() == ammoItem) {
                            totalCount += ammoStack.getCount();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error reading AmmoBox contents in getAmmoCountInExoSuit: " + e.getMessage());
                }
            }
        }

        return totalCount;
    }

    /**
     * Try to add an item (like casings) to exo suit pouches
     * Returns true if successfully added
     */
    public static boolean addItemToExoSuit(Player player, ItemStack itemToAdd) {
        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return false;
        }

        ItemStack pouchUpgrade = findPouchUpgrade(chestplate);
        if (pouchUpgrade.isEmpty()) {
            return false;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null) {
            return false;
        }

        String pouchId = getPouchId(pouchUpgrade);
        ItemStackHandler pouchInventory = getPouchInventory(chestplate, pouchId, upgrade.getDisplay().getStorageSize());

        for (int i = 0; i < pouchInventory.getSlots(); i++) {
            ItemStack remaining = pouchInventory.insertItem(i, itemToAdd, false);
            if (remaining.isEmpty()) {
                savePouchInventory(chestplate, pouchId, pouchInventory);
                return true;
            } else if (remaining.getCount() < itemToAdd.getCount()) {
                itemToAdd.setCount(remaining.getCount());
            }
        }
        if (itemToAdd.getCount() < itemToAdd.getCount()) {
            savePouchInventory(chestplate, pouchId, pouchInventory);
        }

        return false;
    }

    /**
     * Shrink ammo from exo suit pouches
     */
    public static int shrinkAmmoInExoSuit(Player player, Item ammoItem, int amountToShrink) {
        if (amountToShrink <= 0) {
            return 0;
        }

        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return 0;
        }

        ItemStack pouchUpgrade = findPouchUpgrade(chestplate);
        if (pouchUpgrade.isEmpty()) {
            return 0;
        }

        ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchUpgrade);
        if (upgrade == null) {
            return 0;
        }

        String pouchId = getPouchId(pouchUpgrade);
        ItemStackHandler pouchInventory = getPouchInventory(chestplate, pouchId, upgrade.getDisplay().getStorageSize());

        int remainingToShrink = amountToShrink;
        int consumedTotal = 0;

        // First, shrink from direct ammo stacks in pouch inventory
        for (int i = 0; i < pouchInventory.getSlots() && remainingToShrink > 0; i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ammoItem) {
                int shrinkAmount = Math.min(remainingToShrink, stack.getCount());
                stack.shrink(shrinkAmount);
                remainingToShrink -= shrinkAmount;
                consumedTotal += shrinkAmount;

                if (stack.isEmpty()) {
                    pouchInventory.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
        for (int i = 0; i < pouchInventory.getSlots() && remainingToShrink > 0; i++) {
            ItemStack stack = pouchInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AmmoBoxItem) {
                try {
                    int consumed = AmmoBoxItem.shrinkAmmo(stack, ammoItem, remainingToShrink, player.registryAccess());
                    if (consumed > 0) {
                        pouchInventory.setStackInSlot(i, stack);
                        remainingToShrink -= consumed;
                        consumedTotal += consumed;
                    }
                } catch (Exception e) {
                    System.out.println("Error shrinking ammo from AmmoBox: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        if (consumedTotal > 0) {
            savePouchInventory(chestplate, pouchId, pouchInventory);
        }
        return consumedTotal;
    }

    private static ItemStack getEquippedChestplate(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findPouchUpgrade(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pouches")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static String getPouchId(ItemStack pouchUpgrade) {
        return pouchUpgrade.getItem().toString();
    }

    private static ItemStackHandler getPouchInventory(ItemStack chestplate, String pouchId, int size) {
        CompoundTag pouchData = getOrCreateCustomData(chestplate).getCompound("PouchData");

        ItemStackHandler handler = new ItemStackHandler(size);
        if (pouchData.contains(pouchId)) {
            handler.deserializeNBT(Gun.builtInRegistryProvider(), pouchData.getCompound(pouchId));
        }

        return handler;
    }

    private static void savePouchInventory(ItemStack chestplate, String pouchId, ItemStackHandler handler) {
        CompoundTag root = getOrCreateCustomData(chestplate);
        CompoundTag pouchData = root.getCompound("PouchData");
        pouchData.put(pouchId, handler.serializeNBT(Gun.builtInRegistryProvider()));
        root.put("PouchData", pouchData);
        setCustomData(chestplate, root);
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static CompoundTag getOrCreateCustomData(ItemStack stack) {
        return getCustomData(stack);
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
