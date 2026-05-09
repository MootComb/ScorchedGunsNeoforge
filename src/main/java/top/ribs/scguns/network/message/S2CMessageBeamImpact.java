package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.client.handler.BeamHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CMessageBeamImpact
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageBeamImpact> STREAM_CODEC = StreamCodec.of(
            S2CMessageBeamImpact::encode,
            S2CMessageBeamImpact::decode
    );


    private Vec3 hitPosition;
    private UUID playerUUID;

    // Default constructor required for decoding
    public S2CMessageBeamImpact() {}

    public S2CMessageBeamImpact(Vec3 hitPosition, UUID playerUUID) {
        this.hitPosition = hitPosition;
        this.playerUUID = playerUUID;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageBeamImpact message) {
        buffer.writeDouble(message.hitPosition.x);
        buffer.writeDouble(message.hitPosition.y);
        buffer.writeDouble(message.hitPosition.z);
        buffer.writeUUID(message.playerUUID);
    }
    private static S2CMessageBeamImpact decode(RegistryFriendlyByteBuf buffer) {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        Vec3 hitPosition = new Vec3(x, y, z);
        UUID playerUUID = buffer.readUUID();
        return new S2CMessageBeamImpact(hitPosition, playerUUID);
    }
    public static void handle(S2CMessageBeamImpact message, MessageContext context) {
        context.execute(() -> {
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null) {
                Player player = world.getPlayerByUUID(message.playerUUID);
                if (player != null) {
                    BeamHandler.spawnBeamImpactParticles(world, message.hitPosition, player);
                }
            }
        });
        context.setHandled(true);
    }
}
