package top.ribs.scguns.init;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import top.ribs.scguns.Reference;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ModArmorMaterials {
    private static final Map<Holder<ArmorMaterial>, Integer> DURABILITY_FACTORS = new IdentityHashMap<>();

    public static final Holder<ArmorMaterial> ADRIEN = register("adrien", 22, new int[]{3, 6, 6, 4}, 8,
            SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 0.1f, () -> Ingredient.of(ModItems.TREATED_IRON_INGOT.get()));
    public static final Holder<ArmorMaterial> ANTHRALITE = register("anthralite", 32, new int[]{2, 4, 3, 2}, 12,
            SoundEvents.ARMOR_EQUIP_GOLD, 1.0f, 0.05f, () -> Ingredient.of(ModItems.ANTHRALITE_INGOT.get()));
    public static final Holder<ArmorMaterial> DIAMOND_STEEL = register("diamond_steel", 36, new int[]{3, 6, 5, 3}, 16,
            SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0f, 0.05f, () -> Ingredient.of(ModItems.DIAMOND_STEEL_INGOT.get()));
    public static final Holder<ArmorMaterial> TREATED_BRASS = register("treated_brass", 30, new int[]{4, 6, 5, 4}, 10,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.2f, () -> Ingredient.of(ModItems.TREATED_BRASS_INGOT.get()));
    public static final Holder<ArmorMaterial> ANCIENT_BRASS = register("ancient_brass", 16, new int[]{3, 5, 4, 3}, 10,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.15f, () -> Ingredient.of(ModItems.ANCIENT_BRASS.get()));
    public static final Holder<ArmorMaterial> EXO_SUIT = register("exo_suit", 200, new int[]{1, 1, 1, 1}, 6,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 0.0f, 0.0f, () -> Ingredient.of(ModItems.TREATED_IRON_INGOT.get()));

    private ModArmorMaterials() {
    }

    public static Item.Properties withDurability(Item.Properties properties, Holder<ArmorMaterial> material, ArmorItem.Type type) {
        return properties.durability(type.getDurability(durabilityFactor(material)));
    }

    private static Holder<ArmorMaterial> register(String name, int durabilityFactor, int[] defense, int enchantmentValue,
                                                  Holder<SoundEvent> equipSound, float toughness, float knockbackResistance,
                                                  Supplier<Ingredient> repairIngredient) {
        EnumMap<ArmorItem.Type, Integer> defenseMap = new EnumMap<>(ArmorItem.Type.class);
        defenseMap.put(ArmorItem.Type.BOOTS, defense[0]);
        defenseMap.put(ArmorItem.Type.LEGGINGS, defense[1]);
        defenseMap.put(ArmorItem.Type.CHESTPLATE, defense[2]);
        defenseMap.put(ArmorItem.Type.HELMET, defense[3]);
        defenseMap.put(ArmorItem.Type.BODY, defense[2]);

        Holder<ArmorMaterial> holder = Holder.direct(new ArmorMaterial(
                defenseMap,
                enchantmentValue,
                equipSound,
                repairIngredient,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, name))),
                toughness,
                knockbackResistance
        ));
        DURABILITY_FACTORS.put(holder, durabilityFactor);
        return holder;
    }

    private static int durabilityFactor(Holder<ArmorMaterial> material) {
        Integer factor = DURABILITY_FACTORS.get(material);
        if (factor != null) {
            return factor;
        }
        if (material == ArmorMaterials.LEATHER) {
            return 5;
        }
        if (material == ArmorMaterials.GOLD) {
            return 7;
        }
        if (material == ArmorMaterials.CHAIN || material == ArmorMaterials.IRON) {
            return 15;
        }
        if (material == ArmorMaterials.TURTLE) {
            return 25;
        }
        if (material == ArmorMaterials.DIAMOND) {
            return 33;
        }
        if (material == ArmorMaterials.NETHERITE) {
            return 37;
        }
        return 15;
    }
}
