package top.ribs.scguns.entity.raid;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.config.RaidConfig;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ActiveRaid {
    private static final int BOSS_VALIDATION_TICKS = 600;
    private static final int BOSS_REVALIDATION_INTERVAL = 100;
    private static final int TARGET_UPDATE_INTERVAL = 40;

    private final UUID raidId;
    private final Integer raidLevel;
    private final RaidConfig.RaidData config;
    private final ServerLevel level;
    private final Vec3 spawnCenter;
    private final long startTime;
    private final Set<UUID> henchmenUUIDs;
    private UUID bossUUID;
    private UUID mountUUID;
    private UUID targetPlayerUUID;
    private int spawnTimer;
    private int totalHenchmenSpawned;
    private boolean active;
    private boolean bossConfirmed;
    private int ticksSinceLoad;
    private int ticksSinceLastValidation;
    private int ticksSinceTargetUpdate;
    private int ticksSinceStart;
    private final ServerBossEvent bossBar;

    public ActiveRaid(Integer raidLevel, RaidConfig.RaidData config, ServerLevel level, Vec3 spawnCenter, long startTime) {
        this.raidId = UUID.randomUUID();
        this.raidLevel = raidLevel;
        this.config = config;
        this.level = level;
        this.spawnCenter = spawnCenter;
        this.startTime = startTime;
        this.henchmenUUIDs = new HashSet<>();
        this.spawnTimer = config.henchmen().spawnIntervalTicks();
        this.active = true;
        this.bossBar = createBossBar();
    }

    public static ActiveRaid restore(RaidSaveData.ActiveRaidData data, RaidConfig.RaidData config, ServerLevel level) {
        ActiveRaid raid = new ActiveRaid(data.raidLevel(), config, level, data.spawnCenter(), data.startTime());
        raid.bossUUID = data.bossUUID();
        raid.mountUUID = data.mountUUID();
        raid.targetPlayerUUID = data.targetPlayerUUID();
        raid.henchmenUUIDs.addAll(data.henchmenUUIDs());
        raid.spawnTimer = data.spawnTimer();
        raid.totalHenchmenSpawned = data.totalSpawned();
        raid.active = data.isActive();
        raid.bossConfirmed = false;
        raid.ticksSinceStart = (int) Math.min(level.getGameTime() - data.startTime(), Integer.MAX_VALUE);
        return raid;
    }

    private ServerBossEvent createBossBar() {
        String bossName = this.config.boss().customName();
        Component title;
        if (bossName != null && bossName.startsWith("translation:")) {
            title = Component.translatable(bossName.substring("translation:".length()));
        } else if (bossName != null) {
            title = Component.literal(bossName);
        } else {
            title = Component.literal("Raid Boss: " + this.config.raidId());
        }
        ServerBossEvent event = new ServerBossEvent(title, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        event.setProgress(1.0F);
        event.setVisible(true);
        return event;
    }

    public void tick() {
        if (!this.active) {
            return;
        }
        this.ticksSinceLoad++;
        this.ticksSinceLastValidation++;
        this.ticksSinceTargetUpdate++;
        this.ticksSinceStart++;

        int timeoutMinutes = Config.COMMON.raids.raidTimeoutMinutes.get();
        if (timeoutMinutes > 0 && this.ticksSinceStart >= timeoutMinutes * 60 * 20) {
            announceToNearbyPlayers(Component.translatable("raid.scguns.timeout").withStyle(ChatFormatting.RED), Math.max(256.0D, this.config.spawnConditions().searchRadius()));
            endRaid(false);
            return;
        }

        if (!validateBoss()) {
            return;
        }

        if (this.ticksSinceLastValidation >= BOSS_REVALIDATION_INTERVAL) {
            revalidateRaidState();
            this.ticksSinceLastValidation = 0;
        }
        if (this.ticksSinceTargetUpdate >= TARGET_UPDATE_INTERVAL) {
            updateMobTargets();
            this.ticksSinceTargetUpdate = 0;
        }

        LivingEntity boss = getBoss();
        if (boss != null && boss.isAlive()) {
            updateBossBar();
            if (this.level.getGameTime() % 20L == 0L) {
                updateBossBarPlayers();
            }
            if (this.bossConfirmed && this.spawnTimer > 0) {
                this.spawnTimer--;
            }
        } else {
            endRaid(this.bossConfirmed);
        }
    }

    private boolean validateBoss() {
        if (this.bossUUID == null) {
            endRaid(false);
            return false;
        }
        if (this.bossConfirmed) {
            return true;
        }
        LivingEntity boss = getBoss();
        if (boss != null && boss.isAlive()) {
            this.bossConfirmed = true;
            this.ticksSinceLoad = 0;
            return true;
        }
        if (this.ticksSinceLoad >= BOSS_VALIDATION_TICKS) {
            endRaid(false);
            return false;
        }
        if (this.ticksSinceLoad % 20 == 0) {
            ChunkPos chunkPos = new ChunkPos((int) this.spawnCenter.x >> 4, (int) this.spawnCenter.z >> 4);
            this.level.setChunkForced(chunkPos.x, chunkPos.z, true);
        }
        return false;
    }

    private void revalidateRaidState() {
        this.henchmenUUIDs.removeIf(uuid -> {
            Entity entity = this.level.getEntity(uuid);
            return !(entity instanceof LivingEntity livingEntity) || !livingEntity.isAlive();
        });
    }

    public void updateBossBarPlayers() {
        LivingEntity boss = getBoss();
        if (boss == null || !boss.isAlive()) {
            return;
        }
        List<ServerPlayer> nearbyPlayers = this.level.getPlayers(player -> player.isAlive() && !player.isSpectator() && boss.position().distanceTo(player.position()) <= 128.0D);
        Set<ServerPlayer> current = new HashSet<>(this.bossBar.getPlayers());
        for (ServerPlayer player : current) {
            if (!nearbyPlayers.contains(player)) {
                this.bossBar.removePlayer(player);
            }
        }
        for (ServerPlayer player : nearbyPlayers) {
            if (!current.contains(player)) {
                this.bossBar.addPlayer(player);
            }
        }
    }

    private void updateBossBar() {
        LivingEntity boss = getBoss();
        if (boss != null && boss.isAlive()) {
            this.bossBar.setProgress(Math.max(0.0F, Math.min(1.0F, boss.getHealth() / boss.getMaxHealth())));
        }
    }

    private void updateMobTargets() {
        ServerPlayer target = getTargetPlayer(this.level);
        if (target == null || !target.isAlive() || target.isSpectator() || target.isCreative()) {
            target = findNewTargetPlayer();
            if (target != null) {
                this.targetPlayerUUID = target.getUUID();
            }
        }
        if (target == null) {
            return;
        }
        setTarget(getBoss(), target);
        for (UUID henchmanUUID : this.henchmenUUIDs) {
            setTarget(this.level.getEntity(henchmanUUID), target);
        }
    }

    private void setTarget(@Nullable Entity entity, ServerPlayer target) {
        if (entity instanceof PathfinderMob mob && mob.getTarget() == null) {
            mob.setTarget(target);
            if (mob instanceof AbstractPiglin piglin) {
                try {
                    piglin.getBrain().eraseMemory(MemoryModuleType.AVOID_TARGET);
                    piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, target.getUUID());
                    piglin.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
                    piglin.setTarget(target);
                } catch (Exception ignored) {
                    piglin.setTarget(target);
                }
            }
        }
    }

    @Nullable
    private ServerPlayer findNewTargetPlayer() {
        ServerPlayer closest = null;
        double closestDist = Double.MAX_VALUE;
        for (ServerPlayer player : this.level.getPlayers(player -> !player.isSpectator() && !player.isCreative() && player.isAlive())) {
            double distance = player.position().distanceTo(this.spawnCenter);
            if (distance <= this.config.spawnConditions().searchRadius() && distance < closestDist) {
                closestDist = distance;
                closest = player;
            }
        }
        return closest;
    }

    public void announceToNearbyPlayers(Component message, double radius) {
        for (ServerPlayer player : this.level.getPlayers(player -> player.position().distanceTo(this.spawnCenter) <= radius)) {
            player.sendSystemMessage(message);
        }
    }

    public boolean shouldSpawnHenchmen() {
        return this.active && this.bossConfirmed && this.spawnTimer <= 0 && canSpawnMoreHenchmen();
    }

    public boolean canSpawnMoreHenchmen() {
        return this.henchmenUUIDs.size() < this.config.henchmen().maxAlive() && this.totalHenchmenSpawned < this.config.henchmen().maxTotal();
    }

    public void resetSpawnTimer() {
        this.spawnTimer = this.config.henchmen().spawnIntervalTicks();
    }

    public void addHenchman(UUID uuid) {
        this.henchmenUUIDs.add(uuid);
        this.totalHenchmenSpawned++;
    }

    public void removeHenchman(UUID uuid) {
        this.henchmenUUIDs.remove(uuid);
    }

    public void onBossDefeated() {
        endRaid(true);
    }

    public void endRaid(boolean victory) {
        if (!this.active) {
            return;
        }
        this.active = false;
        if (victory) {
            announceToNearbyPlayers(Component.translatable("raid.scguns.defeated").withStyle(ChatFormatting.GREEN), Math.max(256.0D, this.config.spawnConditions().searchRadius()));
        } else {
            announceToNearbyPlayers(Component.translatable("raid.scguns.failed").withStyle(ChatFormatting.RED), Math.max(256.0D, this.config.spawnConditions().searchRadius()));
            cleanupRaidMobs();
        }
        this.bossBar.setVisible(false);
        this.bossBar.removeAllPlayers();
    }

    private void cleanupRaidMobs() {
        for (UUID henchmanUUID : this.henchmenUUIDs) {
            Entity entity = this.level.getEntity(henchmanUUID);
            if (entity != null && entity.isAlive()) {
                entity.discard();
            }
        }
        Entity mount = getMount();
        if (mount != null && mount.isAlive()) {
            mount.discard();
        }
    }

    @Nullable
    public LivingEntity getBoss() {
        Entity entity = this.bossUUID != null ? this.level.getEntity(this.bossUUID) : null;
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Nullable
    public Entity getMount() {
        return this.mountUUID != null ? this.level.getEntity(this.mountUUID) : null;
    }

    @Nullable
    public ServerPlayer getTargetPlayer(ServerLevel level) {
        return this.targetPlayerUUID != null ? level.getServer().getPlayerList().getPlayer(this.targetPlayerUUID) : null;
    }

    public UUID getRaidId() { return raidId; }
    public Integer getRaidLevel() { return raidLevel; }
    public RaidConfig.RaidData getConfig() { return config; }
    public Vec3 getSpawnCenter() { return spawnCenter; }
    public long getStartTime() { return startTime; }
    public UUID getBossUUID() { return bossUUID; }
    public UUID getMountUUID() { return mountUUID; }
    public UUID getTargetPlayerUUID() { return targetPlayerUUID; }
    public Set<UUID> getHenchmenUUIDs() { return new HashSet<>(henchmenUUIDs); }
    public int getSpawnTimer() { return spawnTimer; }
    public int getTotalHenchmenSpawned() { return totalHenchmenSpawned; }
    public boolean isActive() { return active; }
    public boolean isBossConfirmed() { return bossConfirmed; }
    public ServerBossEvent getBossBar() { return bossBar; }
    public void setBossUUID(UUID bossUUID) { this.bossUUID = bossUUID; }
    public void setMountUUID(UUID mountUUID) { this.mountUUID = mountUUID; }
    public void setTargetPlayer(UUID targetPlayerUUID) { this.targetPlayerUUID = targetPlayerUUID; }
    public void setActive(boolean active) { this.active = active; }
    public void setBossConfirmed(boolean bossConfirmed) { this.bossConfirmed = bossConfirmed; }
}
