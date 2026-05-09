package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import top.ribs.scguns.client.network.ClientPlayHandler;

public class S2CMessageDualWieldShotCount
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageDualWieldShotCount> STREAM_CODEC = StreamCodec.of(
            S2CMessageDualWieldShotCount::encode,
            S2CMessageDualWieldShotCount::decode
    );

    private int entityId;
    private int shotCount;

    public S2CMessageDualWieldShotCount() {}

    public S2CMessageDualWieldShotCount(int entityId, int shotCount) {
        this.entityId = entityId;
        this.shotCount = shotCount;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageDualWieldShotCount message) {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.shotCount);
    }
    private static S2CMessageDualWieldShotCount decode(RegistryFriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        int shotCount = buffer.readInt();
        return new S2CMessageDualWieldShotCount(entityId, shotCount);
    }
    public static void handle(S2CMessageDualWieldShotCount message, MessageContext context) {
        context.execute(() -> ClientPlayHandler.handleMessageDualWieldShotCount(message));
        context.setHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public int getShotCount() {
        return this.shotCount;
    }
}