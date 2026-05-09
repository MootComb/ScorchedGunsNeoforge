package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import top.ribs.scguns.client.network.ClientPlayHandler;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class S2CMessageBlood
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageBlood> STREAM_CODEC = StreamCodec.of(
            S2CMessageBlood::encode,
            S2CMessageBlood::decode
    );

    private double x;
    private double y;
    private double z;
    private EntityType<?> entityType;

    public S2CMessageBlood() {}

    public S2CMessageBlood(double x, double y, double z, EntityType<?> entityType)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.entityType = entityType;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageBlood message)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(message.entityType)));
    }
    private static S2CMessageBlood decode(RegistryFriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        ResourceLocation entityTypeLocation = buffer.readResourceLocation();
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityTypeLocation);
        return new S2CMessageBlood(x, y, z, entityType);
    }
    public static void handle(S2CMessageBlood message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleMessageBlood(message));
        context.setHandled(true);
    }

    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getZ() { return this.z; }
    public EntityType<?> getEntityType() { return this.entityType; }
}
