package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RaidConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final Map<String, RaidData> RAIDS_BY_ID = new HashMap<>();
    private static final Map<Integer, List<RaidData>> RAIDS_BY_LEVEL = new HashMap<>();

    public static void loadRaidConfigs(ResourceManager resourceManager) {
        RAIDS_BY_ID.clear();
        RAIDS_BY_LEVEL.clear();

        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("raids", loc -> loc.getPath().endsWith(".json")).entrySet()) {
            String path = entry.getKey().getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String fileRaidId = fileName.replace("_raid.json", "").replace(".json", "");

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                RaidData raidData = parseRaidData(json);
                if (raidData != null) {
                    RAIDS_BY_ID.put(raidData.raidId(), raidData);
                    if (raidData.raidLevel() != null) {
                        RAIDS_BY_LEVEL.computeIfAbsent(raidData.raidLevel(), k -> new ArrayList<>()).add(raidData);
                    }
                    LOGGER.info("Loaded Scorched Guns raid: {}", raidData.raidId());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load Scorched Guns raid config: {}", fileRaidId, e);
            }
        }

        int progressionCount = RAIDS_BY_LEVEL.values().stream().mapToInt(List::size).sum();
        LOGGER.info("Loaded {} Scorched Guns raids ({} progression, {} custom)", RAIDS_BY_ID.size(), progressionCount, RAIDS_BY_ID.size() - progressionCount);
    }

    @Nullable
    public static RaidData getRaidById(String raidId) {
        return RAIDS_BY_ID.get(raidId);
    }

    @Nullable
    public static RaidData getRaidByRaidId(String raidId) {
        return getRaidById(raidId);
    }

    public static List<RaidData> getRaidsAtLevel(int level) {
        return RAIDS_BY_LEVEL.getOrDefault(level, Collections.emptyList());
    }

    public static List<RaidData> getRaidsForLevel(int playerLevel) {
        List<RaidData> availableRaids = new ArrayList<>();
        for (int level = 1; level <= playerLevel; level++) {
            availableRaids.addAll(getRaidsAtLevel(level));
        }
        return availableRaids;
    }

    public static Collection<RaidData> getAllRaids() {
        return RAIDS_BY_ID.values();
    }

    @Nullable
    private static RaidData parseRaidData(JsonObject json) {
        try {
            String raidId = json.get("raid_id").getAsString();
            Integer raidLevel = json.has("raid_level") ? json.get("raid_level").getAsInt() : null;
            BossData boss = parseBossData(json.getAsJsonObject("boss"), raidId);
            if (boss == null) {
                LOGGER.warn("Skipping raid {} because its boss could not be resolved.", raidId);
                return null;
            }
            HenchmenData henchmen = parseHenchmenData(json.getAsJsonObject("henchmen"));
            SpawnConditions conditions = parseSpawnConditions(json.getAsJsonObject("spawn_conditions"));
            return new RaidData(raidId, raidLevel, boss, henchmen, conditions);
        } catch (Exception e) {
            LOGGER.error("Error parsing Scorched Guns raid data", e);
            return null;
        }
    }

    @Nullable
    private static BossData parseBossData(JsonObject json, String raidId) {
        try {
            EntityType<?> entityType = resolveEntity(json.get("entity_type").getAsString(), "boss for raid " + raidId);
            if (entityType == null) {
                return null;
            }
            String customName = json.has("custom_name") ? json.get("custom_name").getAsString() : null;
            HealthConfig healthConfig = parseHealthConfig(json);
            WeaponEntry weapon = json.has("weapon") ? parseWeaponEntry(json.getAsJsonObject("weapon"), "boss weapon for raid " + raidId) : null;
            List<ArmorEntry> armor = parseArmorEntries(json, "armor", false);
            List<EffectEntry> effects = parseEffects(json);
            int aiDifficulty = json.has("ai_difficulty") ? json.get("ai_difficulty").getAsInt() : 3;
            ResourceLocation lootTable = json.has("special_loot_table") ? parseLocation(json.get("special_loot_table").getAsString(), null) : null;
            MountData mount = json.has("mount") ? parseMountData(json.getAsJsonObject("mount"), raidId) : null;
            return new BossData(entityType, customName, healthConfig, weapon, armor, effects, aiDifficulty, lootTable, mount);
        } catch (Exception e) {
            LOGGER.error("Error parsing boss data for raid {}", raidId, e);
            return null;
        }
    }

    @Nullable
    private static MountData parseMountData(JsonObject json, String raidId) {
        EntityType<?> mountType = resolveEntity(json.get("entity_type").getAsString(), "mount for raid " + raidId);
        if (mountType == null) {
            return null;
        }
        HealthConfig healthConfig = parseHealthConfig(json);
        List<ArmorEntry> armor = parseArmorEntries(json, "armor", false);
        List<EffectEntry> effects = parseEffects(json);
        boolean dropsLoot = !json.has("drops_loot") || json.get("drops_loot").getAsBoolean();
        return new MountData(mountType, healthConfig, armor, effects, dropsLoot);
    }

    private static HenchmenData parseHenchmenData(@Nullable JsonObject json) {
        if (json == null) {
            return new HenchmenData(List.of(), 0, 0, 200, 20, 0);
        }
        List<HenchmanType> types = new ArrayList<>();
        if (json.has("types")) {
            for (JsonElement element : json.getAsJsonArray("types")) {
                JsonObject typeObj = element.getAsJsonObject();
                EntityType<?> entityType = resolveEntity(typeObj.get("entity_type").getAsString(), "raid henchman");
                if (entityType == null) {
                    continue;
                }
                float weight = typeObj.has("weight") ? typeObj.get("weight").getAsFloat() : 1.0F;
                HealthConfig healthConfig = parseHealthConfig(typeObj);
                List<Item> weapons = parseWeapons(typeObj);
                List<ArmorEntry> armor = parseArmorEntries(typeObj, "armor", true);
                List<EffectEntry> effects = parseEffects(typeObj);
                int aiDifficulty = typeObj.has("ai_difficulty") ? typeObj.get("ai_difficulty").getAsInt() : 2;
                types.add(new HenchmanType(entityType, weight, healthConfig, weapons, armor, effects, aiDifficulty));
            }
        }

        int maxAlive = json.has("max_concurrent") ? json.get("max_concurrent").getAsInt() : (json.has("max_alive") ? json.get("max_alive").getAsInt() : 4);
        int maxTotal = json.has("max_total") ? json.get("max_total").getAsInt() : 15;
        int spawnInterval = json.has("spawn_interval_ticks") ? json.get("spawn_interval_ticks").getAsInt() : 200;
        int spawnRadius = json.has("spawn_radius") ? json.get("spawn_radius").getAsInt() : 20;
        int spawnAttempts = json.has("spawn_attempts_per_wave") ? json.get("spawn_attempts_per_wave").getAsInt() : 3;
        return new HenchmenData(types, maxAlive, maxTotal, spawnInterval, spawnRadius, spawnAttempts);
    }

    private static List<Item> parseWeapons(JsonObject typeObj) {
        List<Item> weapons = new ArrayList<>();
        if (!typeObj.has("weapons")) {
            return weapons;
        }
        for (JsonElement weaponElement : typeObj.getAsJsonArray("weapons")) {
            Item weapon = resolveItem(weaponElement.getAsString(), "raid henchman weapon");
            if (weapon != null) {
                weapons.add(weapon);
            }
        }
        return weapons;
    }

    private static List<ArmorEntry> parseArmorEntries(JsonObject json, String key, boolean chanceField) {
        List<ArmorEntry> armor = new ArrayList<>();
        if (!json.has(key)) {
            return armor;
        }
        for (JsonElement element : json.getAsJsonArray(key)) {
            JsonObject armorObj = element.getAsJsonObject();
            Item armorItem = resolveItem(armorObj.get("item").getAsString(), "raid armor");
            if (armorItem == null) {
                continue;
            }
            String slot = armorObj.get("slot").getAsString();
            float dropChance = armorObj.has(chanceField ? "chance" : "drop_chance") ? armorObj.get(chanceField ? "chance" : "drop_chance").getAsFloat() : 0.085F;
            CompoundTag nbt = armorObj.has("nbt") ? parseNBT(armorObj.getAsJsonObject("nbt")) : null;
            armor.add(new ArmorEntry(armorItem, slot, dropChance, nbt));
        }
        return armor;
    }

    @Nullable
    private static WeaponEntry parseWeaponEntry(JsonObject weaponObj, String context) {
        Item weaponItem = resolveItem(weaponObj.get("item").getAsString(), context);
        if (weaponItem == null) {
            return null;
        }
        float dropChance = weaponObj.has("drop_chance") ? weaponObj.get("drop_chance").getAsFloat() : 0.085F;
        CompoundTag nbt = weaponObj.has("nbt") ? parseNBT(weaponObj.getAsJsonObject("nbt")) : null;
        return new WeaponEntry(weaponItem, dropChance, nbt);
    }

    @Nullable
    private static CompoundTag parseNBT(JsonObject json) {
        try {
            CompoundTag tag = new CompoundTag();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive()) {
                    putPrimitive(tag, key, value.getAsJsonPrimitive());
                } else if (value.isJsonObject()) {
                    tag.put(key, Objects.requireNonNull(parseNBT(value.getAsJsonObject())));
                } else if (value.isJsonArray()) {
                    tag.put(key, parseArray(key, value.getAsJsonArray()));
                }
            }
            return tag;
        } catch (Exception e) {
            LOGGER.error("Error parsing raid NBT data", e);
            return null;
        }
    }

    private static void putPrimitive(CompoundTag tag, String key, JsonPrimitive primitive) {
        if (primitive.isNumber()) {
            if (primitive.getAsString().contains(".")) {
                tag.putFloat(key, primitive.getAsFloat());
            } else {
                tag.putInt(key, primitive.getAsInt());
            }
        } else if (primitive.isString()) {
            tag.putString(key, primitive.getAsString());
        } else if (primitive.isBoolean()) {
            tag.putBoolean(key, primitive.getAsBoolean());
        }
    }

    private static ListTag parseArray(String key, JsonArray array) {
        ListTag list = new ListTag();
        if ("Enchantments".equals(key)) {
            for (JsonElement elem : array) {
                if (elem.isJsonObject()) {
                    JsonObject enchObj = elem.getAsJsonObject();
                    CompoundTag enchTag = new CompoundTag();
                    enchTag.putString("id", enchObj.get("id").getAsString());
                    enchTag.putInt("lvl", enchObj.get("lvl").getAsInt());
                    list.add(enchTag);
                }
            }
            return list;
        }
        for (JsonElement elem : array) {
            if (elem.isJsonObject()) {
                list.add(Objects.requireNonNull(parseNBT(elem.getAsJsonObject())));
            } else if (elem.isJsonPrimitive()) {
                JsonPrimitive prim = elem.getAsJsonPrimitive();
                if (prim.isString()) {
                    list.add(StringTag.valueOf(prim.getAsString()));
                }
            }
        }
        return list;
    }

    private static HealthConfig parseHealthConfig(JsonObject json) {
        Float fixedHealth = json.has("fixed_health") ? json.get("fixed_health").getAsFloat() : null;
        Float healthMultiplier = json.has("health_multiplier") ? json.get("health_multiplier").getAsFloat() : null;
        return new HealthConfig(fixedHealth, healthMultiplier);
    }

    private static List<EffectEntry> parseEffects(JsonObject json) {
        List<EffectEntry> effects = new ArrayList<>();
        if (!json.has("effects")) {
            return effects;
        }
        for (JsonElement element : json.getAsJsonArray("effects")) {
            JsonObject effectObj = element.getAsJsonObject();
            ResourceLocation effectId = parseLocation(effectObj.get("effect").getAsString(), null);
            if (effectId == null) {
                continue;
            }
            BuiltInRegistries.MOB_EFFECT.getHolder(ResourceKey.create(Registries.MOB_EFFECT, effectId)).ifPresent(effect -> {
                int amplifier = effectObj.has("amplifier") ? effectObj.get("amplifier").getAsInt() : 0;
                int duration = effectObj.has("duration") ? effectObj.get("duration").getAsInt() : -1;
                boolean ambient = effectObj.has("ambient") && effectObj.get("ambient").getAsBoolean();
                boolean visible = !effectObj.has("visible") || effectObj.get("visible").getAsBoolean();
                effects.add(new EffectEntry(effect, amplifier, duration, ambient, visible));
            });
        }
        return effects;
    }

    private static SpawnConditions parseSpawnConditions(@Nullable JsonObject json) {
        int minPlayers = json != null && json.has("min_players_nearby") ? json.get("min_players_nearby").getAsInt() : 1;
        int searchRadius = json != null && json.has("search_radius") ? json.get("search_radius").getAsInt() : 64;
        List<ResourceLocation> validDimensions = new ArrayList<>();
        if (json != null && json.has("valid_dimensions")) {
            for (JsonElement element : json.getAsJsonArray("valid_dimensions")) {
                ResourceLocation id = parseLocation(element.getAsString(), null);
                if (id != null) {
                    validDimensions.add(id);
                }
            }
        }
        if (validDimensions.isEmpty()) {
            validDimensions.add(ResourceLocation.withDefaultNamespace("overworld"));
        }
        String announcement = json != null && json.has("announcement_message") ? json.get("announcement_message").getAsString() : "translation:raid.scguns.warning";
        return new SpawnConditions(minPlayers, searchRadius, validDimensions, announcement);
    }

    @Nullable
    private static EntityType<?> resolveEntity(String id, String context) {
        ResourceLocation location = parseLocation(id, context);
        if (location == null) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(location).orElseGet(() -> {
            LOGGER.warn("Skipping missing entity type {} in {}", location, context);
            return null;
        });
    }

    @Nullable
    private static Item resolveItem(String id, String context) {
        ResourceLocation location = parseLocation(id, context);
        if (location == null) {
            return null;
        }
        return BuiltInRegistries.ITEM.getOptional(location).orElseGet(() -> {
            LOGGER.warn("Skipping missing item {} in {}", location, context);
            return null;
        });
    }

    @Nullable
    private static ResourceLocation parseLocation(String id, @Nullable String context) {
        try {
            return ResourceLocation.parse(id);
        } catch (Exception e) {
            LOGGER.warn("Invalid resource id{}: {}", context != null ? " in " + context : "", id);
            return null;
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                loadRaidConfigs(resourceManager);
            }
        });
    }

    public record ArmorEntry(Item item, String slot, float dropChance, @Nullable CompoundTag nbt) {
    }

    public record BossData(EntityType<?> entityType, @Nullable String customName, HealthConfig healthConfig,
                           @Nullable WeaponEntry weapon, List<ArmorEntry> armor, List<EffectEntry> effects,
                           int aiDifficulty, @Nullable ResourceLocation specialLootTable, @Nullable MountData mount) {
    }

    public record EffectEntry(Holder<MobEffect> effect, int amplifier, int duration, boolean ambient, boolean visible) {
    }

    public record HealthConfig(@Nullable Float fixedHealth, @Nullable Float healthMultiplier) {
        public boolean useMultiplier() {
            return this.healthMultiplier != null;
        }
    }

    public record HenchmanType(EntityType<?> entityType, float weight, HealthConfig healthConfig, List<Item> weapons,
                               List<ArmorEntry> armor, List<EffectEntry> effects, int aiDifficulty) {
    }

    public record HenchmenData(List<HenchmanType> types, int maxAlive, int maxTotal, int spawnIntervalTicks,
                               int spawnRadius, int spawnAttemptsPerWave) {
        @Nullable
        public HenchmanType selectRandomType(RandomSource random) {
            if (this.types.isEmpty()) {
                return null;
            }
            float totalWeight = 0.0F;
            for (HenchmanType type : this.types) {
                totalWeight += type.weight();
            }
            float roll = random.nextFloat() * totalWeight;
            float currentWeight = 0.0F;
            for (HenchmanType type : this.types) {
                currentWeight += type.weight();
                if (roll < currentWeight) {
                    return type;
                }
            }
            return this.types.get(this.types.size() - 1);
        }
    }

    public record MountData(EntityType<?> entityType, HealthConfig healthConfig, List<ArmorEntry> armor,
                            List<EffectEntry> effects, boolean mountDropsLoot) {
    }

    public record RaidData(String raidId, @Nullable Integer raidLevel, BossData boss, HenchmenData henchmen,
                           SpawnConditions spawnConditions) {
    }

    public record SpawnConditions(int minPlayersNearby, int searchRadius, List<ResourceLocation> validDimensions,
                                  String announcementMessage) {
    }

    public record WeaponEntry(Item item, float dropChance, @Nullable CompoundTag nbt) {
    }
}
