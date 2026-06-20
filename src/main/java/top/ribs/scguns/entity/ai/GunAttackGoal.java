package top.ribs.scguns.entity.ai;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.DistantGunSoundRouter;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.entity.weapon.ScGunsWeapon;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageGunSound;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.EnumSet;

public class GunAttackGoal<T extends PathfinderMob> extends Goal {
    private static final double DEFAULT_MAX_ATTACK_RANGE = 32.0D;
    private static final double MIN_MAX_ATTACK_RANGE = 8.0D;
    private static final double MAX_MAX_ATTACK_RANGE = 48.0D;
    private static final double CLOSE_STOP_RANGE_SQR = 36.0D;
    private static final double NAVIGATION_TARGET_RECALC_DISTANCE_SQR = 4.0D;
    private static final int NAVIGATION_RECALC_INTERVAL = 10;
    private static final String MOB_GUN_KEY = "scguns:MobGun";
    private static final String VIVENTRUM_MOB_GUN_KEY = "scguns:ViventrumMobGun";

    private final T shooter;
    private final int aiDifficulty;
    private int seeTime;
    private int attackTime;
    private int burstTimer;
    private int remainingBursts;
    private int reloadTime;
    private int navigationUpdateCooldown;
    private double lastNavigationTargetX = Double.NaN;
    private double lastNavigationTargetY = Double.NaN;
    private double lastNavigationTargetZ = Double.NaN;
    private boolean reloading;
    private ScGunsWeapon weapon;
    private ItemStack cachedStack = ItemStack.EMPTY;

    public GunAttackGoal(T shooter, int aiDifficulty) {
        this.shooter = shooter;
        this.aiDifficulty = Math.max(1, aiDifficulty);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = shooter.getTarget();
        return target != null && target.isAlive() && isHoldingGun();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        shooter.setAggressive(true);
        attackTime = 0;
        seeTime = 0;
        remainingBursts = 0;
        burstTimer = 0;
        reloadTime = 0;
        resetNavigationThrottle();
        reloading = false;
    }

    @Override
    public void stop() {
        shooter.setAggressive(false);
        seeTime = 0;
        remainingBursts = 0;
        burstTimer = 0;
        reloadTime = 0;
        resetNavigationThrottle();
        reloading = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = shooter.getTarget();
        if (target == null || !target.isAlive() || !isHoldingGun()) {
            return;
        }

        double distance = shooter.distanceToSqr(target);
        double maxAttackRange = getMaxAttackRange();
        double maxAttackRangeSqr = maxAttackRange * maxAttackRange;
        boolean canSee = shooter.getSensing().hasLineOfSight(target);
        seeTime = canSee ? seeTime + 1 : 0;

        updateCombatNavigation(target, distance);

        shooter.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (!weapon.isLoaded()) {
            tickReload();
            return;
        }

        reloading = false;
        reloadTime = 0;

        if (distance > maxAttackRangeSqr) {
            seeTime = 0;
            remainingBursts = 0;
            burstTimer = 0;
            return;
        }

        if (!canSee || seeTime < 5) {
            return;
        }

        if (attackTime > 0) {
            attackTime--;
            return;
        }

        if (burstTimer > 0) {
            burstTimer--;
            return;
        }

        boolean nativeBurstWeapon = isNativeBurstWeapon();
        boolean continuingNativeBurst = nativeBurstWeapon && remainingBursts > 0;
        shoot(target);

        if (nativeBurstWeapon) {
            if (continuingNativeBurst) {
                remainingBursts--;
            } else {
                remainingBursts = Math.max(0, weapon.getBurstAmount() - 1);
            }
            if (remainingBursts > 0) {
                burstTimer = weapon.getBurstCooldown();
            } else {
                attackTime = Math.max(getMobAttackCooldown(), getBurstResetDelay());
            }
        } else {
            attackTime = getMobAttackCooldown();
        }
    }

    private boolean isHoldingGun() {
        ItemStack stack = shooter.getMainHandItem();
        if (ItemStack.isSameItemSameComponents(stack, cachedStack)) {
            return weapon != null;
        }
        cachedStack = stack.copy();
        if (stack.getItem() instanceof GunItem) {
            weapon = new ScGunsWeapon(stack);
            return true;
        }
        weapon = null;
        return false;
    }

    private void updateCombatNavigation(LivingEntity target, double distanceSqr) {
        if (distanceSqr < CLOSE_STOP_RANGE_SQR) {
            if (!shooter.getNavigation().isDone()) {
                shooter.getNavigation().stop();
            }
            resetNavigationThrottle();
            rememberNavigationTarget(target);
            return;
        }

        if (navigationUpdateCooldown > 0) {
            navigationUpdateCooldown--;
        }

        if (navigationUpdateCooldown > 0 && !shooter.getNavigation().isDone() && !hasNavigationTargetMoved(target)) {
            return;
        }

        shooter.getNavigation().moveTo(target, getCombatMoveSpeed());
        rememberNavigationTarget(target);
        navigationUpdateCooldown = NAVIGATION_RECALC_INTERVAL + shooter.getRandom().nextInt(5);
    }

    private boolean hasNavigationTargetMoved(LivingEntity target) {
        if (Double.isNaN(lastNavigationTargetX)) {
            return true;
        }
        double dx = target.getX() - lastNavigationTargetX;
        double dy = target.getY() - lastNavigationTargetY;
        double dz = target.getZ() - lastNavigationTargetZ;
        return dx * dx + dy * dy + dz * dz > NAVIGATION_TARGET_RECALC_DISTANCE_SQR;
    }

