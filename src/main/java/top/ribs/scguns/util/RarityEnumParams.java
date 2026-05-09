package top.ribs.scguns.util;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class RarityEnumParams {
    public static final EnumProxy<Rarity> OCEANIC = new EnumProxy<>(Rarity.class, -1, "scguns:oceanic", ChatFormatting.BLUE);
    public static final EnumProxy<Rarity> UNIQUE = new EnumProxy<>(Rarity.class, -1, "scguns:unique", ChatFormatting.GREEN);
    public static final EnumProxy<Rarity> PIGLISH = new EnumProxy<>(Rarity.class, -1, "scguns:piglish", ChatFormatting.GOLD);
    public static final EnumProxy<Rarity> SCORCHED = new EnumProxy<>(Rarity.class, -1, "scguns:scorched", ChatFormatting.RED);
    public static final EnumProxy<Rarity> DEEP_DARK = new EnumProxy<>(Rarity.class, -1, "scguns:deep_dark", ChatFormatting.DARK_AQUA);
    public static final EnumProxy<Rarity> ENDISH = new EnumProxy<>(Rarity.class, -1, "scguns:endish", ChatFormatting.DARK_PURPLE);
    public static final EnumProxy<Rarity> BIZARRE = new EnumProxy<>(Rarity.class, -1, "scguns:bizarre", ChatFormatting.GRAY);

    private RarityEnumParams() {
    }
}
