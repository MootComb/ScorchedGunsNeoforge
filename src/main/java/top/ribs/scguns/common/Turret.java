package top.ribs.scguns.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Turret {
    private final Targeting targeting = new Targeting();
    private final Combat combat = new Combat();
    private final Ammunition ammunition = new Ammunition();
    private final Behavior behavior = new Behavior();
    private final Display display = new Display();

    public Targeting getTargeting() {
        return targeting;
    }

    public Combat getCombat() {
        return combat;
    }

    public Ammunition getAmmunition() {
        return ammunition;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    public Display getDisplay() {
        return display;
    }

    public static class Targeting {
        private double range = 12.0D;
        private double verticalRange = 12.0D;
        private double minFiringDistance = 1.3D;
        private float rotationSpeed = 0.5F;
        private float positionSmoothing = 0.2F;
        private float maxPitch = 60.0F;
        private float minPitch = -25.0F;
        private int predictionMultiplier = 7;
        private boolean requiresLineOfSight = true;

        public double getRange() {
            return range;
        }

        public void setRange(double range) {
            this.range = range;
        }

        public double getVerticalRange() {
            return verticalRange;
        }

        public void setVerticalRange(double verticalRange) {
            this.verticalRange = verticalRange;
        }

        public double getMinFiringDistance() {
            return minFiringDistance;
        }

        public void setMinFiringDistance(double minFiringDistance) {
            this.minFiringDistance = minFiringDistance;
        }

        public float getRotationSpeed() {
            return rotationSpeed;
        }

        public void setRotationSpeed(float rotationSpeed) {
            this.rotationSpeed = rotationSpeed;
        }

        public float getPositionSmoothing() {
            return positionSmoothing;
        }

        public void setPositionSmoothing(float positionSmoothing) {
            this.positionSmoothing = positionSmoothing;
        }

        public float getMaxPitch() {
            return maxPitch;
        }

        public void setMaxPitch(float maxPitch) {
            this.maxPitch = maxPitch;
        }

        public float getMinPitch() {
            return minPitch;
        }

        public void setMinPitch(float minPitch) {
            this.minPitch = minPitch;
        }

        public int getPredictionMultiplier() {
            return predictionMultiplier;
        }

        public void setPredictionMultiplier(int predictionMultiplier) {
            this.predictionMultiplier = predictionMultiplier;
        }

        public boolean requiresLineOfSight() {
            return requiresLineOfSight;
        }

        public void setRequiresLineOfSight(boolean requiresLineOfSight) {
            this.requiresLineOfSight = requiresLineOfSight;
        }
    }

    public static class Combat {
        private int cooldown = 16;
        private float inaccuracy = 0.05F;
        private int pelletCount = 1;
        private float spreadAngle = 0.0F;
        private float recoilMax = 4.0F;
        private float recoilSpeed = 0.3F;
        private int damageModifier = 2;
        private double projectileSpeed = 3.0D;
        private ResourceLocation fireSound = ResourceLocation.fromNamespaceAndPath("scguns", "item.iron_rifle.fire");

        public int getCooldown() {
            return cooldown;
        }

        public void setCooldown(int cooldown) {
            this.cooldown = cooldown;
        }

        public float getInaccuracy() {
            return inaccuracy;
        }

        public void setInaccuracy(float inaccuracy) {
            this.inaccuracy = inaccuracy;
        }

        public int getPelletCount() {
            return pelletCount;
        }

        public void setPelletCount(int pelletCount) {
            this.pelletCount = pelletCount;
        }

        public float getSpreadAngle() {
            return spreadAngle;
        }

        public void setSpreadAngle(float spreadAngle) {
            this.spreadAngle = spreadAngle;
        }

        public float getRecoilMax() {
            return recoilMax;
        }

        public void setRecoilMax(float recoilMax) {
            this.recoilMax = recoilMax;
        }

        public float getRecoilSpeed() {
            return recoilSpeed;
        }

        public void setRecoilSpeed(float recoilSpeed) {
            this.recoilSpeed = recoilSpeed;
        }

        public int getDamageModifier() {
            return damageModifier;
        }

        public void setDamageModifier(int damageModifier) {
            this.damageModifier = damageModifier;
        }

        public double getProjectileSpeed() {
            return projectileSpeed;
        }

        public void setProjectileSpeed(double projectileSpeed) {
            this.projectileSpeed = projectileSpeed;
        }

        public ResourceLocation getFireSound() {
            return fireSound;
        }

        public void setFireSound(ResourceLocation fireSound) {
            this.fireSound = fireSound;
        }
    }

    public static class Ammunition {
        private final List<AmmoType> acceptedAmmo = new ArrayList<>();
        private float casingEjectChance = 0.65F;

        public List<AmmoType> getAcceptedAmmo() {
            return acceptedAmmo;
        }

        public float getCasingEjectChance() {
            return casingEjectChance;
        }

        public void setCasingEjectChance(float casingEjectChance) {
            this.casingEjectChance = casingEjectChance;
        }

        public void clearAcceptedAmmo() {
            this.acceptedAmmo.clear();
        }

        public void addAmmoType(AmmoType ammoType) {
            this.acceptedAmmo.add(ammoType);
        }

        public static class AmmoType {
            private ResourceLocation item;
            private ResourceLocation bulletType;
            @Nullable
            private ResourceLocation casingType;
            private double damage = 5.0D;
            private float armorPenetration = 0.0F;

            @Nullable
            public Item getItem() {
                return item == null ? null : BuiltInRegistries.ITEM.get(item);
            }

            public ResourceLocation getBulletType() {
                return bulletType;
            }

            public void setBulletType(ResourceLocation bulletType) {
                this.bulletType = bulletType;
            }

            @Nullable
            public ResourceLocation getCasingType() {
                return casingType;
            }

            public void setCasingType(@Nullable ResourceLocation casingType) {
                this.casingType = casingType;
            }

            public double getDamage() {
                return damage;
            }

            public void setDamage(double damage) {
                this.damage = damage;
            }

            public float getArmorPenetration() {
                return armorPenetration;
            }

            public void setArmorPenetration(float armorPenetration) {
                this.armorPenetration = armorPenetration;
            }

            public void setItem(ResourceLocation item) {
                this.item = item;
            }
        }
    }

    public static class Behavior {
        private float restingYaw = 0.0F;
        private float restingPitch = -30.0F;
        private int disableTime = 200;
        private boolean hasOpenAnimation = false;

        public float getRestingYaw() {
            return restingYaw;
        }

        public void setRestingYaw(float restingYaw) {
            this.restingYaw = restingYaw;
        }

        public float getRestingPitch() {
            return restingPitch;
        }

        public void setRestingPitch(float restingPitch) {
            this.restingPitch = restingPitch;
        }

        public int getDisableTime() {
            return disableTime;
        }

        public void setDisableTime(int disableTime) {
            this.disableTime = disableTime;
        }

        public boolean hasOpenAnimation() {
            return hasOpenAnimation;
        }

        public void setHasOpenAnimation(boolean hasOpenAnimation) {
            this.hasOpenAnimation = hasOpenAnimation;
        }
    }

    public static class Display {
        private double muzzleLength = 1.0D;
        private double muzzleOffsetY = 1.4D;

        public double getMuzzleLength() {
            return muzzleLength;
        }

        public void setMuzzleLength(double muzzleLength) {
            this.muzzleLength = muzzleLength;
        }

        public double getMuzzleOffsetY() {
            return muzzleOffsetY;
        }

        public void setMuzzleOffsetY(double muzzleOffsetY) {
            this.muzzleOffsetY = muzzleOffsetY;
        }
    }
}
