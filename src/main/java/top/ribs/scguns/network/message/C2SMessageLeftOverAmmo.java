package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessageLeftOverAmmo
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageLeftOverAmmo> STREAM_CODEC = StreamCodec.of(
            C2SMessageLeftOverAmmo::encode,
            C2SMessageLeftOverAmmo::decode
    );

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageLeftOverAmmo message) {}
    private static C2SMessageLeftOverAmmo decode(RegistryFriendlyByteBuf buffer)
    {
        return new C2SMessageLeftOverAmmo();
    }
    public static void handle(C2SMessageLeftOverAmmo message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.handleExtraAmmo(player);
            }
        });
        context.setHandled(true);
    }
}
