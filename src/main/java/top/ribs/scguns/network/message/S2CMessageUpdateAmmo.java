package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import top.ribs.scguns.network.ClientMessageHandler;

public class S2CMessageUpdateAmmo
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateAmmo> STREAM_CODEC = StreamCodec.of(
            S2CMessageUpdateAmmo::encode,
            S2CMessageUpdateAmmo::decode
    );

    private int ammoCount;

    public S2CMessageUpdateAmmo() {}

    public S2CMessageUpdateAmmo(int ammoCount) {
        this.ammoCount = ammoCount;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageUpdateAmmo message) {
        buffer.writeInt(message.ammoCount);
    }
    private static S2CMessageUpdateAmmo decode(RegistryFriendlyByteBuf buffer) {
        S2CMessageUpdateAmmo message = new S2CMessageUpdateAmmo();
        message.ammoCount = buffer.readInt();
        return message;
    }
    public static void handle(S2CMessageUpdateAmmo message, MessageContext context) {
        context.execute(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                if (ClientMessageHandler.handleUpdateAmmo(message.ammoCount)) {
                    context.setHandled(true);
                }
            }
        });
    }
}
