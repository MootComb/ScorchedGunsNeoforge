package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import top.ribs.scguns.client.network.ClientPlayHandler;

public class S2CMessageStunGrenade
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageStunGrenade> STREAM_CODEC = StreamCodec.of(
            S2CMessageStunGrenade::encode,
            S2CMessageStunGrenade::decode
    );

    private double x, y, z;

    public S2CMessageStunGrenade() {}

    public S2CMessageStunGrenade(double x, double y, double z)
    {
        this.z = z;
        this.y = y;
        this.x = x;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageStunGrenade message)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }
    private static S2CMessageStunGrenade decode(RegistryFriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        return new S2CMessageStunGrenade(x, y, z);
    }
    public static void handle(S2CMessageStunGrenade message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleExplosionStunGrenade(message));
        context.setHandled(true);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }
}