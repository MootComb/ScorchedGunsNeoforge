package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModParticleTypes;

import java.util.function.Supplier;

public class S2CMessageMuzzleFlash
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageMuzzleFlash> STREAM_CODEC = StreamCodec.of(
            S2CMessageMuzzleFlash::encode,
            S2CMessageMuzzleFlash::decode
    );


    private Vec3 position;
    private float yaw;
    private float pitch;

    public S2CMessageMuzzleFlash() {}

    public S2CMessageMuzzleFlash(Vec3 position, float yaw, float pitch) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageMuzzleFlash message) {
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
        buffer.writeFloat(message.yaw);
        buffer.writeFloat(message.pitch);
    }
    private static S2CMessageMuzzleFlash decode(RegistryFriendlyByteBuf buffer) {
        return new S2CMessageMuzzleFlash(
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }
    public static void handle(S2CMessageMuzzleFlash message, MessageContext context) {
        context.execute(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                level.addParticle(
                        ModParticleTypes.TURRET_MUZZLE_FLASH.get(),
                        message.position.x,
                        message.position.y,
                        message.position.z,
                        0, 0, 0
                );
            }
        });
        context.setHandled(true);
    }
}
