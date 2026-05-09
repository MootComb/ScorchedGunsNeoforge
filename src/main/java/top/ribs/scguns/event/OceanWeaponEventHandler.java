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
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModTags;

@EventBusSubscriber(modid = "scguns")
public class OceanWeaponEventHandler {

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
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, false, false, true));
            }
        } else {
            if (dolphinGraceEffect != null) {
                player.removeEffect(MobEffects.DOLPHINS_GRACE);
            }
        }
    }

    private static boolean isOceanWeapon(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ModTags.Items.OCEAN_GUN);
    }
}
