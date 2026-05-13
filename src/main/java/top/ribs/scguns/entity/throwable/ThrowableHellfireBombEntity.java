package top.ribs.scguns.entity.throwable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.world.ProjectileExplosion;

public class ThrowableHellfireBombEntity extends ThrowableGrenadeEntity {
    public float rotation;

    public ThrowableHellfireBombEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ThrowableHellfireBombEntity(Level world, LivingEntity entity, int timeLeft) {
        super(ModEntities.THROWABLE_HELLFIRE_BOMB.get(), world, entity);
        this.setShouldBounce(false);
        this.setItem(new ItemStack(ModItems.HELLFIRE_BOMB.get()));
        this.setMaxLife(60);
    }

    @Override
    public void particleTick() {
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY() + 0.25, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void onDeath() {
        double y = this.getY() + this.getType().getDimensions().height() * 0.5;
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 1.0F);
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 2.0F, 1.0F);
        createSoulFireExplosion(this, 4.0F);
    }

    private static void createSoulFireExplosion(Entity entity, float radius) {
        Level world = entity.level();
        if (world.isClientSide()) {
            return;
        }

        DamageSource source = entity.damageSources().explosion(entity, entity instanceof ThrowableItemEntity throwable ? throwable.getOwner() : null);
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius * 0.5F, false, Explosion.BlockInteraction.KEEP) {
            @Override
            protected float getEntityDamageAmount(Entity entity, double distance) {
                return 0.0F;
            }
        };

        if (EventHooks.onExplosionStart(world, explosion)) {
            return;
        }

        explosion.explode();
        explosion.finalizeExplosion(true);

        BlockPos centerPos = entity.blockPosition();
        AABB effectArea = new AABB(centerPos).inflate(radius);
        for (LivingEntity livingEntity : world.getEntitiesOfClass(LivingEntity.class, effectArea)) {
            double distance = livingEntity.distanceTo(entity);
            if (distance <= radius) {
                livingEntity.igniteForSeconds(8);
                float damage = (float) (4.0 * (1.0 - distance / radius));
                livingEntity.hurt(world.damageSources().onFire(), damage);
            }
        }

        int radiusInt = (int) Math.ceil(radius);
        int radiusSquared = radiusInt * radiusInt;
        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int z = -radiusInt; z <= radiusInt; z++) {
                BlockPos columnPos = centerPos.offset(x, 0, z);
                if (centerPos.distSqr(columnPos) <= radiusSquared) {
                    for (int y = -radiusInt; y <= radiusInt; y++) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        BlockState stateAtPos = world.getBlockState(pos);
                        BlockState stateBelow = world.getBlockState(pos.below());
                        if (stateAtPos.isAir() && (stateBelow.isFaceSturdy(world, pos.below(), Direction.UP) || !stateBelow.isAir())) {
                            world.setBlock(pos, ModBlocks.FAKE_SOUL_FIRE.get().defaultBlockState(), 3);
                            break;
                        }
                    }
                }
            }
        }

        if (world instanceof ServerLevel serverLevel) {
            Vec3 center = entity.position();
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 0.25, center.z, 24, 0.8, 0.4, 0.8, 0.05);
            serverLevel.sendParticles(ParticleTypes.SOUL, center.x, center.y + 0.25, center.z, 16, 0.8, 0.4, 0.8, 0.04);
        }
    }
}
