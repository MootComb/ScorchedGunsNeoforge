package top.ribs.scguns.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModItems;

public class AdjudicatorEntity extends LateRaidGunnerEntity {
    public AdjudicatorEntity(EntityType<? extends AdjudicatorEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return lateRaidGunnerAttributes(40.0D, 32.0D, 0.28D, 4.0D, 4.0D);
    }

    @Nullable
    @Override
    protected Item getDefaultGun() {
        return ModItems.KRAUSER.get();
    }

    @Override
    protected int defaultAiDifficulty() {
        return 4;
    }
}
