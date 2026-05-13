package top.ribs.scguns.entity.projectile;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.config.RaidConfig;
import top.ribs.scguns.config.RaidFlareConfig;
import top.ribs.scguns.entity.raid.RaidManager;
import top.ribs.scguns.init.ModEntities;

import javax.annotation.Nullable;

public class RaidFlareEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<String> RAID_ID = SynchedEntityData.defineId(RaidFlareEntity.class, EntityDataSerializers.STRING);
    private String raidId = "";
    private boolean hasBurst;

    public RaidFlareEntity(EntityType<? extends RaidFlareEntity> type, Level level) {
        super(type, level);
    }

    public RaidFlareEntity(Level level, LivingEntity shooter, String raidId) {
        super(ModEntities.RAID_FLARE.get(), shooter, level);
        this.raidId = raidId;
        this.entityData.set(RAID_ID, raidId);
    }

    public String getRaidId() {
        if (this.raidId == null || this.raidId.isEmpty()) {
            this.raidId = this.entityData.get(RAID_ID);
        }
        return this.raidId;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RAID_ID, "");
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && !this.hasBurst) {
            burst();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide && !this.hasBurst) {
            burst();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public void tick() {
        super.tick();
        String currentRaidId = getRaidId();
        if (currentRaidId == null || currentRaidId.isEmpty()) {
            if (!this.level().isClientSide) {
                this.discard();
            }
            return;
        }

        RaidFlareConfig.FlareData flareData = RaidFlareConfig.getFlareData(currentRaidId);
        if (flareData == null) {
            if (!this.level().isClientSide && this.tickCount > 200) {
                this.discard();
            }
            return;
        }

        if (this.level().isClientSide && this.tickCount % 2 == 0) {
            spawnTrailParticles(flareData);
        }

        if (!this.level().isClientSide && this.tickCount >= flareData.burstDelay() && !this.hasBurst) {
            burst();
        }

        if (!this.level().isClientSide && this.hasBurst && this.tickCount >= flareData.burstDelay() + 40) {
            this.discard();
        }

        Vec3 motion = this.getDeltaMovement();
        if (!this.onGround() && !this.hasBurst) {
            this.setDeltaMovement(motion.x * 0.99D, motion.y - 0.04D, motion.z * 0.99D);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void burst() {
        RaidFlareConfig.FlareData flareData = RaidFlareConfig.getFlareData(getRaidId());
        if (flareData == null) {
            return;
        }
        performBurst(flareData);
        this.hasBurst = true;
    }

    private void spawnTrailParticles(RaidFlareConfig.FlareData flareData) {
        for (RaidFlareConfig.ParticleEffect effect : flareData.trailParticles()) {
            ParticleOptions particle = getParticleType(effect.particleType());
            if (particle == null) {
                continue;
            }
            for (int i = 0; i < effect.count(); i++) {
                double offsetX = (this.random.nextDouble() - 0.5D) * effect.spread();
                double offsetY = (this.random.nextDouble() - 0.5D) * effect.spread();
                double offsetZ = (this.random.nextDouble() - 0.5D) * effect.spread();
                this.level().addParticle(particle, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                        offsetX * effect.speed(), offsetY * effect.speed(), offsetZ * effect.speed());
            }
        }
    }

    private void performBurst(RaidFlareConfig.FlareData flareData) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse(flareData.burstSound())).orElse(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.AMBIENT, flareData.burstSoundVolume(), flareData.burstSoundPitch());

        for (RaidFlareConfig.ParticleEffect effect : flareData.burstParticles()) {
            ParticleOptions particle = getParticleType(effect.particleType());
            if (particle != null) {
                serverLevel.sendParticles(particle, this.getX(), this.getY(), this.getZ(), effect.count(), effect.spread(), effect.spread(), effect.spread(), effect.speed());
            }
        }

        if (this.getOwner() instanceof ServerPlayer player) {
            RaidConfig.RaidData config = RaidConfig.getRaidByRaidId(flareData.raidId());
            if (config != null) {
                RaidManager.get(serverLevel).startRaidFromPlayer(config, serverLevel, player);
            }
        }
    }

    @Nullable
    private ParticleOptions getParticleType(String particleId) {
        try {
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(particleId));
            return type instanceof ParticleOptions options ? options : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("RaidId", getRaidId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RaidId")) {
            this.raidId = tag.getString("RaidId");
            this.entityData.set(RAID_ID, this.raidId);
        }
    }
}
