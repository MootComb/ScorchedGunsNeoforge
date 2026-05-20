package top.ribs.scguns.entity.raid;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.config.GunnerMobSpawner;
import top.ribs.scguns.config.RaidConfig;
import top.ribs.scguns.entity.ai.GunAttackGoal;
import top.ribs.scguns.entity.player.PlayerGunProgression;
import top.ribs.scguns.item.GunItem;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RaidManager {
    private static final ResourceLocation BOSS_HEALTH_MODIFIER_ID = Reference.id("raid_boss_health");
    private static final ResourceLocation MOUNT_HEALTH_MODIFIER_ID = Reference.id("raid_mount_health");
    private static final ResourceLocation HENCHMAN_HEALTH_MODIFIER_ID = Reference.id("raid_henchman_health");
    private static final long NIGHT_START = 13000L;
    private static final long RAID_SPAWN_TIME = 18000L;
    private static final long ACTIVE_RAID_SAVE_INTERVAL = 1200L;
    private static final Map<ResourceLocation, RaidManager> INSTANCES = new HashMap<>();

    private final Map<UUID, ActiveRaid> activeRaids = new HashMap<>();
    private UUID currentActiveRaidId;
    private boolean needsRestore = true;

    public static RaidManager get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level.dimension().location(), id -> new RaidManager());
    }

    public boolean hasActiveRaid() {
        ActiveRaid raid = this.currentActiveRaidId != null ? this.activeRaids.get(this.currentActiveRaidId) : null;
        return raid != null && raid.isActive();
    }

    public static boolean hasActiveRaidInDimension(ServerLevel level) {
        return get(level).hasActiveRaid();
    }

    @Nullable
    public ActiveRaid getCurrentActiveRaid() {
        return this.currentActiveRaidId != null ? this.activeRaids.get(this.currentActiveRaidId) : null;
    }

    public static void surrenderRaid(ServerLevel level) {
        RaidManager manager = get(level);
        ActiveRaid raid = manager.getCurrentActiveRaid();
        if (raid == null || !raid.isActive()) {
            return;
        }
        LivingEntity boss = raid.getBoss();
        if (boss != null && boss.isAlive()) {
            boss.discard();
        }
        Entity mount = raid.getMount();
        if (mount != null && mount.isAlive()) {
            mount.discard();
        }
        for (UUID henchmanUUID : raid.getHenchmenUUIDs()) {
            Entity henchman = level.getEntity(henchmanUUID);
            if (henchman != null && henchman.isAlive()) {
                henchman.discard();
            }
        }
        raid.announceToNearbyPlayers(Component.translatable("raid.scguns.surrendered").withStyle(ChatFormatting.YELLOW), 64.0D);
        raid.getBossBar().setVisible(false);
        raid.getBossBar().removeAllPlayers();
        raid.setActive(false);
        manager.currentActiveRaidId = null;
        manager.activeRaids.remove(raid.getRaidId());
        RaidSaveData.get(level).removeActiveRaid(raid.getRaidId());
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel == serverLevel.getServer().overworld()) {
            RaidManager manager = get(serverLevel);
            if (manager.needsRestore) {
                manager.restoreRaidsFromSave(serverLevel);
                manager.needsRestore = false;
            }
        }
    }

    private void restoreRaidsFromSave(ServerLevel level) {
        RaidSaveData saveData = RaidSaveData.get(level);
        saveData.cleanupInvalidRaids(level);
        for (RaidSaveData.ActiveRaidData data : saveData.getActiveRaidData()) {
            RaidConfig.RaidData config = RaidConfig.getRaidById(data.configRaidId());
            if (config != null) {
                ActiveRaid raid = ActiveRaid.restore(data, config, level);
                this.activeRaids.put(raid.getRaidId(), raid);
                if (this.currentActiveRaidId == null && raid.isActive()) {
                    this.currentActiveRaidId = raid.getRaidId();
                }
                raid.updateBossBarPlayers();
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            get(serverLevel).tick(serverLevel);
        }
    }

    public void tick(ServerLevel level) {
        tickActiveRaids(level);
        if (Config.COMMON.raids.raidsEnabled.get() && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            checkForNightlyRaidSpawn(level);
            if (Level.OVERWORLD.equals(level.dimension()) && this.hasActiveRaid() && level.getGameTime() % ACTIVE_RAID_SAVE_INTERVAL == 0L) {
                saveActiveRaids(level);
            }
        }
    }

    private void tickActiveRaids(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveRaid>> iterator = this.activeRaids.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveRaid> entry = iterator.next();
            ActiveRaid raid = entry.getValue();
            if (!raid.isActive()) {
                if (raid.getRaidId().equals(this.currentActiveRaidId)) {
                    this.currentActiveRaidId = null;
                }
                RaidSaveData.get(level).removeActiveRaid(raid.getRaidId());
                iterator.remove();
                continue;
            }
            raid.tick();
            if (raid.shouldSpawnHenchmen()) {
                spawnHenchmen(raid, level);
                raid.resetSpawnTimer();
            }
        }
    }

    private void saveActiveRaids(ServerLevel level) {
        RaidSaveData saveData = RaidSaveData.get(level);
        for (ActiveRaid raid : this.activeRaids.values()) {
            if (raid.isActive()) {
                saveData.saveActiveRaid(raid);
            }
        }
    }

    private void checkForNightlyRaidSpawn(ServerLevel level) {
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }
        ResourceLocation dimension = level.dimension().location();
        long dayTime = level.getDayTime() % 24000L;
        long currentDay = level.getDayTime() / 24000L;
        RaidSaveData saveData = RaidSaveData.get(level);
        if (dayTime >= NIGHT_START && dayTime < NIGHT_START + 20L) {
            if (this.hasActiveRaid()) {
                return;
            }
            RaidSaveData.ScheduledRaidData scheduled = saveData.getScheduledRaid(dimension);
            if (scheduled != null && scheduled.scheduledDay() < currentDay) {
                saveData.removeScheduledRaid(dimension);
                scheduled = null;
            }
            if (scheduled == null && dayTime == NIGHT_START && saveData.canScheduleRaid(dimension, currentDay, Config.COMMON.raids.minDaysBetweenRaids.get())) {
                if (level.random.nextFloat() < Config.COMMON.raids.nightlyRaidChance.get()) {
                    scheduleRaidForTonight(level, dimension, currentDay, saveData);
                }
            }
        }
        if (dayTime >= RAID_SPAWN_TIME && dayTime < RAID_SPAWN_TIME + 20L && !this.hasActiveRaid()) {
            RaidSaveData.ScheduledRaidData scheduled = saveData.getScheduledRaid(dimension);
            if (scheduled == null || scheduled.scheduledDay() != currentDay) {
                return;
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(scheduled.targetPlayerUUID());
            if (player == null || player.isSpectator() || player.isCreative()) {
                saveData.removeScheduledRaid(dimension);
                return;
            }
            RaidConfig.RaidData config = RaidConfig.getRaidById(scheduled.raidId());
            Vec3 spawnPos = config != null ? findRaidSpawnLocation(level, player.position()) : null;
            if (config != null && spawnPos != null) {
                startRaid(config, level, spawnPos);
                saveData.setLastRaidDay(dimension, currentDay);
            }
            saveData.removeScheduledRaid(dimension);
        }
    }

    private void scheduleRaidForTonight(ServerLevel level, ResourceLocation dimension, long currentDay, RaidSaveData saveData) {
        var players = level.getPlayers(player -> !player.isSpectator() && !player.isCreative());
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer target = players.get(level.random.nextInt(players.size()));
        int raidLevel = PlayerGunProgression.get(target).getCurrentRaidLevel();
        if (raidLevel <= 0) {
            return;
        }
        RaidConfig.RaidData selectedRaid = selectRaidForLevel(raidLevel, level.random);
        if (selectedRaid != null) {
            saveData.scheduleRaid(dimension, target, selectedRaid.raidId(), currentDay);
            target.sendSystemMessage(Component.translatable("raid.scguns.warning").withStyle(ChatFormatting.GOLD));
        }
    }

    @Nullable
    private RaidConfig.RaidData selectRaidForLevel(int playerRaidLevel, RandomSource random) {
        var availableRaids = RaidConfig.getRaidsForLevel(playerRaidLevel);
        if (availableRaids.isEmpty()) {
            return null;
        }
        var highestLevelRaids = RaidConfig.getRaidsAtLevel(playerRaidLevel);
        if (!highestLevelRaids.isEmpty() && random.nextFloat() < 0.6F) {
            return highestLevelRaids.get(random.nextInt(highestLevelRaids.size()));
        }
        return availableRaids.get(random.nextInt(availableRaids.size()));
    }

    public void startRaidFromPlayer(RaidConfig.RaidData config, ServerLevel level, ServerPlayer player) {
        if (this.hasActiveRaid() || !canStartInDimension(config, level)) {
            return;
        }
        int nearbyPlayers = level.getPlayers(p -> !p.isSpectator() && p.position().distanceTo(player.position()) <= config.spawnConditions().searchRadius()).size();
        if (nearbyPlayers < config.spawnConditions().minPlayersNearby()) {
            player.displayClientMessage(Component.translatable("raid.scguns.not_enough_players").withStyle(ChatFormatting.RED), true);
            return;
        }
        Vec3 spawnPos = findRaidSpawnLocation(level, player.position());
        if (spawnPos != null) {
            startRaid(config, level, spawnPos);
        }
    }

    private boolean canStartInDimension(RaidConfig.RaidData config, ServerLevel level) {
        return config.spawnConditions().validDimensions().contains(level.dimension().location());
    }

    @Nullable
    private Vec3 findRaidSpawnLocation(ServerLevel level, Vec3 center) {
        RandomSource random = level.getRandom();
        int playerY = (int) center.y;
        boolean underground = playerY < 50;
        for (int attempt = 0; attempt < 15; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = 25.0D + random.nextDouble() * 15.0D;
            BlockPos pos = BlockPos.containing(center.x + Math.cos(angle) * distance, playerY, center.z + Math.sin(angle) * distance);
            BlockPos groundPos = underground ? findNearestValidCaveSpawn(level, pos, playerY) : level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
            if (groundPos != null && validSpawn(level, groundPos)) {
                return Vec3.atBottomCenterOf(groundPos);
            }
        }
        return null;
    }

    @Nullable
    private BlockPos findNearestValidCaveSpawn(ServerLevel level, BlockPos center, int playerY) {
        for (int yOffset = -5; yOffset <= 5; yOffset++) {
            BlockPos checkPos = new BlockPos(center.getX(), playerY + yOffset, center.getZ());
            if (validSpawn(level, checkPos)) {
                return checkPos;
            }
        }
        return null;
    }

    private boolean validSpawn(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid()
                && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.above(2)).isAir();
    }

    public void startRaid(RaidConfig.RaidData config, ServerLevel level, Vec3 spawnPos) {
        if (this.hasActiveRaid()) {
            return;
        }
        ServerPlayer targetPlayer = findNearestPlayer(level, spawnPos);
        ActiveRaid raid = new ActiveRaid(config.raidLevel(), config, level, spawnPos, level.getGameTime());
        if (targetPlayer != null) {
            raid.setTargetPlayer(targetPlayer.getUUID());
        }
        Mob boss = spawnBoss(raid, level, spawnPos);
        if (boss == null) {
            return;
        }
        raid.setBossUUID(boss.getUUID());
        raid.setBossConfirmed(true);
        this.activeRaids.put(raid.getRaidId(), raid);
        this.currentActiveRaidId = raid.getRaidId();
        Component announcement = message(config.spawnConditions().announcementMessage());
        raid.announceToNearbyPlayers(announcement, config.spawnConditions().searchRadius());
        spawnHenchmen(raid, level);
        raid.resetSpawnTimer();
        RaidSaveData.get(level).saveActiveRaid(raid);
    }

    @Nullable
    private ServerPlayer findNearestPlayer(ServerLevel level, Vec3 pos) {
        ServerPlayer nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && !player.isCreative()) {
                double dist = player.position().distanceTo(pos);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = player;
                }
            }
        }
        return nearest;
    }

    @Nullable
    private Mob spawnBoss(ActiveRaid raid, ServerLevel level, Vec3 spawnPos) {
        RaidConfig.BossData bossData = raid.getConfig().boss();
        Entity entity = bossData.entityType().create(level);
        if (!(entity instanceof Mob boss)) {
            return null;
        }
        boss.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, level.random.nextFloat() * 360.0F, 0.0F);
        if (bossData.customName() != null) {
            boss.setCustomName(message(bossData.customName()));
            boss.setCustomNameVisible(true);
        }
        applyHealthConfig(boss, bossData.healthConfig(), BOSS_HEALTH_MODIFIER_ID);
        applyEffects(boss, bossData.effects());
        equipWeapon(boss, bossData.weapon(), bossData.aiDifficulty());
        equipArmor(boss, bossData.armor(), false);
        boss.addTag("RaidBoss");
        boss.addTag("RaidMember_" + raid.getRaidId());
        boss.addTag("MobGunner");
        if (boss instanceof PathfinderMob pathfinderBoss && boss.getMainHandItem().getItem() instanceof GunItem && !GunnerMobSpawner.hasGunAttackGoal(pathfinderBoss)) {
            pathfinderBoss.goalSelector.addGoal(2, new GunAttackGoal<>(pathfinderBoss, bossData.aiDifficulty()));
        }
        ServerPlayer target = findNearestPlayer(level, spawnPos);
        if (target != null && boss instanceof PathfinderMob pathfinder) {
            pathfinder.setTarget(target);
        }
        level.addFreshEntity(boss);

        RaidConfig.MountData mountData = bossData.mount();
        if (mountData != null) {
            Mob mount = spawnMount(raid, mountData, level, spawnPos);
            if (mount != null) {
                boss.startRiding(mount, true);
                raid.setMountUUID(mount.getUUID());
            }
        }
        return boss;
    }

    @Nullable
    private Mob spawnMount(ActiveRaid raid, RaidConfig.MountData mountData, ServerLevel level, Vec3 spawnPos) {
        Entity entity = mountData.entityType().create(level);
        if (!(entity instanceof Mob mount)) {
            return null;
        }
        mount.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, level.random.nextFloat() * 360.0F, 0.0F);
        applyHealthConfig(mount, mountData.healthConfig(), MOUNT_HEALTH_MODIFIER_ID);
        applyEffects(mount, mountData.effects());
        equipArmor(mount, mountData.armor(), false);
        mount.addTag("RaidMount");
        mount.addTag("RaidMember_" + raid.getRaidId());
        if (!mountData.mountDropsLoot()) {
            mount.addTag("NoLootDrop");
        }
        level.addFreshEntity(mount);
        return mount;
    }

    private void spawnHenchmen(ActiveRaid raid, ServerLevel level) {
        RaidConfig.HenchmenData henchmenData = raid.getConfig().henchmen();
        LivingEntity boss = raid.getBoss();
        if (boss == null || !boss.isAlive()) {
            return;
        }
        for (int i = 0; i < henchmenData.spawnAttemptsPerWave() && raid.canSpawnMoreHenchmen(); i++) {
            RaidConfig.HenchmanType type = henchmenData.selectRandomType(level.random);
            Vec3 spawnPos = type != null ? findHenchmanSpawnPos(level, boss.position(), henchmenData.spawnRadius()) : null;
            if (type != null && spawnPos != null) {
                Mob henchman = spawnHenchman(raid, type, level, spawnPos);
                if (henchman != null) {
                    raid.addHenchman(henchman.getUUID());
                }
            }
        }
    }

    @Nullable
    private Vec3 findHenchmanSpawnPos(ServerLevel level, Vec3 center, int radius) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double distance = level.random.nextDouble() * radius;
            BlockPos pos = BlockPos.containing(center.x + Math.cos(angle) * distance, center.y, center.z + Math.sin(angle) * distance);
            BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
            if (validSpawn(level, groundPos)) {
                return Vec3.atBottomCenterOf(groundPos);
            }
        }
        return null;
    }

    @Nullable
    private Mob spawnHenchman(ActiveRaid raid, RaidConfig.HenchmanType type, ServerLevel level, Vec3 spawnPos) {
        Entity entity = type.entityType().create(level);
        if (!(entity instanceof Mob henchman)) {
            return null;
        }
        henchman.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, level.random.nextFloat() * 360.0F, 0.0F);
        applyHealthConfig(henchman, type.healthConfig(), HENCHMAN_HEALTH_MODIFIER_ID);
        applyEffects(henchman, type.effects());
        henchman.addTag("RaidHenchman");
        henchman.addTag("RaidMember_" + raid.getRaidId());
        henchman.addTag("MobGunner");

        if (!type.weapons().isEmpty()) {
            Item weapon = type.weapons().get(level.random.nextInt(type.weapons().size()));
            equipWeapon(henchman, new RaidConfig.WeaponEntry(weapon, 0.05F, null), type.aiDifficulty());
        }
        equipArmor(henchman, type.armor(), true);
        if (henchman instanceof PathfinderMob pathfinder) {
            if (henchman.getMainHandItem().getItem() instanceof GunItem && !GunnerMobSpawner.hasGunAttackGoal(pathfinder)) {
                pathfinder.goalSelector.addGoal(2, new GunAttackGoal<>(pathfinder, type.aiDifficulty()));
            }
            ServerPlayer target = raid.getTargetPlayer(level);
            if (target != null) {
                pathfinder.setTarget(target);
            }
        }
        level.addFreshEntity(henchman);
        return henchman;
    }

    private void equipWeapon(Mob mob, @Nullable RaidConfig.WeaponEntry weapon, int aiDifficulty) {
        if (weapon == null) {
            return;
        }
        ItemStack stack = new ItemStack(weapon.item());
        if (weapon.nbt() != null) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.merge(weapon.nbt());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        markMobGun(stack, mob);
        mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
        mob.setDropChance(EquipmentSlot.MAINHAND, Math.max(0.0F, Math.min(1.0F, weapon.dropChance())));
        mob.getPersistentData().putBoolean("scguns:GunnerMob", true);
        mob.getPersistentData().putInt("scguns:GunnerAiDifficulty", Math.max(1, aiDifficulty));
    }

    private void equipArmor(Mob mob, Collection<RaidConfig.ArmorEntry> armor, boolean chance) {
        for (RaidConfig.ArmorEntry entry : armor) {
            if (chance && mob.getRandom().nextFloat() > entry.dropChance()) {
                continue;
            }
            EquipmentSlot slot = armorSlot(entry.slot());
            if (slot == null) {
                continue;
            }
            ItemStack armorStack = new ItemStack(entry.item());
            if (entry.nbt() != null) {
                CompoundTag tag = armorStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                tag.merge(entry.nbt());
                armorStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
            mob.setItemSlot(slot, armorStack);
            mob.setDropChance(slot, chance ? 0.05F : Math.max(0.0F, Math.min(1.0F, entry.dropChance())));
        }
    }

    @Nullable
    private EquipmentSlot armorSlot(String slot) {
        return switch (slot) {
            case "head" -> EquipmentSlot.HEAD;
            case "chest" -> EquipmentSlot.CHEST;
            case "legs" -> EquipmentSlot.LEGS;
            case "feet" -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    private void markMobGun(ItemStack stack, Mob mob) {
        if (!(stack.getItem() instanceof GunItem gunItem)) {
            return;
        }
        Gun gun = gunItem.getModifiedGun(stack);
        int maxAmmo = Math.max(1, gun.getReloads().getMaxAmmo());
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("AmmoCount", mob.getRandom().nextInt(maxAmmo));
        tag.putBoolean("scguns:MobGun", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private void applyHealthConfig(Mob mob, RaidConfig.HealthConfig healthConfig, ResourceLocation modifierId) {
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        if (healthConfig.useMultiplier() && healthConfig.healthMultiplier() != null) {
            health.addOrReplacePermanentModifier(new AttributeModifier(modifierId, healthConfig.healthMultiplier() - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else if (healthConfig.fixedHealth() != null) {
            health.addOrReplacePermanentModifier(new AttributeModifier(modifierId, healthConfig.fixedHealth() - health.getBaseValue(), AttributeModifier.Operation.ADD_VALUE));
        }
        mob.setHealth(mob.getMaxHealth());
    }

    private void applyEffects(Mob mob, Collection<RaidConfig.EffectEntry> effects) {
        for (RaidConfig.EffectEntry effect : effects) {
            mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect.effect(), effect.duration(), effect.amplifier(), effect.ambient(), effect.visible()));
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        RaidManager manager = get(level);
        for (ActiveRaid raid : manager.activeRaids.values()) {
            if (mob.getUUID().equals(raid.getBossUUID())) {
                ResourceLocation loot = raid.getConfig().boss().specialLootTable();
                if (loot != null) {
                    manager.dropSpecialLoot(mob, loot, level, event.getSource());
                }
                raid.onBossDefeated();
                return;
            }
            if (raid.getHenchmenUUIDs().contains(mob.getUUID())) {
                raid.removeHenchman(mob.getUUID());
                return;
            }
        }
    }

    private void dropSpecialLoot(Mob boss, ResourceLocation lootTableLocation, ServerLevel level, DamageSource source) {
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation));
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, boss)
                .withParameter(LootContextParams.ORIGIN, boss.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source);
        if (source.getEntity() instanceof ServerPlayer player) {
            builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(player.getLuck());
        }
        for (ItemStack stack : lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY))) {
            level.addFreshEntity(new ItemEntity(level, boss.getX(), boss.getY(), boss.getZ(), stack));
        }
    }

    private Component message(String value) {
        if (value.startsWith("translation:")) {
            return Component.translatable(value.substring("translation:".length()));
        }
        return Component.literal(value);
    }

    public Collection<ActiveRaid> getActiveRaids() {
        return this.activeRaids.values();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        RaidManager manager = get(overworld);
        manager.saveActiveRaids(overworld);
        overworld.getDataStorage().save();
        INSTANCES.clear();
    }
}
