package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ShockCoilConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ResourceLocation CONFIG_LOCATION = Reference.id("turrets/shock_coil.json");
    private static ShockCoilData config = ShockCoilData.defaults();

    private static void loadConfig(ResourceManager resourceManager) {
        config = ShockCoilData.defaults();
        try {
            Resource resource = resourceManager.getResource(CONFIG_LOCATION).orElse(null);
            if (resource == null) {
                ScorchedGuns.LOGGER.warn("No JSON found for shock coil config, using defaults");
                return;
            }

            try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null) {
                    parseConfig(json);
                }
                ScorchedGuns.LOGGER.info("Successfully loaded shock coil config");
            }
        } catch (Exception exception) {
            ScorchedGuns.LOGGER.error("Failed to load shock coil config at {}", CONFIG_LOCATION, exception);
            config = ShockCoilData.defaults();
        }
    }

    private static void parseConfig(JsonObject json) {
        if (json.has("maxEnergy")) {
            config.maxEnergy = json.get("maxEnergy").getAsInt();
        }
        if (json.has("energyPerZap")) {
            config.energyPerZap = json.get("energyPerZap").getAsInt();
        }
        if (json.has("zapRange")) {
            config.zapRange = json.get("zapRange").getAsDouble();
        }
        if (json.has("zapCooldown")) {
            config.zapCooldown = json.get("zapCooldown").getAsInt();
        }
        if (json.has("maxTargetsPerZap")) {
            config.maxTargetsPerZap = json.get("maxTargetsPerZap").getAsInt();
        }
        if (json.has("baseDamage")) {
            config.baseDamage = json.get("baseDamage").getAsFloat();
        }
        if (json.has("zapSound")) {
            config.zapSound = json.get("zapSound").getAsString();
        }
        if (json.has("statusEffects")) {
            config.statusEffects.clear();
            for (JsonElement element : json.getAsJsonArray("statusEffects")) {
                StatusEffect effect = parseStatusEffect(element.getAsJsonObject());
                if (effect != null) {
                    config.statusEffects.add(effect);
                }
            }
        }
    }

    @Nullable
    private static StatusEffect parseStatusEffect(JsonObject json) {
        try {
            ResourceLocation effectId = ResourceLocation.parse(json.get("effect").getAsString());
            ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, effectId);
            Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(key).orElse(null);
            if (effect == null) {
                ScorchedGuns.LOGGER.warn("Unknown shock coil effect: {}", effectId);
                return null;
            }
            int duration = json.has("duration") ? json.get("duration").getAsInt() : 100;
            int amplifier = json.has("amplifier") ? json.get("amplifier").getAsInt() : 0;
            float chance = json.has("chance") ? json.get("chance").getAsFloat() : 1.0F;
            return new StatusEffect(effect, duration, amplifier, chance);
        } catch (Exception exception) {
            ScorchedGuns.LOGGER.error("Error parsing shock coil status effect", exception);
            return null;
        }
    }

    public static int getMaxEnergy() {
        return config.maxEnergy;
    }

    public static int getEnergyPerZap() {
        return config.energyPerZap;
    }

    public static double getZapRange() {
        return config.zapRange;
    }

    public static int getZapCooldown() {
        return config.zapCooldown;
    }

    public static int getMaxTargetsPerZap() {
        return config.maxTargetsPerZap;
    }

    public static float getBaseDamage() {
        return config.baseDamage;
    }

    public static String getZapSound() {
        return config.zapSound;
    }

    public static List<StatusEffect> getStatusEffects() {
        return config.statusEffects;
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected @NotNull Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                ShockCoilConfig.loadConfig(resourceManager);
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                ScorchedGuns.LOGGER.info("Shock coil configuration loaded successfully");
            }
        });
    }

    private static class ShockCoilData {
        private int maxEnergy = 16000;
        private int energyPerZap = 100;
        private double zapRange = 8.0D;
        private int zapCooldown = 20;
        private int maxTargetsPerZap = 3;
        private float baseDamage = 4.0F;
        private String zapSound = "scguns:item.shock.fire";
        private List<StatusEffect> statusEffects = new ArrayList<>();

        private static ShockCoilData defaults() {
            ShockCoilData data = new ShockCoilData();
            data.statusEffects.add(new StatusEffect(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, 0.35F));
            return data;
        }
    }

    public record StatusEffect(Holder<MobEffect> effect, int duration, int amplifier, float chance) {
    }
}
