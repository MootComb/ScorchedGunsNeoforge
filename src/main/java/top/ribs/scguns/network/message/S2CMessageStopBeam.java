package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import top.ribs.scguns.client.network.ClientPlayHandler;

import java.util.UUID;

public class S2CMessageStopBeam
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageStopBeam> STREAM_CODEC = StreamCodec.of(
            S2CMessageStopBeam::encode,
            S2CMessageStopBeam::decode
    );

    private UUID playerId;

    public S2CMessageStopBeam() {}

    public S2CMessageStopBeam(UUID playerId) {
        this.playerId = playerId;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageStopBeam message) {
        buffer.writeUUID(message.playerId);
    }
    private static S2CMessageStopBeam decode(RegistryFriendlyByteBuf buffer) {
        return new S2CMessageStopBeam(buffer.readUUID());
    }
    public static void handle(S2CMessageStopBeam message, MessageContext context) {
        context.execute(() -> ClientPlayHandler.handleStopBeam(message));
        context.setHandled(true);
    }

    public UUID getPlayerId() {
        return playerId;
    }
}