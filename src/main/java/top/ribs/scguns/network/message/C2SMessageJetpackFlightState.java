package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.exosuit.ExoSuitFlightHandler;

public class C2SMessageJetpackFlightState {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageJetpackFlightState> STREAM_CODEC = StreamCodec.of(
            C2SMessageJetpackFlightState::encode,
            C2SMessageJetpackFlightState::decode
    );

    private final boolean active;

    public C2SMessageJetpackFlightState() {
        this(false);
    }

    public C2SMessageJetpackFlightState(boolean active) {
        this.active = active;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageJetpackFlightState message) {
        buffer.writeBoolean(message.active);
    }

    private static C2SMessageJetpackFlightState decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageJetpackFlightState(buffer.readBoolean());
    }

    public static void handle(C2SMessageJetpackFlightState message, MessageContext context) {
        context.execute(() -> context.getPlayer()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .filter(player -> !player.isSpectator())
                .ifPresent(player -> ExoSuitFlightHandler.setJetpackFlightActive(player, message.active)));
        context.setHandled(true);
    }
}
