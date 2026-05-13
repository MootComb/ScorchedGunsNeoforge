package top.ribs.scguns.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.ai.GunAttackGoal;
import top.ribs.scguns.item.GunItem;

public abstract class LateRaidGunnerEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(LateRaidGunnerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT = SynchedEntityData.defineId(LateRaidGunnerEntity.class, EntityDataSerializers.INT);
    private static final int ALERT_RANGE_XZ = 32;
    private static final int ALERT_RANGE_Y = 10;

    private int ticksUntilNextAlert;

    protected LateRaidGunnerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    protected static AttributeSupplier.Builder lateRaidGunnerAttributes(double health, double followRange, double speed, double armor, double damage) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.FOLLOW_RANGE, followRange)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, damage);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(ATTACK_TIMEOUT, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GunAttackGoal<>(this, defaultAiDifficulty()));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            protected void checkAndPerformAttack(LivingEntity enemy) {
                if (this.canPerformAttack(enemy) && !LateRaidGunnerEntity.this.isAttacking()) {
                    LateRaidGunnerEntity.this.setAttacking(true);
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(enemy);
                }
            }
        });
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, player -> !(player instanceof Player target) || (!target.isCreative() && !target.isSpectator())));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);
                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }
            if (this.getTarget() != null) {
                this.maybeAlertAllies();
            }
        }
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float amount = this.getPose() == Pose.STANDING ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(amount, 0.2F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        if (this.getMainHandItem().isEmpty()) {
            Item defaultGun = this.getDefaultGun();
            if (defaultGun != null) {
                this.equipMobGun(new ItemStack(defaultGun));
            }
        }
        return result;
    }

    @Nullable
    protected abstract Item getDefaultGun();

    protected int defaultAiDifficulty() {
        return 3;
    }

    protected void equipMobGun(ItemStack stack) {
        if (stack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(stack);
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putBoolean("IgnoreAmmo", true);
            tag.putInt("AmmoCount", Math.max(1, gun.getReloads().getMaxAmmo()));
            tag.putBoolean("scguns:MobGun", true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, stack);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
        this.getPersistentData().putBoolean("scguns:GunnerMob", true);
        this.getPersistentData().putInt("scguns:GunnerAiDifficulty", defaultAiDifficulty());
    }

    private void maybeAlertAllies() {
        if (this.ticksUntilNextAlert > 0) {
            this.ticksUntilNextAlert--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.getSensing().hasLineOfSight(target)) {
            AABB alertArea = AABB.ofSize(this.position(), ALERT_RANGE_XZ * 2.0D, ALERT_RANGE_Y * 2.0D, ALERT_RANGE_XZ * 2.0D);
            this.level().getEntitiesOfClass(this.getClass(), alertArea, EntitySelector.NO_SPECTATORS)
                    .stream()
                    .filter(entity -> entity != this)
                    .filter(entity -> entity.getTarget() == null)
                    .filter(entity -> !entity.isAlliedTo(target))
                    .forEach(entity -> entity.setTarget(target));
        }
        this.ticksUntilNextAlert = 20 + this.random.nextInt(20);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(12);
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

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        this.setAttacking(true);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AlertCooldown", this.ticksUntilNextAlert);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ticksUntilNextAlert = tag.getInt("AlertCooldown");
    }
}
