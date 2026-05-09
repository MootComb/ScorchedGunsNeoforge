package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageUnload> STREAM_CODEC = StreamCodec.of(
            C2SMessageUnload::encode,
            C2SMessageUnload::decode
    );

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageUnload message) {}
    private static C2SMessageUnload decode(RegistryFriendlyByteBuf buffer)
    {
        return new C2SMessageUnload();
    }
    public static void handle(C2SMessageUnload message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.handleUnload(player);
            }
        });
        context.setHandled(true);
    }
}
