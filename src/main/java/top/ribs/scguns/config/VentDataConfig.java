package top.ribs.scguns.config;

import com.google.gson.Gson;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class VentDataConfig {
    private static final Gson GSON = new Gson();
    private static final ResourceLocation GEOTHERMAL_VENT = Reference.id("geothermal_vent");
    private static final ResourceLocation SULFUR_VENT = Reference.id("sulfur_vent");
    private static final Map<ResourceLocation, VentDefinition> VENTS = new HashMap<>();
    private static VentCollectorDefinition collector = VentCollectorDefinition.defaults();

    static {
        loadDefaults();
    }

    public static VentCollectorDefinition collector() {
        return collector;
    }

    public static int nextTickInterval(ResourceLocation ventId, Random random) {
        VentDefinition vent = VENTS.getOrDefault(ventId, defaultVent(ventId));
        int base = Math.max(1, vent.power.baseTickInterval);
        int wiggle = Math.max(0, vent.power.tickWiggleRoom);
        return base + (wiggle > 0 ? random.nextInt(wiggle) : 0);
    }

    public static ItemStack createOutput(ResourceLocation ventId, Random random) {
        VentDefinition vent = VENTS.getOrDefault(ventId, defaultVent(ventId));
        if (random.nextFloat() > vent.production.productionChance) {
            return ItemStack.EMPTY;
        }

        int totalWeight = 0;
        for (Output output : vent.production.outputs) {
            if (getItem(output.item()) != null) {
                totalWeight += Math.max(0, output.weight());
            }
        }
        if (totalWeight <= 0) {
            return ItemStack.EMPTY;
        }

        int roll = random.nextInt(totalWeight);
        for (Output output : vent.production.outputs) {
            Item item = getItem(output.item());
            if (item == null) {
                continue;
            }
            roll -= Math.max(0, output.weight());
            if (roll < 0) {
                return new ItemStack(item);
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isFilterItem(ItemStack stack) {
        return getFilterChargeAmount(stack) > 0;
    }

    public static int getFilterChargeAmount(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (FilterItem filterItem : collector.filters.filterItems) {
            if (filterItem.matches(stack)) {
                return Math.max(0, filterItem.chargeAmount);
            }
        }
        return 0;
    }

    private static void loadConfig(ResourceManager resourceManager) {
        loadDefaults();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("vents", location -> location.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation configId = toConfigId(entry.getKey());
            if (configId == null) {
                continue;
            }
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                if (configId.equals(Reference.id("vent_collector"))) {
                    VentCollectorDefinition loaded = GSON.fromJson(reader, VentCollectorDefinition.class);
                    if (loaded != null) {
                        collector = loaded.withDefaults();
                    }
                } else {
                    VentDefinition loaded = GSON.fromJson(reader, VentDefinition.class);
                    if (loaded != null) {
                        VENTS.put(configId, loaded.withDefaults(configId));
                    }
                }
            } catch (Exception exception) {
                ScorchedGuns.LOGGER.warn("Failed to load Scorched Guns vent config {}", entry.getKey(), exception);
            }
        }
    }

    private static ResourceLocation toConfigId(ResourceLocation location) {
        String path = location.getPath();
        if (!path.startsWith("vents/") || !path.endsWith(".json")) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), path.substring("vents/".length(), path.length() - ".json".length()));
    }

    private static void loadDefaults() {
        collector = VentCollectorDefinition.defaults();
        VENTS.clear();
        VENTS.put(GEOTHERMAL_VENT, defaultVent(GEOTHERMAL_VENT));
        VENTS.put(SULFUR_VENT, defaultVent(SULFUR_VENT));
    }

    private static VentDefinition defaultVent(ResourceLocation ventId) {
        if (ventId.equals(GEOTHERMAL_VENT)) {
            return new VentDefinition(
                    new Power(5, 80, 60),
                    new Production(List.of(new Output("scguns:niter_dust", 10)), 0.7F)
            );
        }
        return new VentDefinition(
                new Power(5, 100, 40),
                new Production(List.of(new Output("scguns:sulfur_dust", 19), new Output("minecraft:blaze_powder", 1)), 0.6F)
        );
    }

    private static Item getItem(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).orElse(null);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(@NotNull ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                loadConfig(resourceManager);
            }
        });
    }

    public static class VentCollectorDefinition {
        private Filters filters = Filters.defaults();
        private Processing processing = Processing.defaults();

        public static VentCollectorDefinition defaults() {
            return new VentCollectorDefinition();
        }

        private VentCollectorDefinition withDefaults() {
            if (filters == null) {
                filters = Filters.defaults();
            } else {
                filters = filters.withDefaults();
            }
            if (processing == null) {
                processing = Processing.defaults();
            }
            return this;
        }

        public int maxCharge() {
            return Math.max(1, filters.maxCharge);
        }

        public float consumptionChance() {
            return Math.max(0.0F, Math.min(1.0F, filters.consumptionChance));
        }

        public int processCooldown() {
            return Math.max(1, filters.processCooldown);
        }

        public float powerSpeedMultiplier() {
            return processing.powerSpeedMultiplier;
        }

        public int pushCooldown() {
            return Math.max(1, processing.pushCooldown);
        }
    }

    private static class Filters {
        private int maxCharge = 64;
        private float consumptionChance = 0.5F;
        private int processCooldown = 2;
        private List<FilterItem> filterItems = List.of(
                new FilterItem(null, "scguns:weak_filter", 4),
                new FilterItem("scguns:composite_filter", null, 8)
        );

        private static Filters defaults() {
            return new Filters();
        }

        private Filters withDefaults() {
            if (filterItems == null || filterItems.isEmpty()) {
                filterItems = defaults().filterItems;
            }
            return this;
        }
    }

    private static class Processing {
        private float powerSpeedMultiplier = 0.35F;
        private int pushCooldown = 5;

        private static Processing defaults() {
            return new Processing();
        }
    }

    private record FilterItem(String item, String tag, int chargeAmount) {
        private boolean matches(ItemStack stack) {
            if (item != null) {
                Item configuredItem = getItem(item);
                if (configuredItem != null && stack.is(configuredItem)) {
                    return true;
                }
            }
            if (tag != null) {
                try {
                    TagKey<Item> key = ItemTags.create(ResourceLocation.parse(tag));
                    return stack.is(key);
                } catch (IllegalArgumentException ignored) {
                }
            }
            return false;
        }
    }

    private static class VentDefinition {
        private Power power = new Power(5, 100, 40);
        private Production production = new Production(List.of(new Output("scguns:sulfur_dust", 19), new Output("minecraft:blaze_powder", 1)), 0.6F);

        private VentDefinition() {
        }

        private VentDefinition(Power power, Production production) {
            this.power = power;
            this.production = production;
        }

        private VentDefinition withDefaults(ResourceLocation ventId) {
            VentDefinition defaults = defaultVent(ventId);
            if (power == null) {
                power = defaults.power;
            }
            if (production == null || production.outputs == null || production.outputs.isEmpty()) {
                production = defaults.production;
            }
            return this;
        }
    }

    private record Power(int maxPower, int baseTickInterval, int tickWiggleRoom) {
    }

    private record Production(List<Output> outputs, float productionChance) {
    }

    private record Output(String item, int weight) {
    }
}
