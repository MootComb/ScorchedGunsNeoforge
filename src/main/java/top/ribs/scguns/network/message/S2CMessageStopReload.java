package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import top.ribs.scguns.client.network.ClientPlayHandler;

public class S2CMessageStopReload
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageStopReload> STREAM_CODEC = StreamCodec.of(
            S2CMessageStopReload::encode,
            S2CMessageStopReload::decode
    );

    public S2CMessageStopReload() {}
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageStopReload message) {
        // No data to encode
    }
    private static S2CMessageStopReload decode(RegistryFriendlyByteBuf buffer) {
        return new S2CMessageStopReload();
    }
    public static void handle(S2CMessageStopReload message, MessageContext context) {
        context.execute(() -> ClientPlayHandler.handleStopReload(message));
        context.setHandled(true);
    }
}