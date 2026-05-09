package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.cache.HotBarrelCache;

public class S2CMessageHotBarrelSync
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageHotBarrelSync> STREAM_CODEC = StreamCodec.of(
            S2CMessageHotBarrelSync::encode,
            S2CMessageHotBarrelSync::decode
    );

    private int hotBarrelLevel;
    private String weaponDescriptionId;

    public S2CMessageHotBarrelSync() {}

    public S2CMessageHotBarrelSync(int hotBarrelLevel, String weaponDescriptionId) {
        this.hotBarrelLevel = hotBarrelLevel;
        this.weaponDescriptionId = weaponDescriptionId;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageHotBarrelSync message) {
        buffer.writeInt(message.hotBarrelLevel);
        buffer.writeUtf(message.weaponDescriptionId);
    }
    private static S2CMessageHotBarrelSync decode(RegistryFriendlyByteBuf buffer) {
        return new S2CMessageHotBarrelSync(buffer.readInt(), buffer.readUtf());
    }
    public static void handle(S2CMessageHotBarrelSync message, MessageContext context) {
        context.execute(() -> {
            Player player = context.getPlayer().orElse(null);
            if (player != null) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem().getDescriptionId().equals(message.weaponDescriptionId)) {
                    HotBarrelCache.setHotBarrelLevel(player, heldItem, message.hotBarrelLevel);
                }
            }
        });
        context.setHandled(true);
    }
}