    private void rememberNavigationTarget(LivingEntity target) {
        lastNavigationTargetX = target.getX();
        lastNavigationTargetY = target.getY();
        lastNavigationTargetZ = target.getZ();
    }

    private void resetNavigationThrottle() {
        navigationUpdateCooldown = 0;
        lastNavigationTargetX = Double.NaN;
        lastNavigationTargetY = Double.NaN;
        lastNavigationTargetZ = Double.NaN;
    }

    private void shoot(LivingEntity target) {
        if (weapon == null || !weapon.isLoaded() || !isTargetWithinAttackRange(target)) {
            return;
        }

        double spread = Math.max(0.02D, 0.35D - aiDifficulty * 0.08D) * getInaccuracyMultiplier();
        double x = target.getX() + (shooter.getRandom().nextDouble() - 0.5D) * spread;
        double y = (target.getEyeY() + target.getY()) * 0.5D + (shooter.getRandom().nextDouble() - 0.5D) * spread;
        double z = target.getZ() + (shooter.getRandom().nextDouble() - 0.5D) * spread;

        SoundEvent sound = weapon.getShootSound();
        if (sound != null) {
            playShootSound(sound);
        }
        weapon.performRangedAttackIWeapon(shooter, x, y, z, weapon.getProjectileSpeed());
        shooter.swing(shooter.getUsedItemHand());
    }

    private double getInaccuracyMultiplier() {
        ItemStack stack = shooter.getMainHandItem();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.getBoolean(VIVENTRUM_MOB_GUN_KEY)) {
            return 1.0D;
        }
        return Config.COMMON.gunnerMobs.gunnerMobInaccuracyMultiplier.get();
    }

    private int getMobAttackCooldown() {
        if (weapon == null) {
            return 20;
        }
        double multiplier = Config.COMMON.gameplay.mobFireRateMultiplier.get();
        return Math.max(1, (int) (weapon.getMobAttackCooldown() * multiplier));
    }

    private boolean isNativeBurstWeapon() {
        return weapon != null && FireMode.BURST.equals(weapon.getFireMode());
    }

    private double getCombatMoveSpeed() {
        return Math.max(0.8D, weapon != null ? weapon.getMoveSpeedAmp() : 0.4D);
    }

    private int getBurstResetDelay() {
        double multiplier = Config.COMMON.gameplay.mobBurstDelayMultiplier.get();
        int base = 40 + shooter.getRandom().nextInt(40);
        return Math.max(5, (int) (base * multiplier));
    }

    private void tickReload() {
        shooter.getNavigation().stop();
        if (!reloading) {
            reloading = true;
            reloadTime = weapon.getMobReloadTime();
            SoundEvent reloadSound = weapon.getLoadSound();
            if (reloadSound != null) {
                shooter.level().playSound(null, shooter, reloadSound, SoundSource.HOSTILE, 0.8F, 1.0F);
            }
        }

        if (reloadTime > 0) {
            reloadTime--;
            return;
        }

        refillCurrentWeapon();
        reloading = false;
        reloadTime = 0;
        attackTime = Math.max(5, getMobAttackCooldown());
    }

    private void refillCurrentWeapon() {
        ItemStack stack = shooter.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem)) {
            return;
        }

        ScGunsWeapon currentWeapon = new ScGunsWeapon(stack);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.getBoolean(MOB_GUN_KEY) && !tag.getBoolean(VIVENTRUM_MOB_GUN_KEY)) {
            tag.remove("IgnoreAmmo");
        }
        tag.putInt("AmmoCount", currentWeapon.getMaxAmmo());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        weapon = new ScGunsWeapon(stack);
        cachedStack = stack.copy();
    }

    private boolean isTargetWithinAttackRange(LivingEntity target) {
        double maxAttackRange = getMaxAttackRange();
        return shooter.distanceToSqr(target) <= maxAttackRange * maxAttackRange;
    }

    private double getMaxAttackRange() {
        double followRange = shooter.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (!Double.isFinite(followRange) || followRange <= 0.0D) {
            return DEFAULT_MAX_ATTACK_RANGE;
        }
        return Math.max(MIN_MAX_ATTACK_RANGE, Math.min(MAX_MAX_ATTACK_RANGE, followRange));
    }

    private void playShootSound(SoundEvent sound) {
        if (!(shooter.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceLocation soundId = BuiltInRegistries.SOUND_EVENT.getKey(sound);
        if (soundId == null) {
            return;
        }

        ItemStack stack = shooter.getMainHandItem();
        double posX = shooter.getX();
        double posY = shooter.getY() + shooter.getEyeHeight();
        double posZ = shooter.getZ();
        float volume = GunModifierHelper.getFireSoundVolume(stack);
        float pitch = 0.9F + shooter.getRandom().nextFloat() * 0.2F;
        double radius = GunModifierHelper.getModifiedFireSoundRadius(stack, Config.SERVER.gunShotMaxDistance.get());
        boolean muzzle = weapon != null && weapon.hasMuzzleFlash();

        S2CMessageGunSound messageSound = new S2CMessageGunSound(
                soundId,
                SoundSource.HOSTILE,
                (float) posX,
                (float) posY,
                (float) posZ,
                volume,
                pitch,
                shooter.getId(),
                muzzle,
                false
        );

        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(serverLevel, posX, posY, posZ, radius),
                messageSound
        );
        DistantGunSoundRouter.send(
                serverLevel,
                new net.minecraft.world.phys.Vec3(posX, posY, posZ),
                SoundSource.HOSTILE,
                volume,
                pitch,
                shooter.getId(),
                GunModifierHelper.isSilencedFire(stack),
                radius
        );
    }
}
