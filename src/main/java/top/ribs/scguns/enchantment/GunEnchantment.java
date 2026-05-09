package top.ribs.scguns.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public abstract class GunEnchantment
{
    private final Rarity rarity;
    private final Predicate<Item> supportedItems;
    private final Type type;

    protected GunEnchantment(Rarity rarityIn, Predicate<Item> supportedItems, EquipmentSlot[] slots, Type type)
    {
        this.rarity = rarityIn;
        this.supportedItems = supportedItems;
        this.type = type;
    }

    protected boolean checkCompatibility(GunEnchantment enchantment)
    {
        return enchantment.type != this.type;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public Predicate<Item> getSupportedItems() {
        return this.supportedItems;
    }

    public Type getType() {
        return this.type;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinCost(int level) {
        return 1 + level * 10;
    }

    public int getMaxCost(int level) {
        return this.getMinCost(level) + 5;
    }

    public boolean isTreasureOnly() {
        return false;
    }

    public boolean isCurse() {
        return false;
    }

    public static Enchantment build(ResourceLocation id, Predicate<Item> supportedItems, Rarity rarity, int maxLevel,
                                    int minCostBase, int minCostPerLevel, int maxCostBase, int maxCostPerLevel,
                                    HolderSet<Enchantment> exclusiveSet) {
        HolderSet<Item> supported = supportedItems(supportedItems);
        Enchantment.EnchantmentDefinition definition = Enchantment.definition(
                supported,
                supported,
                rarity.weight(),
                maxLevel,
                Enchantment.dynamicCost(minCostBase, minCostPerLevel),
                Enchantment.dynamicCost(maxCostBase, maxCostPerLevel),
                rarity.anvilCost(),
                EquipmentSlotGroup.MAINHAND
        );
        return Enchantment.enchantment(definition)
                .exclusiveWith(exclusiveSet)
                .build(id);
    }

    private static HolderSet<Item> supportedItems(Predicate<Item> predicate) {
        List<Holder<Item>> holders = BuiltInRegistries.ITEM.holders()
                .filter(holder -> predicate.test(holder.value()))
                .map(holder -> (Holder<Item>) holder)
                .toList();
        return HolderSet.direct(holders);
    }

    public enum Rarity
    {
        COMMON(10, 1),
        UNCOMMON(5, 2),
        RARE(2, 4),
        VERY_RARE(1, 8);

        private final int weight;
        private final int anvilCost;

        Rarity(int weight, int anvilCost) {
            this.weight = weight;
            this.anvilCost = anvilCost;
        }

        public int weight() {
            return this.weight;
        }

        public int anvilCost() {
            return this.anvilCost;
        }
    }

    public enum Type
    {
        WEAPON, AMMO, PROJECTILE
    }
}
