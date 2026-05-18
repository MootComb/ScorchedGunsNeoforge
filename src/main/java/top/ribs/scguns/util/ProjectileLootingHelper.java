package top.ribs.scguns.util;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import top.ribs.scguns.entity.projectile.BeowulfProjectileEntity;
import top.ribs.scguns.entity.projectile.GibbsRoundProjectileEntity;

public final class ProjectileLootingHelper {
    private ProjectileLootingHelper() {
    }

    public static int applyProjectileLootingBonus(Holder<Enchantment> enchantment, LootContext context, int baseLevel) {
        if (!enchantment.is(Enchantments.LOOTING)) {
            return baseLevel;
        }

        Entity directEntity = context.getParamOrNull(LootContextParams.DIRECT_ATTACKING_ENTITY);
        if (directEntity instanceof BeowulfProjectileEntity) {
            return baseLevel + BeowulfProjectileEntity.BEOWULF_LOOTING_LEVEL;
        }
        if (directEntity instanceof GibbsRoundProjectileEntity) {
            return baseLevel + GibbsRoundProjectileEntity.GIBBS_ROUND_LOOTING_LEVEL;
        }
        return baseLevel;
    }
}
