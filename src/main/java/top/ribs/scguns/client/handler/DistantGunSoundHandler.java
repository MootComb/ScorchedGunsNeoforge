package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import top.ribs.scguns.ScorchedGuns;

import java.util.ArrayDeque;
import java.util.Queue;

public final class DistantGunSoundHandler {
    private static final DistantGunSoundHandler INSTANCE = new DistantGunSoundHandler();
    private static final double PROJECTED_SOUND_DISTANCE = 45.0D;
    private static final int MAX_QUEUED_SOUNDS = 128;
    private final Queue<PendingSound> pendingSounds = new ArrayDeque<>();

    private DistantGunSoundHandler() {
    }

    public static DistantGunSoundHandler get() {
        return INSTANCE;
    }

    public void queue(ResourceLocation id, SoundSource category, Vec3 sourcePos, float volume, float pitch, int delayTicks) {
        if (volume <= 0.0F || this.pendingSounds.size() >= MAX_QUEUED_SOUNDS) {
            ScorchedGuns.LOGGER.debug("[DistantGunshot] Client dropped queued sound id={} volume={} queueSize={}", id, volume, this.pendingSounds.size());
            return;
        }
        ScorchedGuns.LOGGER.debug("[DistantGunshot] Client queued id={} category={} source=({}, {}, {}) volume={} pitch={} delayTicks={} queueSize={}",
                id, category, sourcePos.x, sourcePos.y, sourcePos.z, volume, pitch, delayTicks, this.pendingSounds.size());
        this.pendingSounds.add(new PendingSound(id, category, sourcePos, volume, pitch, Math.max(0, delayTicks)));
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            this.pendingSounds.clear();
            return;
        }

        int soundsThisTick = this.pendingSounds.size();
        while (soundsThisTick-- > 0) {
            PendingSound sound = this.pendingSounds.poll();
            if (sound == null) {
                return;
            }

            if (sound.delayTicks > 0) {
                this.pendingSounds.add(sound.tick());
                continue;
            }

            this.play(mc, sound);
        }
    }

    private void play(Minecraft mc, PendingSound sound) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        Vec3 soundPos = this.projectSoundPosition(player, sound.sourcePos);
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(sound.id).orElse(null);
        if (soundEvent == null) {
            ScorchedGuns.LOGGER.warn("[DistantGunshot] Missing client SoundEvent for {}, falling back to direct sound id playback", sound.id);
            mc.getSoundManager().play(new SimpleSoundInstance(
                    sound.id,
                    sound.category,
                    sound.volume,
                    sound.pitch,
                    mc.level.getRandom(),
                    false,
                    0,
                    net.minecraft.client.resources.sounds.SoundInstance.Attenuation.LINEAR,
                    soundPos.x,
                    soundPos.y,
                    soundPos.z,
                    false
            ));
            return;
        }

        ScorchedGuns.LOGGER.debug("[DistantGunshot] Client playing id={} category={} projected=({}, {}, {}) source=({}, {}, {}) distance={} volume={} pitch={}",
                sound.id,
                sound.category,
                soundPos.x,
                soundPos.y,
                soundPos.z,
                sound.sourcePos.x,
                sound.sourcePos.y,
                sound.sourcePos.z,
                player.position().distanceTo(sound.sourcePos),
                sound.volume,
                sound.pitch);
        mc.getSoundManager().play(new SimpleSoundInstance(
                soundEvent,
                sound.category,
                sound.volume,
                sound.pitch,
                RandomSource.create(),
                soundPos.x,
                soundPos.y,
                soundPos.z
        ));
    }

    private Vec3 projectSoundPosition(LocalPlayer player, Vec3 sourcePos) {
        Vec3 listenerPos = player.getEyePosition();
        Vec3 direction = sourcePos.subtract(listenerPos);
        if (direction.lengthSqr() < 1.0E-6D) {
            direction = player.getLookAngle();
        }
        return listenerPos.add(direction.normalize().scale(PROJECTED_SOUND_DISTANCE));
    }

    private record PendingSound(ResourceLocation id, SoundSource category, Vec3 sourcePos, float volume, float pitch, int delayTicks) {
        private PendingSound tick() {
            return new PendingSound(this.id, this.category, this.sourcePos, this.volume, this.pitch, this.delayTicks - 1);
        }
    }
}
