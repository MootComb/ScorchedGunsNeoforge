package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import top.ribs.scguns.client.network.ClientPlayHandler;

/**
 * Author: MrCrayfish
 */
public class S2CMessageRemoveProjectile
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageRemoveProjectile> STREAM_CODEC = StreamCodec.of(
            S2CMessageRemoveProjectile::encode,
            S2CMessageRemoveProjectile::decode
    );

    private int entityId;

    public S2CMessageRemoveProjectile() {}

    public S2CMessageRemoveProjectile(int entityId)
    {
        this.entityId = entityId;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageRemoveProjectile message)
    {
        buffer.writeInt(message.entityId);
    }
    private static S2CMessageRemoveProjectile decode(RegistryFriendlyByteBuf buffer)
    {
        return new S2CMessageRemoveProjectile(buffer.readInt());
    }
    public static void handle(S2CMessageRemoveProjectile message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleRemoveProjectile(message));
        context.setHandled(true);
    }


    public int getEntityId()
    {
        return this.entityId;
    }
}