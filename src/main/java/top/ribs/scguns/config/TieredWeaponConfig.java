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

public class TieredWeaponConfig {
    private final Map<String, List<String>> tierWeapons;

    public TieredWeaponConfig(Map<String, List<String>> tierWeapons) {
        this.tierWeapons = tierWeapons != null ? tierWeapons : Collections.emptyMap();
    }

    public List<String> getWeapons(PlayerGunProgression.GunTier tier) {
        return tierWeapons.getOrDefault(tier.name(), Collections.emptyList());
    }

    public PlayerGunProgression.GunTier getTierForItem(String itemId) {
        for (PlayerGunProgression.GunTier tier : PlayerGunProgression.GunTier.values()) {
            if (getWeapons(tier).contains(itemId)) {
                return tier;
            }
        }
        return null;
    }

    public static TieredWeaponConfig load() {
        try (InputStream inputStream = ScorchedGuns.class.getClassLoader().getResourceAsStream("data/scguns/entity/tier_weapons.json")) {
            if (inputStream == null) {
                ScorchedGuns.LOGGER.warn("Could not find tiered weapon config");
                return new TieredWeaponConfig(Collections.emptyMap());
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
                return new TieredWeaponConfig(new Gson().fromJson(reader, type));
            }
        } catch (IOException e) {
            ScorchedGuns.LOGGER.error("Failed to load tiered weapon config", e);
            return new TieredWeaponConfig(Collections.emptyMap());
        }
    }
}
