package top.ribs.scguns.entity.monster;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.common.SulfurGasCloud;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModSounds;

public class SulfurheadEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(SulfurheadEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT = SynchedEntityData.defineId(SulfurheadEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PRIMED = SynchedEntityData.defineId(SulfurheadEntity.class, EntityDataSerializers.BOOLEAN);
    private static final float LOW_HEALTH_THRESHOLD = 0.3F;
    private static final float GAS_CLOUD_RADIUS = 6.0F;
    private static final int GAS_CLOUD_DURATION = 200;
    private static final int MAX_SWELL = 43;
    private int oldSwell;
    private int swell;
    private boolean hasTriggeredGasCloud;

    public SulfurheadEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(ATTACK_TIMEOUT, 0);
        builder.define(PRIMED, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance potionEffect) {
        Holder<MobEffect> effect = potionEffect.getEffect();
        if (effect == MobEffects.POISON ||
                effect == MobEffects.WITHER ||
                effect == MobEffects.HUNGER ||
                effect == MobEffects.REGENERATION ||
                effect == MobEffects.SATURATION ||
                effect == MobEffects.CONFUSION ||
                effect == MobEffects.BLINDNESS ||
                effect == MobEffects.WEAKNESS ||
                effect == MobEffects.MOVEMENT_SLOWDOWN ||
                effect == MobEffects.DIG_SLOWDOWN ||
                effect == MobEffects.HARM ||
                effect == MobEffects.HEAL ||
                potionEffect.is(ModEffects.SULFUR_POISONING)) {
            return false;
        }
        return super.canBeAffected(potionEffect);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float amount = this.getPose() == Pose.STANDING ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(amount, 0.2F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);
                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }

            if (this.isAlive() && this.isLowHealth() && !this.hasTriggeredGasCloud && !this.isPrimed()) {
                this.setPrimed(true);
                this.level().broadcastEntityEvent(this, (byte) 5);
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.8F);
            }

            if (this.isPrimed() && this.swell >= MAX_SWELL) {
                this.swell = MAX_SWELL;
                this.spawnGasCloudAndDie();
            }
        }

        if (this.isAlive()) {
            this.oldSwell = this.swell;
            if (this.isPrimed()) {
                this.swell++;
            } else if (this.swell > 0) {
                this.swell--;
            }

            if (this.swell < 0) {
                this.swell = 0;
            }
        }
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(12);
        }
    }

    public void setAttackTimeout(int timeout) {
        this.entityData.set(ATTACK_TIMEOUT, timeout);
    }

    public int getAttackTimeout() {
        return this.entityData.get(ATTACK_TIMEOUT);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setPrimed(boolean primed) {
        this.entityData.set(PRIMED, primed);
    }

    public boolean isPrimed() {
        return this.entityData.get(PRIMED);
    }

    public boolean isLowHealth() {
        return this.getHealth() / this.getMaxHealth() <= LOW_HEALTH_THRESHOLD;
    }

    public float getSwelling(float partialTicks) {
        return Mth.lerp(partialTicks, this.oldSwell, this.swell) / (MAX_SWELL - 2.0F);
    }

    private void spawnGasCloudAndDie() {
        if (!this.level().isClientSide && !this.hasTriggeredGasCloud) {
            this.hasTriggeredGasCloud = true;
            Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
            SulfurGasCloud.checkAndHandleFireExplosion(this.level(), center, GAS_CLOUD_RADIUS);
            SulfurGasCloud.spawnEnhancedGasCloud(this.level(), center, GAS_CLOUD_RADIUS, 1.0F, this.random);
            SulfurGasCloud.applyGasEffects(this.level(), center, GAS_CLOUD_RADIUS, GAS_CLOUD_DURATION, 2);
            this.spawnDeathParticleBurst(center);
            this.playSound(SoundEvents.SLIME_BLOCK_BREAK, 1.5F, 0.8F);
            this.discard();
        }
    }

    private void spawnDeathParticleBurst(Vec3 center) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        int smokeParticles = 40;
        int dustParticles = 30;

        for (int i = 0; i < smokeParticles; i++) {
            double angle = this.random.nextDouble() * 2.0D * Math.PI;
            double verticalAngle = (this.random.nextDouble() - 0.5D) * Math.PI;
            double speed = 0.15D + this.random.nextDouble() * 0.25D;
            double xSpeed = Math.cos(angle) * Math.cos(verticalAngle) * speed;
            double ySpeed = Math.sin(verticalAngle) * speed + 0.1D;
            double zSpeed = Math.sin(angle) * Math.cos(verticalAngle) * speed;
            serverLevel.sendParticles((SimpleParticleType) ModParticleTypes.SULFUR_SMOKE.get(), center.x, center.y, center.z, 3, xSpeed, ySpeed, zSpeed, 0.2D);
        }

        for (int i = 0; i < dustParticles; i++) {
            double angle = this.random.nextDouble() * 2.0D * Math.PI;
            double radius = this.random.nextDouble() * 1.5D;
            double speed = 0.1D + this.random.nextDouble() * 0.15D;
            double xSpeed = Math.cos(angle) * speed;
            double ySpeed = 0.05D + this.random.nextDouble() * 0.1D;
            double zSpeed = Math.sin(angle) * speed;
            serverLevel.sendParticles((SimpleParticleType) ModParticleTypes.SULFUR_DUST.get(), center.x + Math.cos(angle) * radius, center.y, center.z + Math.sin(angle) * radius, 2, xSpeed, ySpeed, zSpeed, 0.15D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !(source.getEntity() instanceof Player)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity enemy) {
                if (this.canPerformAttack(enemy) && !SulfurheadEntity.this.isAttacking()) {
                    SulfurheadEntity.this.setAttacking(true);
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(enemy);
                }
            }
        });
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, player -> !((Player) player).isCreative() && !((Player) player).isSpectator()));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SULFURHEAD_IDLE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return ModSounds.SULFURHEAD_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SULFURHEAD_DIE.get();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Swell", (short) this.swell);
        tag.putBoolean("HasTriggeredGasCloud", this.hasTriggeredGasCloud);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.swell = tag.getShort("Swell");
        this.oldSwell = this.swell;
        this.hasTriggeredGasCloud = tag.getBoolean("HasTriggeredGasCloud");
    }
}
