package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class C2SMessageChargeSync
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageChargeSync> STREAM_CODEC = StreamCodec.of(
            C2SMessageChargeSync::encode,
            C2SMessageChargeSync::decode
    );

    private float chargeProgress;

    public C2SMessageChargeSync() {}

    public C2SMessageChargeSync(float chargeProgress) {
        this.chargeProgress = chargeProgress;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageChargeSync message) {
        buffer.writeFloat(message.chargeProgress);
    }
    private static C2SMessageChargeSync decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageChargeSync(buffer.readFloat());
    }
    public static void handle(C2SMessageChargeSync message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if(player != null) {
                player.getPersistentData().putFloat("ChargeProgress", message.chargeProgress);
            }
        });
        context.setHandled(true);
    }

    public float getChargeProgress() {
        return this.chargeProgress;
    }
}