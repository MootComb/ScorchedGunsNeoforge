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
    private static final double PREFERRED_STOP_RANGE_SQR = 196.0D;
    private static final String MOB_GUN_KEY = "scguns:MobGun";
    private static final String VIVENTRUM_MOB_GUN_KEY = "scguns:ViventrumMobGun";

    private final T shooter;
    private final int aiDifficulty;
    private int seeTime;
    private int attackTime;
    private int burstTimer;
    private int remainingBursts;
    private int reloadTime;
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
        reloading = false;
    }

    @Override
    public void stop() {
        shooter.setAggressive(false);
        seeTime = 0;
        remainingBursts = 0;
        burstTimer = 0;
        reloadTime = 0;
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

        if (distance < CLOSE_STOP_RANGE_SQR) {
            shooter.getNavigation().stop();
        } else if (distance > PREFERRED_STOP_RANGE_SQR) {
            shooter.getNavigation().moveTo(target, weapon != null ? weapon.getMoveSpeedAmp() : 0.4D);
        } else {
            shooter.getNavigation().stop();
        }

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

        shoot(target);

        if (remainingBursts > 0) {
            remainingBursts--;
            burstTimer = Math.max(getWeaponCooldown(), Math.max(3, 8 - aiDifficulty));
        } else {
            remainingBursts = aiDifficulty > 1 ? shooter.getRandom().nextInt(aiDifficulty) : 0;
            attackTime = Math.max(10, weapon.getAdjustedAttackCooldown(1.4D - Math.min(0.4D, aiDifficulty * 0.1D)));
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

    private int getWeaponCooldown() {
        return weapon != null ? Math.max(1, weapon.getAttackCooldown()) : 20;
    }

    private void tickReload() {
        shooter.getNavigation().stop();
        if (!reloading) {
            reloading = true;
            reloadTime = weapon.getReloadTime();
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
        attackTime = Math.max(5, getWeaponCooldown());
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
    }
}
