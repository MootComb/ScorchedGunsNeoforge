package top.ribs.scguns.enchantment;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModTags;

import java.util.Random;

/**
 * Corroded Enchantment - Deals extra damage to bots and has a chance to poison non-bot enemies
 */
@EventBusSubscriber(modid = Reference.MOD_ID)
public class CorrodedEnchantment extends GunEnchantment {

    private static final Random RANDOM = new Random();

    public CorrodedEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentTypes.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, Type.WEAPON);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    public void doPostAttack(LivingEntity attacker, Entity target, int level) {
        if (target instanceof LivingEntity livingTarget) {
            if (isBotEntity(livingTarget)) {
                spawnCorrodedParticles(livingTarget, level);
                return;
            } else {
                if (RANDOM.nextFloat() < 0.30F) {
                    int poisonDuration = 60 + (level * 20);
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 0));
                }
            }
        }
    }

    private void spawnCorrodedParticles(LivingEntity entity, int level) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < level * 5; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * entity.getBbWidth();
                double offsetY = RANDOM.nextDouble() * entity.getBbHeight();
                double offsetZ = (RANDOM.nextDouble() - 0.5) * entity.getBbWidth();
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,
                        1,
                        0, 0, 0,
                        0.1);
            }
        }
    }
    private static boolean isBotEntity(LivingEntity entity) {
        return entity.getType().is(ModTags.Entities.BOT);
    }

    public static float getBotDamageBonus(int level) {
        return level * 2.0F;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
            ItemStack weapon = attacker.getMainHandItem();

            if (!weapon.isEmpty()) {
                int corrodedLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CORRODED, weapon);

                if (corrodedLevel > 0 && event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    if (isBotEntity(target)) {
                        // Apply bonus damage to bots
                        float bonusDamage = getBotDamageBonus(corrodedLevel);
                        event.setAmount(event.getAmount() + bonusDamage);
                        spawnCorrodedParticlesStatic(target, corrodedLevel);
                    } else if (RANDOM.nextFloat() < 0.30F) {
                        int poisonDuration = 60 + (corrodedLevel * 20);
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 0));
                    }
                }
            }
        }
    }

    private static void spawnCorrodedParticlesStatic(LivingEntity entity, int level) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < level * 5; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * entity.getBbWidth();
                double offsetY = RANDOM.nextDouble() * entity.getBbHeight();
                double offsetZ = (RANDOM.nextDouble() - 0.5) * entity.getBbWidth();
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        entity.getX() + offsetX,
                        entity.getY() + offsetY,
                        entity.getZ() + offsetZ,
                        1,
                        0, 0, 0,
                        0.1);
            }
        }
    }
}
