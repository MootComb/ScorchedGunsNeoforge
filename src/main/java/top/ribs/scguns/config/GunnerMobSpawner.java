package top.ribs.scguns.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.ai.GunAttackGoal;
import top.ribs.scguns.entity.player.PlayerGunProgression;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;

import java.util.ArrayList;
import java.util.List;

public class GunnerMobSpawner {
    private static final String CHECKED_KEY = "scguns:GunnerChecked";
    private static final String GUNNER_KEY = "scguns:GunnerMob";

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof PathfinderMob mob)) {
            return;
        }
        if (mob.getPersistentData().getBoolean(CHECKED_KEY)) {
            return;
        }
        mob.getPersistentData().putBoolean(CHECKED_KEY, true);
        tryEquipGunner(mob);
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)) {
            return;
        }
        if (event.getSlot() == EquipmentSlot.MAINHAND && event.getTo().getItem() instanceof GunItem) {
            reassessWeaponGoal(mob);
        }
    }

    public static boolean hasGunAttackGoal(PathfinderMob mob) {
        for (WrappedGoal goal : mob.goalSelector.getAvailableGoals()) {
            if (goal.getGoal() instanceof GunAttackGoal<?>) {
                return true;
            }
        }
        return false;
    }

    public static void reassessWeaponGoal(PathfinderMob mob) {
        if (!hasGunAttackGoal(mob) && mob.getMainHandItem().getItem() instanceof GunItem) {
            int difficulty = mob.getPersistentData().getInt("scguns:GunnerAiDifficulty");
            mob.goalSelector.addGoal(1, new GunAttackGoal<>(mob, Math.max(1, difficulty)));
        }
    }

    public static void reloadConfigs() {
        ScorchedGuns.GUNNER_MOB_CONFIG = GunnerMobConfig.load();
        ScorchedGuns.TIERED_WEAPON_CONFIG = TieredWeaponConfig.load();
        ScorchedGuns.ELITE_TIER_CONFIG = EliteTierConfig.load();
        GunMobValues.init();
    }

    private static void tryEquipGunner(PathfinderMob mob) {
        if (!GunMobValues.enabled || mob.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        GunnerMobConfig.MobGunnerData thematic = ScorchedGuns.GUNNER_MOB_CONFIG.getMob(mobId.toString());
        Player nearestPlayer = level.getNearestPlayer(mob, 128.0D);

        if (thematic != null) {
            if (mob.getRandom().nextDouble() > thematic.getSpawnChance()) {
                return;
            }
            equipThematicGun(mob, thematic);
            return;
        }

        if (!mob.getType().is(ModTags.Entities.GUNNER)) {
            return;
        }
        if (nearestPlayer == null || mob.getRandom().nextDouble() > GunMobValues.getGunnerSpawnChance(level.getDifficulty())) {
            return;
        }
        equipProgressionGun(mob, nearestPlayer);
    }

    private static void equipThematicGun(PathfinderMob mob, GunnerMobConfig.MobGunnerData data) {
        ItemStack gun = randomStack(data.getWeapons(), mob);
        if (gun.isEmpty()) {
            return;
        }
        equipGun(mob, gun, data.getAiDifficulty(), data.getWeaponDropChance());
        equipArmor(mob, data.getArmor());
        maybeEquipElite(mob, getTier(gun), data.getAiDifficulty(), data.getWeaponDropChance());
    }

    private static void equipProgressionGun(PathfinderMob mob, Player player) {
        PlayerGunProgression progression = PlayerGunProgression.get(player);
        List<PlayerGunProgression.GunTier> availableTiers = getPopulatedTiers(progression.getAvailableMobTiers());
        if (availableTiers.isEmpty()) {
            return;
        }

        PlayerGunProgression.GunTier tier = availableTiers.get(mob.getRandom().nextInt(availableTiers.size()));
        ItemStack gun = randomStack(ScorchedGuns.TIERED_WEAPON_CONFIG.getWeapons(tier), mob);
        if (gun.isEmpty()) {
            return;
        }
        equipGun(mob, gun, 1, 0.08D);
        maybeEquipElite(mob, tier, 1, 0.08D);
    }

    private static List<PlayerGunProgression.GunTier> getPopulatedTiers(List<PlayerGunProgression.GunTier> availableTiers) {
        List<PlayerGunProgression.GunTier> populatedTiers = new ArrayList<>();
        for (PlayerGunProgression.GunTier tier : availableTiers) {
            if (hasRegisteredItem(ScorchedGuns.TIERED_WEAPON_CONFIG.getWeapons(tier))) {
                populatedTiers.add(tier);
            }
        }
        return populatedTiers;
    }

    private static boolean hasRegisteredItem(List<String> ids) {
        for (String id : ids) {
            try {
                if (BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).isPresent()) {
                    return true;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }

    private static void maybeEquipElite(PathfinderMob mob, PlayerGunProgression.GunTier tier, int baseAiDifficulty, double weaponDropChance) {
        if (!GunMobValues.elitesEnabled || tier == null || tier == PlayerGunProgression.GunTier.NONE) {
            return;
        }
        if (mob.getRandom().nextDouble() > GunMobValues.getEliteChance(mob.level().getDifficulty())) {
            return;
        }

        EliteTierConfig.EliteData eliteData = ScorchedGuns.ELITE_TIER_CONFIG.get(tier);
        if (eliteData == null) {
            return;
        }

        ItemStack eliteGun = randomStack(eliteData.getWeapons(), mob);
        if (!eliteGun.isEmpty()) {
            equipGun(mob, eliteGun, baseAiDifficulty + 1, weaponDropChance);
        }
        equipArmor(mob, eliteData.getArmor());
    }

    private static void equipGun(PathfinderMob mob, ItemStack gun, int aiDifficulty, double weaponDropChance) {
        markMobGun(gun);
        mob.setItemSlot(EquipmentSlot.MAINHAND, gun);
        mob.setDropChance(EquipmentSlot.MAINHAND, (float) Math.max(0.0D, Math.min(1.0D, weaponDropChance)));
        mob.getPersistentData().putBoolean(GUNNER_KEY, true);
        mob.getPersistentData().putInt("scguns:GunnerAiDifficulty", Math.max(1, aiDifficulty));
        reassessWeaponGoal(mob);
    }

    private static ItemStack randomStack(List<String> ids, PathfinderMob mob) {
        List<Item> items = new ArrayList<>();
        for (String id : ids) {
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).ifPresent(items::add);
        }
        if (items.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(items.get(mob.getRandom().nextInt(items.size())));
    }

    private static void equipArmor(PathfinderMob mob, List<GunnerMobConfig.ArmorPiece> armorPieces) {
        for (GunnerMobConfig.ArmorPiece armorPiece : armorPieces) {
            if (armorPiece.getItem() == null || armorPiece.getSlot() == null) {
                continue;
            }
            if (mob.getRandom().nextDouble() > armorPiece.getChance()) {
                continue;
            }
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(armorPiece.getItem())).ifPresent(item -> {
                EquipmentSlot slot = armorSlot(armorPiece.getSlot());
                if (slot != null) {
                    mob.setItemSlot(slot, new ItemStack(item));
                    mob.setDropChance(slot, 0.05F);
                }
            });
        }
    }

    private static EquipmentSlot armorSlot(String slot) {
        return switch (slot) {
            case "head" -> EquipmentSlot.HEAD;
            case "chest" -> EquipmentSlot.CHEST;
            case "legs" -> EquipmentSlot.LEGS;
            case "feet" -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    private static PlayerGunProgression.GunTier getTier(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ScorchedGuns.TIERED_WEAPON_CONFIG.getTierForItem(itemId.toString());
    }

    private static void markMobGun(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem gunItem)) {
            return;
        }
        Gun gun = gunItem.getModifiedGun(stack);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("IgnoreAmmo", true);
        tag.putInt("AmmoCount", Math.max(1, gun.getReloads().getMaxAmmo()));
        tag.putBoolean("scguns:MobGun", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
