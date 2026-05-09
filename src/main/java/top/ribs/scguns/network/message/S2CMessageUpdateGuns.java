package top.ribs.scguns.network.message;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import top.ribs.scguns.client.network.ClientPlayHandler;
import top.ribs.scguns.common.CustomGun;
import top.ribs.scguns.common.CustomGunLoader;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.NetworkGunManager;

/**
 * Author: MrCrayfish
 */
public class S2CMessageUpdateGuns
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateGuns> STREAM_CODEC = StreamCodec.of(
            S2CMessageUpdateGuns::encode,
            S2CMessageUpdateGuns::decode
    );

    private ImmutableMap<ResourceLocation, Gun> registeredGuns;
    private ImmutableMap<ResourceLocation, CustomGun> customGuns;

    public S2CMessageUpdateGuns() {}
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageUpdateGuns message)
    {
        Validate.notNull(NetworkGunManager.get());
        Validate.notNull(CustomGunLoader.get());
        NetworkGunManager.get().writeRegisteredGuns(buffer);
        CustomGunLoader.get().writeCustomGuns(buffer);
    }
    private static S2CMessageUpdateGuns decode(RegistryFriendlyByteBuf buffer)
    {
        S2CMessageUpdateGuns message = new S2CMessageUpdateGuns();
        message.registeredGuns = NetworkGunManager.readRegisteredGuns(buffer);
        message.customGuns = CustomGunLoader.readCustomGuns(buffer);
        return message;
    }
    public static void handle(S2CMessageUpdateGuns message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleUpdateGuns(message));
        context.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, Gun> getRegisteredGuns()
    {
        return this.registeredGuns;
    }

    public ImmutableMap<ResourceLocation, CustomGun> getCustomGuns()
    {
        return this.customGuns;
    }
}