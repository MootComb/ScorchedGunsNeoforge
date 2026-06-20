package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.client.network.ClientPlayHandler;

public class S2CMessageDistantGunSound {
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageDistantGunSound> STREAM_CODEC = StreamCodec.of(
            S2CMessageDistantGunSound::encode,
            S2CMessageDistantGunSound::decode
    );

    private final ResourceLocation id;
    private final SoundSource category;
    private final double x;
    private final double y;
    private final double z;
    private final float volume;
    private final float pitch;
    private final int delayTicks;

    public S2CMessageDistantGunSound(ResourceLocation id, SoundSource category, double x, double y, double z, float volume, float pitch, int delayTicks) {
        this.id = id;
        this.category = category;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
        this.delayTicks = delayTicks;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageDistantGunSound message) {
        buffer.writeResourceLocation(message.id);
        buffer.writeEnum(message.category);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat(message.volume);
        buffer.writeFloat(message.pitch);
        buffer.writeVarInt(message.delayTicks);
    }

    private static S2CMessageDistantGunSound decode(RegistryFriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        SoundSource category = buffer.readEnum(SoundSource.class);
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        int delayTicks = buffer.readVarInt();
        return new S2CMessageDistantGunSound(id, category, x, y, z, volume, pitch, delayTicks);
    }

    public static void handle(S2CMessageDistantGunSound message, MessageContext context) {
        context.execute(() -> ClientPlayHandler.handleMessageDistantGunSound(message));
        context.setHandled(true);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public SoundSource getCategory() {
        return this.category;
    }

    public Vec3 getSourcePos() {
        return new Vec3(this.x, this.y, this.z);
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getDelayTicks() {
        return this.delayTicks;
    }
}
