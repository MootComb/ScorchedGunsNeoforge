package top.ribs.scguns.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PraetorEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT = SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_VARIATION = SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SECOND_PHASE = SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ROAR_TICK = SynchedEntityData.defineId(PraetorEntity.class, EntityDataSerializers.INT);
    private static final int ROAR_DURATION = 40;

    private boolean hasTriggeredSecondPhase;

    public PraetorEntity(EntityType<? extends PraetorEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(ATTACK_TIMEOUT, 0);
        builder.define(ATTACK_VARIATION, 0);
        builder.define(SECOND_PHASE, false);
        builder.define(ROARING, false);
        builder.define(ROAR_TICK, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity enemy) {
                if (this.canPerformAttack(enemy) && !PraetorEntity.this.isAttacking()) {
                    PraetorEntity.this.setAttacking(true);
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(enemy);
                }
            }
        });
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.0D, 35.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(PraetorEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, player -> !(player instanceof Player target) || (!target.isCreative() && !target.isSpectator())));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.handleSecondPhase();
            this.handleRoaring();
            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);
                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }
        }
    }

    private void handleSecondPhase() {
        if (!this.hasTriggeredSecondPhase && this.getHealth() <= 30.0F) {
            this.hasTriggeredSecondPhase = true;
            this.setInSecondPhase(true);
            this.setRoaring(true);
            this.setRoarTick(0);
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.8F);
        }
    }

    private void handleRoaring() {
        if (!this.isRoaring()) {
            return;
        }
        int currentTick = this.getRoarTick();
        if (currentTick >= ROAR_DURATION) {
            this.setRoaring(false);
            this.setRoarTick(0);
        } else {
            this.setRoarTick(currentTick + 1);
        }
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float amount = this.getPose() == Pose.STANDING ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(amount, 0.2F);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(12);
            this.entityData.set(ATTACK_VARIATION, this.random.nextInt(3));
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttackTimeout(int timeout) {
        this.entityData.set(ATTACK_TIMEOUT, timeout);
    }

    public int getAttackTimeout() {
        return this.entityData.get(ATTACK_TIMEOUT);
    }

    public int getAttackVariation() {
        return this.entityData.get(ATTACK_VARIATION);
    }

    public boolean isInSecondPhase() {
        return this.entityData.get(SECOND_PHASE);
    }

    public void setInSecondPhase(boolean secondPhase) {
        this.entityData.set(SECOND_PHASE, secondPhase);
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public void setRoaring(boolean roaring) {
        this.entityData.set(ROARING, roaring);
    }

    public int getRoarTick() {
        return this.entityData.get(ROAR_TICK);
    }

    public void setRoarTick(int tick) {
        this.entityData.set(ROAR_TICK, tick);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasTriggeredSecondPhase", this.hasTriggeredSecondPhase);
        tag.putBoolean("IsInSecondPhase", this.isInSecondPhase());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hasTriggeredSecondPhase = tag.getBoolean("HasTriggeredSecondPhase");
        if (tag.getBoolean("IsInSecondPhase")) {
            this.setInSecondPhase(true);
        }
    }
}
