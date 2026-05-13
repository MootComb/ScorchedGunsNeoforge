package top.ribs.scguns.entity.raid;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RaidSaveData extends SavedData {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DATA_NAME = "scguns_raid_save_data";
    private final Map<UUID, ActiveRaidData> activeRaidData = new HashMap<>();
    private final Map<ResourceLocation, ScheduledRaidData> scheduledRaids = new HashMap<>();
    private final Map<ResourceLocation, Long> lastRaidDayByDimension = new HashMap<>();

    public static RaidSaveData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(RaidSaveData::new, RaidSaveData::load), DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag activeRaidsList = new ListTag();
        for (ActiveRaidData data : this.activeRaidData.values()) {
            activeRaidsList.add(data.save());
        }
        tag.put("ActiveRaids", activeRaidsList);

        ListTag scheduledRaidsList = new ListTag();
        for (ScheduledRaidData data : this.scheduledRaids.values()) {
            scheduledRaidsList.add(data.save());
        }
        tag.put("ScheduledRaids", scheduledRaidsList);

        CompoundTag lastRaidDays = new CompoundTag();
        for (Map.Entry<ResourceLocation, Long> entry : this.lastRaidDayByDimension.entrySet()) {
            lastRaidDays.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("LastRaidDays", lastRaidDays);
        return tag;
    }

    public static RaidSaveData load(CompoundTag tag, HolderLookup.Provider registries) {
        RaidSaveData data = new RaidSaveData();
        if (tag.contains("ActiveRaids")) {
            ListTag list = tag.getList("ActiveRaids", 10);
            for (int i = 0; i < list.size(); i++) {
                ActiveRaidData raidData = ActiveRaidData.load(list.getCompound(i));
                if (raidData != null) {
                    data.activeRaidData.put(raidData.raidId(), raidData);
                }
            }
        }
        if (tag.contains("ScheduledRaids")) {
            ListTag list = tag.getList("ScheduledRaids", 10);
            for (int i = 0; i < list.size(); i++) {
                ScheduledRaidData raidData = ScheduledRaidData.load(list.getCompound(i));
                if (raidData != null) {
                    data.scheduledRaids.put(raidData.dimension(), raidData);
                }
            }
        }
        if (tag.contains("LastRaidDays")) {
            CompoundTag lastRaidDays = tag.getCompound("LastRaidDays");
            for (String key : lastRaidDays.getAllKeys()) {
                try {
                    data.lastRaidDayByDimension.put(ResourceLocation.parse(key), lastRaidDays.getLong(key));
                } catch (Exception e) {
                    LOGGER.warn("Failed to load Scorched Guns last raid day for dimension {}", key);
                }
            }
        }
        return data;
    }

    public void saveActiveRaid(ActiveRaid raid) {
        this.activeRaidData.put(raid.getRaidId(), new ActiveRaidData(
                raid.getRaidId(),
                raid.getConfig().raidId(),
                raid.getRaidLevel(),
                raid.getSpawnCenter(),
                raid.getStartTime(),
                raid.getBossUUID(),
                raid.getMountUUID(),
                raid.getTargetPlayerUUID(),
                raid.getHenchmenUUIDs(),
                raid.getSpawnTimer(),
                raid.getTotalHenchmenSpawned(),
                raid.isActive(),
                raid.isBossConfirmed()
        ));
        this.setDirty();
    }

    public void removeActiveRaid(UUID raidId) {
        if (this.activeRaidData.remove(raidId) != null) {
            this.setDirty();
        }
    }

    public Collection<ActiveRaidData> getActiveRaidData() {
        return new ArrayList<>(this.activeRaidData.values());
    }

    public void scheduleRaid(ResourceLocation dimension, ServerPlayer player, String raidId, long day) {
        this.scheduledRaids.put(dimension, new ScheduledRaidData(dimension, player.getUUID(), raidId, day));
        this.setDirty();
    }

    @Nullable
    public ScheduledRaidData getScheduledRaid(ResourceLocation dimension) {
        return this.scheduledRaids.get(dimension);
    }

    public void removeScheduledRaid(ResourceLocation dimension) {
        if (this.scheduledRaids.remove(dimension) != null) {
            this.setDirty();
        }
    }

    public void setLastRaidDay(ResourceLocation dimension, long day) {
        this.lastRaidDayByDimension.put(dimension, day);
        this.setDirty();
    }

    public long getLastRaidDay(ResourceLocation dimension) {
        return this.lastRaidDayByDimension.getOrDefault(dimension, -1000L);
    }

    public boolean canScheduleRaid(ResourceLocation dimension, long currentDay, int minDaysBetween) {
        return currentDay - getLastRaidDay(dimension) >= minDaysBetween;
    }

    public void cleanupInvalidRaids(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveRaidData>> iterator = this.activeRaidData.entrySet().iterator();
        boolean dirty = false;
        while (iterator.hasNext()) {
            ActiveRaidData data = iterator.next().getValue();
            if (!data.isActive() || data.bossConfirmed() && data.bossUUID() != null && level.getEntity(data.bossUUID()) == null) {
                iterator.remove();
                dirty = true;
            }
        }
        if (dirty) {
            this.setDirty();
        }
    }

    public record ActiveRaidData(UUID raidId, String configRaidId, @Nullable Integer raidLevel, Vec3 spawnCenter,
                                 long startTime, @Nullable UUID bossUUID, @Nullable UUID mountUUID,
                                 @Nullable UUID targetPlayerUUID, Set<UUID> henchmenUUIDs, int spawnTimer,
                                 int totalSpawned, boolean isActive, boolean bossConfirmed) {
        public ActiveRaidData {
            henchmenUUIDs = new HashSet<>(henchmenUUIDs);
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("RaidId", this.raidId);
            tag.putString("ConfigRaidId", this.configRaidId);
            if (this.raidLevel != null) {
                tag.putInt("RaidLevel", this.raidLevel);
            }
            tag.putDouble("SpawnX", this.spawnCenter.x);
            tag.putDouble("SpawnY", this.spawnCenter.y);
            tag.putDouble("SpawnZ", this.spawnCenter.z);
            tag.putLong("StartTime", this.startTime);
            if (this.bossUUID != null) tag.putUUID("BossUUID", this.bossUUID);
            if (this.mountUUID != null) tag.putUUID("MountUUID", this.mountUUID);
            if (this.targetPlayerUUID != null) tag.putUUID("TargetPlayerUUID", this.targetPlayerUUID);
            ListTag henchmen = new ListTag();
            for (UUID uuid : this.henchmenUUIDs) {
                CompoundTag henchmanTag = new CompoundTag();
                henchmanTag.putUUID("UUID", uuid);
                henchmen.add(henchmanTag);
            }
            tag.put("Henchmen", henchmen);
            tag.putInt("SpawnTimer", this.spawnTimer);
            tag.putInt("TotalSpawned", this.totalSpawned);
            tag.putBoolean("IsActive", this.isActive);
            tag.putBoolean("BossConfirmed", this.bossConfirmed);
            return tag;
        }

        @Nullable
        public static ActiveRaidData load(CompoundTag tag) {
            try {
                UUID raidId = tag.getUUID("RaidId");
                String configRaidId = tag.getString("ConfigRaidId");
                Integer raidLevel = tag.contains("RaidLevel") ? tag.getInt("RaidLevel") : null;
                Vec3 spawnCenter = new Vec3(tag.getDouble("SpawnX"), tag.getDouble("SpawnY"), tag.getDouble("SpawnZ"));
                UUID bossUUID = tag.hasUUID("BossUUID") ? tag.getUUID("BossUUID") : null;
                UUID mountUUID = tag.hasUUID("MountUUID") ? tag.getUUID("MountUUID") : null;
                UUID targetPlayerUUID = tag.hasUUID("TargetPlayerUUID") ? tag.getUUID("TargetPlayerUUID") : null;
                Set<UUID> henchmen = new HashSet<>();
                ListTag henchmenList = tag.getList("Henchmen", 10);
                for (int i = 0; i < henchmenList.size(); i++) {
                    CompoundTag henchmanTag = henchmenList.getCompound(i);
                    if (henchmanTag.hasUUID("UUID")) {
                        henchmen.add(henchmanTag.getUUID("UUID"));
                    }
                }
                return new ActiveRaidData(raidId, configRaidId, raidLevel, spawnCenter, tag.getLong("StartTime"), bossUUID, mountUUID,
                        targetPlayerUUID, henchmen, tag.getInt("SpawnTimer"), tag.getInt("TotalSpawned"),
                        tag.getBoolean("IsActive"), tag.getBoolean("BossConfirmed"));
            } catch (Exception e) {
                LOGGER.warn("Failed to load Scorched Guns active raid data: {}", e.getMessage());
                return null;
            }
        }
    }

    public record ScheduledRaidData(ResourceLocation dimension, UUID targetPlayerUUID, String raidId, long scheduledDay) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Dimension", this.dimension.toString());
            tag.putUUID("PlayerUUID", this.targetPlayerUUID);
            tag.putString("RaidId", this.raidId);
            tag.putLong("ScheduledDay", this.scheduledDay);
            return tag;
        }

        @Nullable
        public static ScheduledRaidData load(CompoundTag tag) {
            try {
                return new ScheduledRaidData(ResourceLocation.parse(tag.getString("Dimension")), tag.getUUID("PlayerUUID"),
                        tag.getString("RaidId"), tag.getLong("ScheduledDay"));
            } catch (Exception e) {
                LOGGER.warn("Failed to load Scorched Guns scheduled raid data: {}", e.getMessage());
                return null;
            }
        }
    }
}
