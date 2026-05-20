package top.ribs.scguns.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.exosuit.ExoSuitGasMaskHandler;
import top.ribs.scguns.init.ModTags;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SporeCompatEvents {
    private static final String SPORE_MOD_ID = "spore";
    private static final ResourceKey<MobEffect> SPORE_MYCELIUM = ResourceKey.create(
            Registries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(SPORE_MOD_ID, "mycelium_ef"));

    private SporeCompatEvents() {
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!ModList.get().isLoaded(SPORE_MOD_ID)) {
            return;
        }

        if (!event.getEffectInstance().getEffect().is(SPORE_MYCELIUM)) {
            return;
        }

        if (hasSporeProtection(event.getEntity())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    private static boolean hasSporeProtection(LivingEntity entity) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty() && helmet.is(ModTags.Items.GAS_MASK)) {
            return true;
        }

        return entity instanceof Player player && ExoSuitGasMaskHandler.hasProtection(player);
    }
}
