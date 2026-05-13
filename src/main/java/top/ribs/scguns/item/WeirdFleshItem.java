package top.ribs.scguns.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.ribs.scguns.init.ModEffects;

import java.util.LinkedHashMap;
import java.util.Map;

public class WeirdFleshItem extends Item {
    private static final Map<Holder<MobEffect>, EffectData> EFFECTS = new LinkedHashMap<>();

    public WeirdFleshItem() {
        super(new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.1F)
                .alwaysEdible()
                .build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            applyRandomEffect(entity);
        }
        return result;
    }

    private static void applyRandomEffect(LivingEntity entity) {
        int totalWeight = EFFECTS.values().stream().mapToInt(EffectData::weight).sum();
        if (totalWeight <= 0) {
            return;
        }

        int roll = entity.getRandom().nextInt(totalWeight);
        int currentWeight = 0;
        for (Map.Entry<Holder<MobEffect>, EffectData> entry : EFFECTS.entrySet()) {
            currentWeight += entry.getValue().weight();
            if (roll < currentWeight) {
                EffectData data = entry.getValue();
                int duration = data.duration(entity);
                int amplifier = data.amplifier(entity);
                entity.addEffect(new MobEffectInstance(entry.getKey(), duration, amplifier));
                return;
            }
        }
    }

    static {
        EFFECTS.put(MobEffects.CONFUSION, new EffectData(20, 100, 200, 0, 1));
        EFFECTS.put(MobEffects.WEAKNESS, new EffectData(18, 100, 180, 0, 0));
        EFFECTS.put(MobEffects.POISON, new EffectData(15, 80, 160, 0, 1));
        EFFECTS.put(MobEffects.MOVEMENT_SLOWDOWN, new EffectData(15, 60, 140, 0, 1));
        EFFECTS.put(MobEffects.HUNGER, new EffectData(25, 100, 300, 0, 2));
        EFFECTS.put(MobEffects.DIG_SLOWDOWN, new EffectData(12, 60, 120, 0, 0));
        EFFECTS.put(MobEffects.BLINDNESS, new EffectData(10, 40, 100, 0, 1));
        EFFECTS.put(ModEffects.SULFUR_POISONING, new EffectData(12, 60, 140, 0, 1));
        EFFECTS.put(ModEffects.BLINDED, new EffectData(10, 40, 100, 0, 0));
        EFFECTS.put(ModEffects.DEAFENED, new EffectData(10, 40, 100, 0, 0));
        EFFECTS.put(MobEffects.WITHER, new EffectData(8, 40, 80, 0, 1));
        EFFECTS.put(MobEffects.LEVITATION, new EffectData(3, 80, 160, 0, 1));
        EFFECTS.put(MobEffects.INVISIBILITY, new EffectData(4, 100, 200, 0, 0));
        EFFECTS.put(MobEffects.GLOWING, new EffectData(4, 100, 200, 0, 0));
    }

    private record EffectData(int weight, int minDuration, int maxDuration, int minAmplifier, int maxAmplifier) {
        private int duration(LivingEntity entity) {
            return minDuration == maxDuration ? minDuration : minDuration + entity.getRandom().nextInt(maxDuration - minDuration + 1);
        }

        private int amplifier(LivingEntity entity) {
            return minAmplifier == maxAmplifier ? minAmplifier : minAmplifier + entity.getRandom().nextInt(maxAmplifier - minAmplifier + 1);
        }
    }
}
