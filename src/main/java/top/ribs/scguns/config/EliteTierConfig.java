package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.entity.player.PlayerGunProgression;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EliteTierConfig {
    private final Map<String, EliteData> tiers;

    public EliteTierConfig(Map<String, EliteData> tiers) {
        this.tiers = tiers != null ? tiers : Collections.emptyMap();
    }

    public EliteData get(PlayerGunProgression.GunTier tier) {
        return tiers.get(tier.name());
    }

    public static EliteTierConfig load() {
        try (InputStream inputStream = ScorchedGuns.class.getClassLoader().getResourceAsStream("data/scguns/entity/elite_tiers.json")) {
            if (inputStream == null) {
                ScorchedGuns.LOGGER.warn("Could not find elite tier config");
                return new EliteTierConfig(Collections.emptyMap());
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                Type type = new TypeToken<Map<String, EliteData>>() {}.getType();
                return new EliteTierConfig(new Gson().fromJson(reader, type));
            }
        } catch (IOException e) {
            ScorchedGuns.LOGGER.error("Failed to load elite tier config", e);
            return new EliteTierConfig(Collections.emptyMap());
        }
    }

    public static class EliteData {
        private List<String> weapons = Collections.emptyList();
        private List<GunnerMobConfig.ArmorPiece> armor = Collections.emptyList();

        public List<String> getWeapons() {
            return weapons != null ? weapons : Collections.emptyList();
        }

        public List<GunnerMobConfig.ArmorPiece> getArmor() {
            return armor != null ? armor : Collections.emptyList();
        }
    }
}
