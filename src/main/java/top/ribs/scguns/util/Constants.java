package top.ribs.scguns.util;

import net.minecraft.world.item.Rarity;

public class Constants {

    public static final Rarity OCEANIC = safeRarity("SCGUNS_OCEANIC", Rarity.RARE);
    public static final Rarity UNIQUE = safeRarity("SCGUNS_UNIQUE", Rarity.UNCOMMON);
    public static final Rarity PIGLISH = safeRarity("SCGUNS_PIGLISH", Rarity.RARE);
    public static final Rarity SCORCHED = safeRarity("SCGUNS_SCORCHED", Rarity.EPIC);
    public static final Rarity DEEP_DARK = safeRarity("SCGUNS_DEEP_DARK", Rarity.EPIC);
    public static final Rarity ENDISH = safeRarity("SCGUNS_ENDISH", Rarity.EPIC);
    public static final Rarity BIZARRE = safeRarity("SCGUNS_BIZARRE", Rarity.EPIC);

    private static Rarity safeRarity(String name, Rarity fallback) {
        try {
            return Rarity.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
