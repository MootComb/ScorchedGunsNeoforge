package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.ai.GunAttackGoal;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;

import java.util.Objects;
import java.util.Optional;

public class ViventrumEntity extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT = SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PATROLLING = SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> PATROL_ORIGIN = SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> PARTYING = SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DEFENSIVE = SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ARMOR_PLATES = SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEAVY_ARMOR_PLATES = SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.INT);
    private static final ResourceLocation DEFENSIVE_ARMOR_BONUS_ID = ResourceLocation.fromNamespaceAndPath("scguns", "viventrum_defensive_bonus");
    private static final String MOB_GUN_KEY = "scguns:MobGun";
    private static final String VIVENTRUM_MOB_GUN_KEY = "scguns:ViventrumMobGun";
    private static final String VIVENTRUM_HAD_AMMO_KEY = "scguns:ViventrumHadAmmoCount";
    private static final String VIVENTRUM_ORIGINAL_AMMO_KEY = "scguns:ViventrumOriginalAmmoCount";
    private static final String VIVENTRUM_HAD_IGNORE_AMMO_KEY = "scguns:ViventrumHadIgnoreAmmo";
    private static final String VIVENTRUM_ORIGINAL_IGNORE_AMMO_KEY = "scguns:ViventrumOriginalIgnoreAmmo";

    private int patrolTimer;
    @Nullable
    private BlockPos currentPatrolTarget;

    public ViventrumEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.3D);
    }

    @NotNull
    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(ATTACK_TIMEOUT, 0);
        builder.define(PATROLLING, false);
        builder.define(PATROL_ORIGIN, Optional.empty());
        builder.define(PARTYING, false);
        builder.define(DEFENSIVE, false);
        builder.define(ARMOR_PLATES, 0);
        builder.define(HEAVY_ARMOR_PLATES, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.isTame()) {
            this.updateDefensiveState();
        }
        this.ensureMainHandGunHasMobAmmo();
        if (this.isAttacking() && this.getAttackTimeout() > 0) {
            this.setAttackTimeout(this.getAttackTimeout() - 1);
            if (this.getAttackTimeout() == 5) {
                LivingEntity target = this.getTarget();
                if (target != null && this.distanceToSqr(target) <= this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + target.getBbWidth()) {
                    this.doHurtTarget(target);
                }
            }
            if (this.getAttackTimeout() <= 0) {
                this.setAttacking(false);
            }
        }
        if (this.isPatrolling()) {
            this.handlePatrolling();
        }
    }

    @Override
    protected void registerGoals() {
        ItemStack mainHandItem = this.getMainHandItem();
        if (mainHandItem.getItem() instanceof GunItem) {
            int difficulty = this.level().getDifficulty().getId() + 1;
            this.goalSelector.addGoal(2, new GunAttackGoal<>(this, difficulty) {
                @Override
                public boolean canUse() {
                    return !ViventrumEntity.this.isDefensive() && super.canUse();
                }

                @Override
                public boolean canContinueToUse() {
                    return !ViventrumEntity.this.isDefensive() && super.canContinueToUse();
                }
            });
        } else {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false) {
                @Override
                public boolean canUse() {
                    return !ViventrumEntity.this.isDefensive() && super.canUse();
                }

                @Override
                public boolean canContinueToUse() {
                    return !ViventrumEntity.this.isDefensive() && super.canContinueToUse();
                }

                @Override
                protected void checkAndPerformAttack(LivingEntity enemy) {
                    if (!ViventrumEntity.this.isAttacking() && this.canPerformAttack(enemy)) {
                        ViventrumEntity.this.setAttacking(true);
                        this.resetAttackCooldown();
                        this.mob.swing(InteractionHand.MAIN_HAND);
                    }
                }
            });
        }

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new ViventrumFollowOwnerGoal(this, 1.3D, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
    }

    @NotNull
    @Override
    public InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.level().isClientSide) {
            boolean shouldConsume = this.isOwnedBy(player) || this.isTame() || itemStack.is(Items.DIAMOND) && !this.isTame();
            return shouldConsume ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        if (this.isTame()) {
            return this.interactTamed(player, itemStack, hand);
        }
        if (itemStack.is(Items.DIAMOND)) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            if (this.random.nextInt(3) == 0 && !EventHooks.onAnimalTame(this, player)) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private InteractionResult interactTamed(Player player, ItemStack itemStack, InteractionHand hand) {
        if (player.isShiftKeyDown() && itemStack.isEmpty()) {
            ItemStack heldItem = this.getMainHandItem();
            if (!heldItem.isEmpty()) {
                this.spawnAtLocation(cleanViventrumMobGunForReturn(heldItem));
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
            ItemStack helmet = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty()) {
                this.spawnAtLocation(helmet);
                this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && itemStack.is(ModItems.ARMOR_PLATE.get())) {
            return this.addPlateFromPlayer(player, itemStack, false);
        }
        if (player.isShiftKeyDown() && itemStack.is(ModItems.HEAVY_ARMOR_PLATE.get())) {
            return this.addPlateFromPlayer(player, itemStack, true);
        }
        if (player.isShiftKeyDown() && itemStack.getItem() instanceof AxeItem && (this.getArmorPlates() > 0 || this.getHeavyArmorPlates() > 0)) {
            boolean wasHeavy = this.removeArmorPlate();
            this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 0.5F, 1.0F);
            this.spawnAtLocation(new ItemStack(wasHeavy ? ModItems.HEAVY_ARMOR_PLATE.get() : ModItems.ARMOR_PLATE.get()));
            player.displayClientMessage(Component.translatable("message.mechanical_entity.armor_plating_removed", this.getArmorPlates() + this.getHeavyArmorPlates(), 4), true);
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlot.HEAD) {
            if (itemStack.is(ModTags.Items.VIVENTRUM_BANNED_ITEMS)) {
                player.displayClientMessage(Component.translatable("message.viventrum.item_too_heavy"), true);
                return InteractionResult.FAIL;
            }
            ItemStack currentHelmet = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!currentHelmet.isEmpty()) {
                this.spawnAtLocation(currentHelmet);
            }
            this.setItemSlot(EquipmentSlot.HEAD, itemStack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && !itemStack.isEmpty() && !(itemStack.getItem() instanceof ArmorItem)) {
            if (itemStack.is(ModTags.Items.VIVENTRUM_BANNED_ITEMS)) {
                player.displayClientMessage(Component.translatable("message.viventrum.item_too_heavy"), true);
                return InteractionResult.FAIL;
            }
            ItemStack currentWeapon = this.getMainHandItem();
            if (!currentWeapon.isEmpty()) {
                this.spawnAtLocation(cleanViventrumMobGunForReturn(currentWeapon));
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown() && itemStack.is(ModItems.REPAIR_KIT.get()) && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            this.heal(10.0F);
            this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 0.5F, 1.0F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5D, this.getZ(), 3, 0.3D, 0.3D, 0.3D, 0.1D);
            }
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown() && itemStack.isEmpty()) {
            Component entityName = this.hasCustomName() ? this.getCustomName() : Component.translatable("entity.scguns.viventrum");
            if (this.isOrderedToSit()) {
                this.setOrderedToSit(false);
                this.setPatrolling(false);
                player.displayClientMessage(Component.translatable("message.viventrum.following", entityName), true);
            } else if (this.isPatrolling()) {
                this.setPatrolling(false);
                this.setOrderedToSit(true);
                player.displayClientMessage(Component.translatable("message.viventrum.sitting", entityName), true);
            } else {
                this.setPatrolling(true);
                this.setPatrolOrigin(this.blockPosition());
                this.spawnPatrolOriginParticles();
                player.displayClientMessage(Component.translatable("message.viventrum.patrolling", entityName), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult addPlateFromPlayer(Player player, ItemStack itemStack, boolean heavy) {
        if (this.getArmorPlates() + this.getHeavyArmorPlates() >= 4) {
            player.displayClientMessage(Component.translatable("message.mechanical_entity.max_armor_plates"), true);
            return InteractionResult.FAIL;
        }
        this.addArmorPlate(heavy);
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        this.playSound(heavy ? SoundEvents.ANVIL_PLACE : SoundEvents.IRON_GOLEM_REPAIR, 0.5F, 1.0F);
        player.displayClientMessage(Component.translatable(heavy ? "message.mechanical_entity.heavy_armor_plating_added" : "message.mechanical_entity.armor_plating_added", this.getArmorPlates() + this.getHeavyArmorPlates(), 4), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean canReplaceCurrentItem(ItemStack candidate, ItemStack existing) {
        return existing.isEmpty() && candidate.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        ItemStack oldStack = this.getItemBySlot(slot);
        ItemStack equippedStack = stack;
        if (!this.level().isClientSide && slot == EquipmentSlot.MAINHAND) {
            equippedStack = prepareViventrumMobGun(stack);
        }
        super.setItemSlot(slot, equippedStack);
        if (!this.level().isClientSide && slot == EquipmentSlot.MAINHAND) {
            boolean hadGun = oldStack.getItem() instanceof GunItem;
            boolean hasGun = equippedStack.getItem() instanceof GunItem;
            if (hadGun != hasGun) {
                this.goalSelector.removeAllGoals(goal -> true);
                this.registerGoals();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.isTame() && this.isDefensive()) {
            amount *= 0.4F;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void setRecordPlayingNearby(@NotNull BlockPos pos, boolean playing) {
        this.setPartying(playing && this.isTame());
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float f = this.getPose() == Pose.STANDING ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(f, 0.2F);
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        ItemStack mainHandItem = this.getMainHandItem();
        if (!mainHandItem.isEmpty()) {
            this.spawnAtLocation(cleanViventrumMobGunForReturn(mainHandItem));
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        ItemStack helmet = this.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            this.spawnAtLocation(helmet);
            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }
        if (this.getArmorPlates() > 0) {
            this.spawnAtLocation(new ItemStack(ModItems.ARMOR_PLATE.get(), this.getArmorPlates()));
        }
        if (this.getHeavyArmorPlates() > 0) {
            this.spawnAtLocation(new ItemStack(ModItems.HEAVY_ARMOR_PLATE.get(), this.getHeavyArmorPlates()));
        }
    }

    private void ensureMainHandGunHasMobAmmo() {
        ItemStack mainHandItem = this.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof GunItem) || isPreparedViventrumMobGun(mainHandItem)) {
            return;
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, mainHandItem);
    }

    private static boolean isPreparedViventrumMobGun(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) {
            return false;
        }
        CompoundTag tag = data.copyTag();
        return tag.getBoolean(VIVENTRUM_MOB_GUN_KEY) && tag.getBoolean("IgnoreAmmo") && tag.getInt("AmmoCount") > 0;
    }

    private static ItemStack prepareViventrumMobGun(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem gunItem)) {
            return stack;
        }

        ItemStack prepared = stack.copy();
        Gun gun = gunItem.getModifiedGun(prepared);
        CompoundTag tag = prepared.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.getBoolean(VIVENTRUM_MOB_GUN_KEY)) {
            tag.putBoolean(VIVENTRUM_HAD_AMMO_KEY, tag.contains("AmmoCount"));
            tag.putInt(VIVENTRUM_ORIGINAL_AMMO_KEY, tag.getInt("AmmoCount"));
            tag.putBoolean(VIVENTRUM_HAD_IGNORE_AMMO_KEY, tag.contains("IgnoreAmmo"));
            tag.putBoolean(VIVENTRUM_ORIGINAL_IGNORE_AMMO_KEY, tag.getBoolean("IgnoreAmmo"));
        }
        tag.putBoolean("IgnoreAmmo", true);
        tag.putInt("AmmoCount", Math.max(1, gun.getReloads().getMaxAmmo()));
        tag.putBoolean(MOB_GUN_KEY, true);
        tag.putBoolean(VIVENTRUM_MOB_GUN_KEY, true);
        prepared.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return prepared;
    }

    private static ItemStack cleanViventrumMobGunForReturn(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem)) {
            return stack.copy();
        }

        ItemStack cleaned = stack.copy();
        CustomData data = cleaned.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) {
            return cleaned;
        }

        CompoundTag tag = data.copyTag();
        if (!tag.getBoolean(VIVENTRUM_MOB_GUN_KEY)) {
            return cleaned;
        }

        if (tag.getBoolean(VIVENTRUM_HAD_AMMO_KEY)) {
            tag.putInt("AmmoCount", tag.getInt(VIVENTRUM_ORIGINAL_AMMO_KEY));
        } else {
            tag.remove("AmmoCount");
        }
        if (tag.getBoolean(VIVENTRUM_HAD_IGNORE_AMMO_KEY)) {
            tag.putBoolean("IgnoreAmmo", tag.getBoolean(VIVENTRUM_ORIGINAL_IGNORE_AMMO_KEY));
        } else {
            tag.remove("IgnoreAmmo");
        }
        tag.remove(MOB_GUN_KEY);
        tag.remove(VIVENTRUM_MOB_GUN_KEY);
        tag.remove(VIVENTRUM_HAD_AMMO_KEY);
        tag.remove(VIVENTRUM_ORIGINAL_AMMO_KEY);
        tag.remove(VIVENTRUM_HAD_IGNORE_AMMO_KEY);
        tag.remove(VIVENTRUM_ORIGINAL_IGNORE_AMMO_KEY);

        if (tag.isEmpty()) {
            cleaned.remove(DataComponents.CUSTOM_DATA);
        } else {
            cleaned.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return cleaned;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Attacking", this.isAttacking());
        tag.putInt("AttackTimeout", this.getAttackTimeout());
        tag.putBoolean("Patrolling", this.isPatrolling());
        tag.putBoolean("Defensive", this.isDefensive());
        tag.putInt("ArmorPlates", this.getArmorPlates());
        tag.putInt("HeavyArmorPlates", this.getHeavyArmorPlates());
        this.getPatrolOrigin().ifPresent(pos -> {
            tag.putInt("PatrolOriginX", pos.getX());
            tag.putInt("PatrolOriginY", pos.getY());
            tag.putInt("PatrolOriginZ", pos.getZ());
        });
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttacking(tag.getBoolean("Attacking"));
        this.setAttackTimeout(tag.getInt("AttackTimeout"));
        this.setPatrolling(tag.getBoolean("Patrolling"));
        if (tag.contains("Defensive")) {
            this.setDefensive(tag.getBoolean("Defensive"));
        }
        if (tag.contains("ArmorPlates")) {
            this.setArmorPlates(tag.getInt("ArmorPlates"));
        }
        if (tag.contains("HeavyArmorPlates")) {
            this.setHeavyArmorPlates(tag.getInt("HeavyArmorPlates"));
        }
        if (tag.contains("PatrolOriginX") && tag.contains("PatrolOriginY") && tag.contains("PatrolOriginZ")) {
            this.setPatrolOrigin(new BlockPos(tag.getInt("PatrolOriginX"), tag.getInt("PatrolOriginY"), tag.getInt("PatrolOriginZ")));
        }
    }

    @Override
    public void setTame(boolean tamed, boolean applyTamingSideEffects) {
        super.setTame(tamed, applyTamingSideEffects);
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(tamed ? 40.0D : 24.0D);
        if (tamed) {
            this.setHealth(40.0F);
        }
        this.goalSelector.removeAllGoals(goal -> true);
        this.registerGoals();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.DIAMOND);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(10);
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

    public boolean isPatrolling() {
        return this.entityData.get(PATROLLING);
    }

    public void setPatrolling(boolean patrolling) {
        this.entityData.set(PATROLLING, patrolling);
    }

    public Optional<BlockPos> getPatrolOrigin() {
        return this.entityData.get(PATROL_ORIGIN);
    }

    public void setPatrolOrigin(@Nullable BlockPos pos) {
        this.entityData.set(PATROL_ORIGIN, pos != null ? Optional.of(pos) : Optional.empty());
    }

    public boolean isPartying() {
        return this.entityData.get(PARTYING);
    }

    public void setPartying(boolean partying) {
        this.entityData.set(PARTYING, partying);
    }

    public boolean isDefensive() {
        return this.entityData.get(DEFENSIVE);
    }

    public void setDefensive(boolean defensive) {
        this.entityData.set(DEFENSIVE, defensive);
        AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
        if (armor == null) {
            return;
        }
        if (defensive) {
            armor.addOrReplacePermanentModifier(new AttributeModifier(DEFENSIVE_ARMOR_BONUS_ID, 15.0D, AttributeModifier.Operation.ADD_VALUE));
        } else {
            armor.removeModifier(DEFENSIVE_ARMOR_BONUS_ID);
        }
    }

    public int getArmorPlates() {
        return this.entityData.get(ARMOR_PLATES);
    }

    public int getHeavyArmorPlates() {
        return this.entityData.get(HEAVY_ARMOR_PLATES);
    }

    public void setArmorPlates(int plates) {
        this.entityData.set(ARMOR_PLATES, Math.max(0, plates));
        this.updateArmorFromPlates();
    }

    public void setHeavyArmorPlates(int plates) {
        this.entityData.set(HEAVY_ARMOR_PLATES, Math.max(0, plates));
        this.updateArmorFromPlates();
    }

    public void addArmorPlate(boolean heavy) {
        if (this.getArmorPlates() + this.getHeavyArmorPlates() >= 4) {
            return;
        }
        if (heavy) {
            this.setHeavyArmorPlates(this.getHeavyArmorPlates() + 1);
        } else {
            this.setArmorPlates(this.getArmorPlates() + 1);
        }
    }

    public boolean removeArmorPlate() {
        if (this.getHeavyArmorPlates() > 0) {
            this.setHeavyArmorPlates(this.getHeavyArmorPlates() - 1);
            return true;
        }
        if (this.getArmorPlates() > 0) {
            this.setArmorPlates(this.getArmorPlates() - 1);
        }
        return false;
    }

    public void spawnPatrolOriginParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = this.getPatrolOrigin().orElse(this.blockPosition());
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.5D, 0.5D, 0.5D, 0.1D);
        }
    }

    private void updateDefensiveState() {
        boolean shouldBeDefensive = this.getHealth() / this.getMaxHealth() <= 0.25F;
        if (shouldBeDefensive != this.isDefensive()) {
            this.setDefensive(shouldBeDefensive);
            if (shouldBeDefensive) {
                this.navigation.stop();
                this.setTarget(null);
            }
        }
    }

    private void handlePatrolling() {
        Optional<BlockPos> patrolOrigin = this.getPatrolOrigin();
        if (patrolOrigin.isEmpty()) {
            return;
        }
        BlockPos origin = patrolOrigin.get();
        if (this.patrolTimer <= 0) {
            if (this.random.nextFloat() < 0.5F) {
                this.currentPatrolTarget = origin.offset(this.random.nextInt(18) - 9, 0, this.random.nextInt(18) - 9);
                this.navigation.moveTo(this.currentPatrolTarget.getX() + 0.5D, this.currentPatrolTarget.getY(), this.currentPatrolTarget.getZ() + 0.5D, 0.8D);
                this.patrolTimer = 80;
            } else {
                this.navigation.stop();
                this.currentPatrolTarget = null;
                this.patrolTimer = 50;
            }
        } else {
            this.patrolTimer--;
            if (this.currentPatrolTarget != null && this.distanceToSqr(this.currentPatrolTarget.getX(), this.currentPatrolTarget.getY(), this.currentPatrolTarget.getZ()) < 4.0D) {
                this.navigation.stop();
                this.currentPatrolTarget = null;
                this.patrolTimer = 20;
            }
        }
        if (this.distanceToSqr(origin.getX(), origin.getY(), origin.getZ()) > 144.0D) {
            this.navigation.moveTo(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D, 1.0D);
            this.currentPatrolTarget = null;
            this.patrolTimer = 40;
        }
    }

    private void updateArmorFromPlates() {
        AttributeInstance armor = this.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.setBaseValue(2.0D + this.getArmorPlates() + this.getHeavyArmorPlates() * 2.0D);
        }
    }

    public static class ViventrumFollowOwnerGoal extends FollowOwnerGoal {
        private final ViventrumEntity viventrum;

        public ViventrumFollowOwnerGoal(ViventrumEntity viventrum, double speed, float minDistance, float maxDistance) {
            super(viventrum, speed, minDistance, maxDistance);
            this.viventrum = viventrum;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.viventrum.isPatrolling();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !this.viventrum.isPatrolling();
        }
    }
}
