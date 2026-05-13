package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RaidFlareConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final Map<String, FlareData> FLARES = new HashMap<>();

    public static void loadFlareConfigs(ResourceManager resourceManager) {
        FLARES.clear();
        for (Map.Entry<net.minecraft.resources.ResourceLocation, Resource> entry : resourceManager.listResources("flares", loc -> loc.getPath().endsWith(".json")).entrySet()) {
            String path = entry.getKey().getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String flareId = fileName.replace("_flare.json", "").replace(".json", "");

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                FlareData flareData = parseFlareData(json, flareId);
                if (flareData != null) {
                    FLARES.put(flareData.raidId(), flareData);
                    LOGGER.info("Loaded raid flare config: {} -> {}", flareId, flareData.raidId());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load raid flare config: {}", flareId, e);
            }
        }
        LOGGER.info("Loaded {} raid flare configurations", FLARES.size());
    }

    @Nullable
    private static FlareData parseFlareData(JsonObject json, String flareId) {
        try {
            String raidId = json.has("raid_id") ? json.get("raid_id").getAsString() : flareId;
            int burstDelay = json.has("burst_delay") ? json.get("burst_delay").getAsInt() : 40;
            int duration = json.has("duration") ? json.get("duration").getAsInt() : 200;
            List<ParticleEffect> trailParticles = parseParticleEffects(json, "trail_particles");
            List<ParticleEffect> burstParticles = parseParticleEffects(json, "burst_particles");
            FlarePattern pattern = json.has("pattern") ? parsePattern(json.getAsJsonObject("pattern")) : null;
            String burstSound = json.has("burst_sound") ? json.get("burst_sound").getAsString() : "minecraft:entity.firework_rocket.large_blast";
            float burstSoundVolume = json.has("burst_sound_volume") ? json.get("burst_sound_volume").getAsFloat() : 1.0F;
            float burstSoundPitch = json.has("burst_sound_pitch") ? json.get("burst_sound_pitch").getAsFloat() : 1.0F;
            return new FlareData(raidId, burstDelay, duration, trailParticles, burstParticles, pattern, burstSound, burstSoundVolume, burstSoundPitch);
        } catch (Exception e) {
            LOGGER.error("Error parsing raid flare data for {}", flareId, e);
            return null;
        }
    }

    private static List<ParticleEffect> parseParticleEffects(JsonObject json, String key) {
        List<ParticleEffect> effects = new ArrayList<>();
        if (!json.has(key)) {
            return effects;
        }
        for (JsonElement element : json.getAsJsonArray(key)) {
            JsonObject effectObj = element.getAsJsonObject();
            String particleType = effectObj.get("particle").getAsString();
            int count = effectObj.has("count") ? effectObj.get("count").getAsInt() : 10;
            double spread = effectObj.has("spread") ? effectObj.get("spread").getAsDouble() : 0.3D;
            double speed = effectObj.has("speed") ? effectObj.get("speed").getAsDouble() : 0.1D;
            int color = effectObj.has("color") ? Integer.parseInt(effectObj.get("color").getAsString().replace("#", ""), 16) : 0xFFFFFF;
            effects.add(new ParticleEffect(particleType, count, spread, speed, color));
        }
        return effects;
    }

    @Nullable
    private static FlarePattern parsePattern(JsonObject json) {
        String patternType = json.has("type") ? json.get("type").getAsString() : "circle";
        int repetitions = json.has("repetitions") ? json.get("repetitions").getAsInt() : 1;
        double scale = json.has("scale") ? json.get("scale").getAsDouble() : 1.0D;
        List<Vec3Data> points = new ArrayList<>();
        if (json.has("points")) {
            for (JsonElement element : json.getAsJsonArray("points")) {
                JsonObject pointObj = element.getAsJsonObject();
                points.add(new Vec3Data(pointObj.get("x").getAsDouble(), pointObj.get("y").getAsDouble(), pointObj.get("z").getAsDouble()));
            }
        }
        return new FlarePattern(patternType, points, repetitions, scale);
    }

    @Nullable
    public static FlareData getFlareData(String raidId) {
        return FLARES.get(raidId);
    }

    public static Set<String> getAllRaidIds() {
        return FLARES.keySet();
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
                loadFlareConfigs(resourceManager);
            }
        });
    }

    public record FlareData(String raidId, int burstDelay, int duration, List<ParticleEffect> trailParticles,
                            List<ParticleEffect> burstParticles, @Nullable FlarePattern pattern, String burstSound,
                            float burstSoundVolume, float burstSoundPitch) {
    }

    public record FlarePattern(String patternType, List<Vec3Data> points, int repetitions, double scale) {
    }

    public record ParticleEffect(String particleType, int count, double spread, double speed, int color) {
    }

    public record Vec3Data(double x, double y, double z) {
    }
}
