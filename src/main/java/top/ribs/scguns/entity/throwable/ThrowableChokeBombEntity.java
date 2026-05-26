package top.ribs.scguns.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModItems;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ThrowableChokeBombEntity extends ThrowableGrenadeEntity
{
    private static final int ACTIVATION_DELAY = 40;
    private static final int CLOUD_DURATION = 400;
    private static final int EFFECT_INTERVAL = 10;
    private static final int SUFFOCATION_INTERVAL = 20;
    private static final float SUFFOCATION_DAMAGE = 1.0F;
    private static final double PARTICLE_RENDER_DISTANCE = 256.0;
    private final float explosionRadius;
    private boolean cloudActive;
    private int cloudTicks;

    public ThrowableChokeBombEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
        this.explosionRadius = 4.0f;
    }

    public ThrowableChokeBombEntity(Level world, LivingEntity entity, int timeLeft, float radius)
    {
        super(ModEntities.THROWABLE_CHOKE_BOMB.get(), world, entity);
        this.setShouldBounce(true);
        this.setItem(new ItemStack(ModItems.CHOKE_BOMB.get()));
        this.setMaxLife(Math.max(ACTIVATION_DELAY + CLOUD_DURATION, timeLeft));
        this.explosionRadius = radius;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.isRemoved()) {
            return;
        }

        if (!this.cloudActive && this.tickCount >= ACTIVATION_DELAY) {
            this.activateCloud();
        }

        if (this.cloudActive) {
            this.cloudTicks++;
            this.emitChokeCloudParticles();
            if (this.cloudTicks % EFFECT_INTERVAL == 0) {
                this.applyChokeEffects();
                this.extinguishFireInArea();
            }
            if (this.cloudTicks >= CLOUD_DURATION) {
                this.remove(RemovalReason.KILLED);
            }
        }
    }

    @Override
    public void particleTick()
    {
        if (this.level().isClientSide)
        {
            this.level().addParticle(ParticleTypes.WHITE_ASH, true, this.getX(), this.getY() + 0.35, this.getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.SNOWFLAKE, true, this.getX(), this.getY() + 0.35, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void onDeath()
    {
        if (!this.cloudActive) {
            this.activateCloud();
        }
    }

    private void activateCloud() {
        this.cloudActive = true;
        this.cloudTicks = 0;
        this.setMaxLife(this.tickCount + CLOUD_DURATION);
        this.setDeltaMovement(Vec3.ZERO);
        double y = this.getY() + this.getType().getDimensions().height() * 0.5;
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 2.0F, 1.0F);
        this.extinguishFireInArea();
        this.applyChokeEffects();
        this.spawnExplosionParticles(new Vec3(this.getX(), y, this.getZ()));
    }

    private void emitChokeCloudParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = this.position().add(0.0, 0.35, 0.0);
        List<ServerPlayer> players = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                new AABB(center.subtract(PARTICLE_RENDER_DISTANCE, PARTICLE_RENDER_DISTANCE, PARTICLE_RENDER_DISTANCE),
                        center.add(PARTICLE_RENDER_DISTANCE, PARTICLE_RENDER_DISTANCE, PARTICLE_RENDER_DISTANCE)));
        int cloudParticles = Math.max(8, Math.round(this.explosionRadius * 3.0F));
        int ashParticles = Math.max(4, Math.round(this.explosionRadius * 1.5F));
        for (int i = 0; i < cloudParticles + ashParticles; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(this.random.nextDouble()) * this.explosionRadius;
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            double y = center.y + (this.random.nextDouble() - 0.5) * (this.explosionRadius * 0.45);
            double speed = 0.002 + this.random.nextDouble() * 0.006;
            double xSpeed = (this.random.nextDouble() - 0.5) * speed;
            double ySpeed = this.random.nextDouble() * speed * 0.5;
            double zSpeed = (this.random.nextDouble() - 0.5) * speed;
            var particle = i < cloudParticles ? ParticleTypes.CLOUD : ParticleTypes.WHITE_ASH;
            for (ServerPlayer player : players) {
                serverLevel.sendParticles(player, particle, true, x, y, z, 1, xSpeed, ySpeed, zSpeed, 0.1);
            }
        }
    }

    private void applyChokeEffects() {
        if (this.level().isClientSide) {
            return;
        }
        boolean shouldSuffocate = this.cloudTicks == 0 || this.cloudTicks % SUFFOCATION_INTERVAL == 0;
        Vec3 center = this.position();
        double radiusSquared = this.explosionRadius * this.explosionRadius;
        AABB area = new AABB(center.subtract(this.explosionRadius, this.explosionRadius, this.explosionRadius),
                center.add(this.explosionRadius, this.explosionRadius, this.explosionRadius));
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity.distanceToSqr(center) <= radiusSquared) {
                if (entity.isOnFire()) {
                    entity.clearFire();
                }
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true));
                if (shouldSuffocate) {
                    entity.hurt(entity.damageSources().inWall(), SUFFOCATION_DAMAGE);
                }
            }
        }
    }

    private void extinguishFireInArea() {
        BlockPos centerPos = this.blockPosition();
        int radius = (int) Math.ceil(this.explosionRadius);
        for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-radius, -1, -radius), centerPos.offset(radius, 1, radius))) {
            if (Vec3.atCenterOf(pos).distanceTo(this.position()) > this.explosionRadius) {
                continue;
            }
            BlockState state = this.level().getBlockState(pos);
            if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(ModBlocks.FAKE_SOUL_FIRE.get())) {
                this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            } else if ((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) && state.hasProperty(BlockStateProperties.LIT)) {
                this.level().setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
            }
        }
    }

    private void spawnExplosionParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;
                double speedX = (this.random.nextDouble() - 0.5) * 0.5;
                double speedY = (this.random.nextDouble() - 0.5) * 0.5;
                double speedZ = (this.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
                serverLevel.sendParticles(ParticleTypes.WHITE_ASH, position.x + offsetX, position.y + offsetY, position.z + offsetZ, 1, speedX, speedY, speedZ, 0.1);
            }
        }
    }
}
