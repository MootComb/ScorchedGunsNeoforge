package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.handler.ClientMeleeAttackHandler;
import top.ribs.scguns.item.GunItem;

import java.util.function.Supplier;

public class S2CMessageMeleeAttack
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageMeleeAttack> STREAM_CODEC = StreamCodec.of(
            S2CMessageMeleeAttack::encode,
            S2CMessageMeleeAttack::decode
    );

    private ItemStack heldItem;

    public S2CMessageMeleeAttack() {}

    public S2CMessageMeleeAttack(ItemStack heldItem) {
        this.heldItem = heldItem;
    }

    public S2CMessageMeleeAttack(RegistryFriendlyByteBuf buf) {
        this.heldItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    }
    private static void encode(RegistryFriendlyByteBuf buf, S2CMessageMeleeAttack message) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, message.heldItem);
    }
    private static S2CMessageMeleeAttack decode(RegistryFriendlyByteBuf buf) {
        return new S2CMessageMeleeAttack(buf);
    }
    public static void handle(S2CMessageMeleeAttack message, MessageContext context) {
        context.execute(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                if (message.heldItem.getItem() instanceof GunItem gunItem) {
                    ClientMeleeAttackHandler.startMeleeAnimation(gunItem, message.heldItem);

                }
            }
        });
        context.setHandled(true);
    }
}
