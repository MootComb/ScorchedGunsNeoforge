package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.init.ModSyncedDataKeys;

/**
 * Author: MrCrayfish
 */
public class C2SMessageShooting
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageShooting> STREAM_CODEC = StreamCodec.of(
            C2SMessageShooting::encode,
            C2SMessageShooting::decode
    );

    private boolean shooting;

    public C2SMessageShooting() {}

    public C2SMessageShooting(boolean shooting)
    {
        this.shooting = shooting;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageShooting message)
    {
        buffer.writeBoolean(message.shooting);
    }
    private static C2SMessageShooting decode(RegistryFriendlyByteBuf buffer)
    {
        return new C2SMessageShooting(buffer.readBoolean());
    }
    public static void handle(C2SMessageShooting message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if(player != null)
            {
                ModSyncedDataKeys.SHOOTING.setValue(player, message.shooting);
            }
        });
        context.setHandled(true);
    }
}