package top.ribs.scguns.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.ribs.scguns.item.CogMaceItem;

public class CogMaceEventHandler {
    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)
                || !(attacker.getMainHandItem().getItem() instanceof CogMaceItem)) {
            return;
        }

        double movementSpeed = attacker.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double speedBonus = Math.max(0.0D, Math.min(movementSpeed - 0.1D, 0.3D)) * 30.0D;
        if (attacker.isSprinting()) {
            speedBonus *= 1.5D;
        }

        double fallBonus = Math.min(attacker.fallDistance, 5.0F);
        float totalBonus = (float) Math.min(speedBonus + fallBonus, 10.0D);
        if (totalBonus <= 0.1F) {
            return;
        }

        if (event.getEntity().isBlocking()) {
            totalBonus *= 0.5F;
        }

        event.setAmount(event.getAmount() + totalBonus);
    }
}
