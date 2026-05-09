package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.network.ClientPlayHandler;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;

public class S2CMessageReload
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageReload> STREAM_CODEC = StreamCodec.of(
            S2CMessageReload::encode,
            S2CMessageReload::decode
    );

    private boolean reloading;

    public S2CMessageReload() {}

    public S2CMessageReload(boolean reloading) {
        this.reloading = reloading;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageReload message) {
        buffer.writeBoolean(message.reloading);
    }
    private static S2CMessageReload decode(RegistryFriendlyByteBuf buffer) {
        return new S2CMessageReload(buffer.readBoolean());
    }
    public static void handle(S2CMessageReload message, MessageContext context) {
        context.execute(() -> {
            ClientPlayHandler.handleReloadState(message.reloading);
        });
        context.setHandled(true);
    }

    public boolean isReloading() {
        return this.reloading;
    }
}