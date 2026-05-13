package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.SulfurGasCloud;
import top.ribs.scguns.item.GunItem;

public class GasGrenadeRoundEntity extends ProjectileEntity {
    private static final float GAS_RADIUS = 6.0F;

    public GasGrenadeRoundEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public GasGrenadeRoundEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && this.tickCount > 1) {
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        releaseGas();
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        releaseGas();
    }

    @Override
    public void onExpired() {
        releaseGas();
    }

    private void releaseGas() {
        if (!this.level().isClientSide) {
            Vec3 center = this.position();
            this.level().playSound(null, this.blockPosition(), SoundEvents.CAT_HISS, SoundSource.BLOCKS, 2.0F, 1.0F);
            SulfurGasCloud.checkAndHandleFireExplosion(this.level(), center, GAS_RADIUS);
            SulfurGasCloud.spawnEnhancedGasCloud(this.level(), center, GAS_RADIUS, 0.8F, this.random);
            SulfurGasCloud.applyGasEffects(this.level(), center, GAS_RADIUS, 500, 2);
        }
        this.discard();
    }
}
