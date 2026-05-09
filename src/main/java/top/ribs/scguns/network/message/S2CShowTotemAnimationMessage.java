package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class S2CShowTotemAnimationMessage
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CShowTotemAnimationMessage> STREAM_CODEC = StreamCodec.of(
            S2CShowTotemAnimationMessage::encode,
            S2CShowTotemAnimationMessage::decode
    );

    private ItemStack itemStack;

    public S2CShowTotemAnimationMessage() {}

    public S2CShowTotemAnimationMessage(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CShowTotemAnimationMessage message) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, message.itemStack);
    }
    private static S2CShowTotemAnimationMessage decode(RegistryFriendlyByteBuf buffer) {
        S2CShowTotemAnimationMessage message = new S2CShowTotemAnimationMessage();
        message.itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        return message;
    }
    public static void handle(S2CShowTotemAnimationMessage message, MessageContext context) {
        context.execute(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.gameRenderer.displayItemActivation(message.itemStack);
            }
        });
        context.setHandled(true);
    }
}
