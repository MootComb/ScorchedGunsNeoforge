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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

public class FireGrenadeRoundEntity extends ProjectileEntity {
    private static final float EXPLOSION_RADIUS = 3.5F;

    public FireGrenadeRoundEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public FireGrenadeRoundEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && this.tickCount > 1) {
            this.level().addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            if (this.tickCount % 4 == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        explode();
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        explode();
    }

    @Override
    public void onExpired() {
        explode();
    }

    private void explode() {
        if (!this.level().isClientSide) {
            Level level = this.level();
            createFireExplosion(this, EXPLOSION_RADIUS, false);
            level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.5F, 0.9F + level.random.nextFloat() * 0.3F);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.blockPosition()).inflate(EXPLOSION_RADIUS))) {
                target.igniteForSeconds(5);
            }
            for (BlockPos pos : BlockPos.betweenClosed(this.blockPosition().offset(-2, -1, -2), this.blockPosition().offset(2, 1, 2))) {
                if (level.random.nextFloat() < 0.25F && level.isEmptyBlock(pos)) {
                    BlockState fire = BaseFireBlock.getState(level, pos);
                    if (fire.canSurvive(level, pos)) {
                        level.setBlock(pos, fire, 3);
                    }
                }
            }
        }
        this.discard();
    }
}
