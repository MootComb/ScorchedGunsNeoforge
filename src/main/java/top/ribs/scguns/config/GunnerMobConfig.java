package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import top.ribs.scguns.ScorchedGuns;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GunnerMobConfig {
    private Map<String, MobGunnerData> mobs = Collections.emptyMap();

    public Map<String, MobGunnerData> getMobs() {
        return mobs != null ? mobs : Collections.emptyMap();
    }

    public MobGunnerData getMob(String id) {
        return getMobs().get(id);
    }

    public static GunnerMobConfig load() {
        try (InputStream inputStream = ScorchedGuns.class.getClassLoader().getResourceAsStream("data/scguns/entity/gunner_mobs.json")) {
            if (inputStream == null) {
                ScorchedGuns.LOGGER.warn("Could not find gunner mob config");
                return new GunnerMobConfig();
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                GunnerMobConfig config = new Gson().fromJson(reader, GunnerMobConfig.class);
                return config != null ? config : new GunnerMobConfig();
            }
        } catch (IOException e) {
            ScorchedGuns.LOGGER.error("Failed to load gunner mob config", e);
            return new GunnerMobConfig();
        }
    }

    public static class MobGunnerData {
        @SerializedName("spawn_chance")
        private double spawnChance;
        @SerializedName("ai_difficulty")
        private int aiDifficulty = 1;
        @SerializedName("weapon_drop_chance")
        private double weaponDropChance = 0.08D;
        private List<String> weapons = Collections.emptyList();
        private List<ArmorPiece> armor = Collections.emptyList();

        public double getSpawnChance() {
            return spawnChance;
        }

        public int getAiDifficulty() {
            return aiDifficulty;
        }

        public double getWeaponDropChance() {
            return Math.max(0.0D, Math.min(1.0D, weaponDropChance));
        }

        public List<String> getWeapons() {
            return weapons != null ? weapons : Collections.emptyList();
        }

        public List<ArmorPiece> getArmor() {
            return armor != null ? armor : Collections.emptyList();
        }
    }

    public static class ArmorPiece {
        private String item;
        private String slot;
        private double chance;

        public String getItem() {
            return item;
        }

        public String getSlot() {
            return slot;
        }

        public double getChance() {
            return chance;
        }
    }
}
