package top.ribs.scguns.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.init.ModItems;

public class FinforcerEntity extends LateRaidGunnerEntity {
    public FinforcerEntity(EntityType<? extends FinforcerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return lateRaidGunnerAttributes(35.0D, 28.0D, 0.26D, 3.0D, 4.0D);
    }

    @NotNull
    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    @Nullable
    @Override
    protected Item getDefaultGun() {
        return ModItems.FLOUNDERGAT.get();
    }

    @Override
    protected int defaultAiDifficulty() {
        return 4;
    }
}
