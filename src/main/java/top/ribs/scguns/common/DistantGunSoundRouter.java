package top.ribs.scguns.common;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageDistantGunSound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class DistantGunSoundRouter {
    private static final ResourceLocation LIGHT_DISTANT_SHOT = Reference.id("item.distant_gunshot.light");
    private static final ResourceLocation HEAVY_DISTANT_SHOT = Reference.id("item.distant_gunshot.heavy");
    private static final ResourceLocation EDGE_DISTANT_SHOT = Reference.id("item.distant_gunshot.edge");
    private static final double HEAVY_RADIUS_THRESHOLD = 128.0D;
    private static final double EDGE_DISTANCE_FACTOR = 0.78D;
    private static final float MIN_AUDIBLE_VOLUME = 0.04F;
    private static final long CLEANUP_INTERVAL_TICKS = 400L;
    private static final long THROTTLE_TTL_TICKS = 1200L;
    private static final Map<ThrottleKey, Long> LAST_SENT_TICK = new HashMap<>();

    private DistantGunSoundRouter() {
    }

    public static void send(ServerLevel level, Vec3 sourcePos, SoundSource category, float volume, float pitch, int sourceEntityId, boolean silenced, double nearRadius) {
        if (!Config.SERVER.enableDistantGunshots.get()) {
            ScorchedGuns.LOGGER.debug("[DistantGunshot] Server skipped source={} category={} because distant gunshots are disabled", sourceEntityId, category);
            return;
        }
        if (silenced) {
            ScorchedGuns.LOGGER.debug("[DistantGunshot] Server skipped source={} category={} because gunshot is silenced", sourceEntityId, category);
            return;
        }

        double maxDistance = Config.SERVER.distantGunShotMaxDistance.get();
        if (maxDistance <= nearRadius || maxDistance <= 0.0D) {
            ScorchedGuns.LOGGER.debug("[DistantGunshot] Server skipped source={} category={} because maxDistance={} nearRadius={}", sourceEntityId, category, maxDistance, nearRadius);
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime % CLEANUP_INTERVAL_TICKS == 0L) {
            cleanup(gameTime);
        }

        double nearDistanceSqr = nearRadius * nearRadius;
        double maxDistanceSqr = maxDistance * maxDistance;
        long fallbackSourceKey = sourceEntityId >= 0 ? 0L : BlockPos.containing(sourcePos).asLong();
        int throttleTicks = Config.SERVER.distantGunShotThrottleTicks.get();
        int checkedPlayers = 0;
        int skippedNear = 0;
        int skippedFar = 0;
        int skippedThrottle = 0;
        int skippedQuiet = 0;
        int sent = 0;

        for (ServerPlayer player : level.players()) {
            checkedPlayers++;
            double distanceSqr = player.distanceToSqr(sourcePos.x, sourcePos.y, sourcePos.z);
            if (distanceSqr <= nearDistanceSqr) {
                skippedNear++;
                continue;
            }
            if (distanceSqr > maxDistanceSqr) {
                skippedFar++;
                continue;
            }

            ThrottleKey throttleKey = new ThrottleKey(player.getUUID(), sourceEntityId, fallbackSourceKey);
            if (throttleTicks > 0) {
                Long lastSent = LAST_SENT_TICK.get(throttleKey);
                if (lastSent != null && gameTime - lastSent < throttleTicks) {
                    skippedThrottle++;
                    continue;
                }
            }

            double distance = Math.sqrt(distanceSqr);
            float distantVolume = calculateVolume(volume, nearRadius, maxDistance, distance);
            if (distantVolume < MIN_AUDIBLE_VOLUME) {
                skippedQuiet++;
                continue;
            }

            ResourceLocation soundId = selectSound(nearRadius, maxDistance, distance, volume);
            LAST_SENT_TICK.put(throttleKey, gameTime);
            int delayTicks = calculateDelayTicks(distance);
            S2CMessageDistantGunSound message = new S2CMessageDistantGunSound(
                    soundId,
                    category,
                    sourcePos.x,
                    sourcePos.y,
                    sourcePos.z,
                    distantVolume,
                    pitch,
                    delayTicks
            );
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, message);
            sent++;
        }
        ScorchedGuns.LOGGER.debug(
                "[DistantGunshot] Server route source={} category={} sourcePos=({}, {}, {}) baseVolume={} nearRadius={} maxDistance={} checked={} sent={} skippedNear={} skippedFar={} skippedThrottle={} skippedQuiet={}",
                sourceEntityId,
                category,
                sourcePos.x,
                sourcePos.y,
                sourcePos.z,
                volume,
                nearRadius,
                maxDistance,
                checkedPlayers,
                sent,
                skippedNear,
                skippedFar,
                skippedThrottle,
                skippedQuiet
        );
    }

    private static ResourceLocation selectSound(double nearRadius, double maxDistance, double distance, float volume) {
        double edgeStart = nearRadius + (maxDistance - nearRadius) * EDGE_DISTANCE_FACTOR;
        if (distance >= edgeStart) {
            return EDGE_DISTANT_SHOT;
        }
        return nearRadius >= HEAVY_RADIUS_THRESHOLD || volume >= 1.2F ? HEAVY_DISTANT_SHOT : LIGHT_DISTANT_SHOT;
    }

    private static float calculateVolume(float baseVolume, double nearRadius, double maxDistance, double distance) {
        double distanceFactor = 1.0D - Mth.clamp((distance - nearRadius) / (maxDistance - nearRadius), 0.0D, 1.0D);
        double easedFactor = Math.pow(distanceFactor, 1.15D);
        return (float) Mth.clamp(baseVolume * Config.SERVER.distantGunShotVolumeMultiplier.get() * easedFactor, 0.0D, 4.0D);
    }

    private static int calculateDelayTicks(double distance) {
        double blocksPerTick = Math.max(1.0D, Config.SERVER.distantGunShotSpeedOfSoundBlocksPerSecond.get()) / 20.0D;
        int maxDelay = Config.SERVER.distantGunShotMaxDelayTicks.get();
        return Mth.clamp((int) Math.round(distance / blocksPerTick), 0, maxDelay);
    }

    private static void cleanup(long gameTime) {
        Iterator<Map.Entry<ThrottleKey, Long>> iterator = LAST_SENT_TICK.entrySet().iterator();
        while (iterator.hasNext()) {
            if (gameTime - iterator.next().getValue() > THROTTLE_TTL_TICKS) {
                iterator.remove();
            }
        }
    }

    private record ThrottleKey(UUID listenerId, int sourceEntityId, long fallbackSourceKey) {
    }
}
