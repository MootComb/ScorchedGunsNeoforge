package top.ribs.scguns.entity.player;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;

import java.util.ArrayList;
import java.util.List;

public class PlayerGunProgression {
    private static final String ROOT_KEY = "scguns:GunProgression";
    private static final String TIER_KEY = "Tier";

    private GunTier currentTier = GunTier.NONE;

    public GunTier getCurrentTier() {
        return currentTier;
    }

    public GunTier getMaxMobTier() {
        int ordinal = currentTier.ordinal() - 1;
        if (ordinal <= GunTier.NONE.ordinal()) {
            return GunTier.NONE;
        }
        return GunTier.values()[ordinal];
    }

    public List<GunTier> getAvailableMobTiers() {
        List<GunTier> tiers = new ArrayList<>();
        GunTier max = getMaxMobTier();
        for (GunTier tier : GunTier.values()) {
            if (tier != GunTier.NONE && tier.ordinal() <= max.ordinal()) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    public boolean canMobSpawnWithTier(GunTier tier) {
        return tier != GunTier.NONE && tier.ordinal() <= getMaxMobTier().ordinal();
    }

    public boolean updateTier(GunTier tier) {
        if (tier != null && tier.ordinal() > currentTier.ordinal()) {
            currentTier = tier;
            return true;
        }
        return false;
    }

    public void setTier(GunTier tier) {
        currentTier = tier != null ? tier : GunTier.NONE;
    }

    public boolean checkAndUpdateFromItem(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem)) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        GunTier tier = getTierFromItemTags(stack);
        if (tier == null) {
            tier = ScorchedGuns.TIERED_WEAPON_CONFIG.getTierForItem(itemId.toString());
        }
        return updateTier(tier);
    }

    private static GunTier getTierFromItemTags(ItemStack stack) {
        if (stack.is(ModTags.Items.ANTIQUE_GUN_TIER)) return GunTier.ANTIQUE;
        if (stack.is(ModTags.Items.FRONTIER_GUN_TIER)) return GunTier.FRONTIER;
        if (stack.is(ModTags.Items.COPPER_GUN_TIER)) return GunTier.COPPER;
        if (stack.is(ModTags.Items.IRON_GUN_TIER)) return GunTier.IRON;
        if (stack.is(ModTags.Items.WRECKER_GUN_TIER)) return GunTier.WRECKER;
        if (stack.is(ModTags.Items.OCEAN_GUN_TIER)) return GunTier.OCEAN;
        if (stack.is(ModTags.Items.DIAMOND_STEEL_GUN_TIER)) return GunTier.DIAMOND_STEEL;
        if (stack.is(ModTags.Items.TREATED_BRASS_GUN_TIER)) return GunTier.TREATED_BRASS;
        if (stack.is(ModTags.Items.PIGLIN_GUN_TIER)) return GunTier.PIGLIN;
        if (stack.is(ModTags.Items.DEEP_DARK_GUN_TIER)) return GunTier.DEEP_DARK;
        if (stack.is(ModTags.Items.END_GUN_TIER)) return GunTier.END;
        if (stack.is(ModTags.Items.SCORCHED_GUN_TIER)) return GunTier.SCORCHED;
        return null;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TIER_KEY, currentTier.name());
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.contains(TIER_KEY)) {
            try {
                currentTier = GunTier.valueOf(tag.getString(TIER_KEY));
            } catch (IllegalArgumentException ignored) {
                currentTier = GunTier.NONE;
            }
        }
    }

    public static PlayerGunProgression get(Player player) {
        PlayerGunProgression progression = new PlayerGunProgression();
        CompoundTag data = player.getPersistentData();
        if (data.contains(ROOT_KEY)) {
            progression.loadNBT(data.getCompound(ROOT_KEY));
        }
        return progression;
    }

    public static void save(Player player, PlayerGunProgression progression) {
        player.getPersistentData().put(ROOT_KEY, progression.saveNBT());
    }

    public static boolean updateAndSave(Player player, ItemStack stack) {
        PlayerGunProgression progression = get(player);
        boolean updated = progression.checkAndUpdateFromItem(stack);
        if (updated) {
            save(player, progression);
        }
        return updated;
    }

    public enum GunTier {
        NONE,
        ANTIQUE,
        FRONTIER,
        COPPER,
        IRON,
        WRECKER,
        OCEAN,
        DIAMOND_STEEL,
        TREATED_BRASS,
        PIGLIN,
        DEEP_DARK,
        END,
        SCORCHED
    }
}
