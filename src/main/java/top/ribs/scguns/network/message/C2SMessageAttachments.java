package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageAttachments
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageAttachments> STREAM_CODEC = StreamCodec.of(
            C2SMessageAttachments::encode,
            C2SMessageAttachments::decode
    );

    public C2SMessageAttachments() {}
    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageAttachments message) {}
    private static C2SMessageAttachments decode(RegistryFriendlyByteBuf buffer)
    {
        return new C2SMessageAttachments();
    }
    public static void handle(C2SMessageAttachments message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if(player != null)
            {
                ServerPlayHandler.handleAttachments(player);
            }
        });
        context.setHandled(true);
    }
}