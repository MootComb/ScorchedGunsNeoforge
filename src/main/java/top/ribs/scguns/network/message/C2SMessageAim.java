package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.init.ModSyncedDataKeys;

public class C2SMessageAim
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageAim> STREAM_CODEC = StreamCodec.of(
            C2SMessageAim::encode,
            C2SMessageAim::decode
    );

    private boolean aiming;

    public C2SMessageAim() {}

    public C2SMessageAim(boolean aiming)
    {
        this.aiming = aiming;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageAim message)
    {
        buffer.writeBoolean(message.aiming);
    }

    private static C2SMessageAim decode(RegistryFriendlyByteBuf buffer)
    {
        return new C2SMessageAim(buffer.readBoolean());
    }

    public static void handle(C2SMessageAim message, MessageContext context) {
        context.execute(() ->
        {
            context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .filter(player -> !player.isSpectator())
                    .ifPresent(player -> {
                boolean currentlyReloading = ModSyncedDataKeys.RELOADING.getValue(player);
                ModSyncedDataKeys.AIMING.getValue(player);

                if(currentlyReloading) {
                    ModSyncedDataKeys.AIMING.setValue(player, false);
                    return;
                }
                ModSyncedDataKeys.AIMING.setValue(player, message.aiming);
            });
        });
        context.setHandled(true);
    }
}
