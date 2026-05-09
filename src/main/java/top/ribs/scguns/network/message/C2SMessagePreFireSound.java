package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Author: MrCrayfish
 */
public class C2SMessagePreFireSound
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessagePreFireSound> STREAM_CODEC = StreamCodec.of(
            C2SMessagePreFireSound::encode,
            C2SMessagePreFireSound::decode
    );


    public C2SMessagePreFireSound() {}

    public C2SMessagePreFireSound(Player player)
    {
    }
    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessagePreFireSound message)
    {

    }
    private static C2SMessagePreFireSound decode(RegistryFriendlyByteBuf buffer)
    {
        return new C2SMessagePreFireSound();
    }
    public static void handle(C2SMessagePreFireSound message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if(player != null)
            {
                ServerPlayHandler.handlePreFireSound(player);
            }
        });
        context.setHandled(true);
    }
}