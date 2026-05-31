package top.ribs.scguns.event;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.ribs.scguns.init.ModTags;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "scguns")
public class OceanWeaponEventHandler {
    private static final int OCEAN_WEAPON_GRACE_DURATION = 60;
    private static final Set<UUID> OCEAN_WEAPON_GRACE_PLAYERS = new HashSet<>();

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyDolphinGrace(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        applyDolphinGrace(event.getEntity());
    }

    private static void applyDolphinGrace(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        boolean holdingSpecialItem = isOceanWeapon(mainHandItem) || isOceanWeapon(offHandItem);
        boolean isInWater = player.isEyeInFluid(FluidTags.WATER);
        MobEffectInstance dolphinGraceEffect = player.getEffect(MobEffects.DOLPHINS_GRACE);

        if (holdingSpecialItem && isInWater) {
            if (dolphinGraceEffect == null || dolphinGraceEffect.getAmplifier() < 0 || dolphinGraceEffect.getDuration() <= 10) {
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, OCEAN_WEAPON_GRACE_DURATION, 0, false, false, true));
                OCEAN_WEAPON_GRACE_PLAYERS.add(player.getUUID());
            } else if (!isOceanWeaponGrace(dolphinGraceEffect)) {
                OCEAN_WEAPON_GRACE_PLAYERS.remove(player.getUUID());
            }
        } else {
            removeOceanWeaponGrace(player, dolphinGraceEffect);
        }
    }

    private static void removeOceanWeaponGrace(Player player, MobEffectInstance dolphinGraceEffect) {
        if (!OCEAN_WEAPON_GRACE_PLAYERS.remove(player.getUUID())) {
            return;
        }
        if (dolphinGraceEffect != null && isOceanWeaponGrace(dolphinGraceEffect)) {
            player.removeEffect(MobEffects.DOLPHINS_GRACE);
        }
    }

    private static boolean isOceanWeaponGrace(MobEffectInstance effect) {
        return effect.getAmplifier() == 0
                && effect.getDuration() <= OCEAN_WEAPON_GRACE_DURATION
                && !effect.isAmbient()
                && !effect.isVisible()
                && effect.showIcon();
    }

    private static boolean isOceanWeapon(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ModTags.Items.OCEAN_GUN);
    }
}
