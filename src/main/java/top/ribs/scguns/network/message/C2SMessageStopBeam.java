package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.client.handler.BeamHandler;
import top.ribs.scguns.common.network.ServerPlayHandler;

public class C2SMessageStopBeam
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageStopBeam> STREAM_CODEC = StreamCodec.of(
            C2SMessageStopBeam::encode,
            C2SMessageStopBeam::decode
    );

    public C2SMessageStopBeam() {}
    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageStopBeam message) {
        // No data needs to be encoded for this message
    }
    private static C2SMessageStopBeam decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageStopBeam();
    }
    public static void handle(C2SMessageStopBeam message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if (player != null) {
                ServerPlayHandler.handleStopBeam(player);
            }
        });
        context.setHandled(true);
    }
}