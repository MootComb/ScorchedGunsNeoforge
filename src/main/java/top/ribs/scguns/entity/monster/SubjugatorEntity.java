package top.ribs.scguns.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModItems;

public class SubjugatorEntity extends LateRaidGunnerEntity {
    public SubjugatorEntity(EntityType<? extends SubjugatorEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return lateRaidGunnerAttributes(45.0D, 32.0D, 0.3D, 5.0D, 5.0D);
    }

    @Nullable
    @Override
    protected Item getDefaultGun() {
        return ModItems.VALORA.get();
    }

    @Override
    protected int defaultAiDifficulty() {
        return 4;
    }
}
