package top.ribs.scguns.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class CogMaceItem extends SwordItem {
    public CogMaceItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamage, attackSpeed)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        attacker.resetFallDistance();
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return super.hurtEnemy(stack, target, attacker);
    }
}
