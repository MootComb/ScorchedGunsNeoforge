package top.ribs.scguns.network.message;


import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

public class C2SMessageReloadByproduct
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageReloadByproduct> STREAM_CODEC = StreamCodec.of(
            C2SMessageReloadByproduct::encode,
            C2SMessageReloadByproduct::decode
    );

    private static final float BYPRODUCT_CHANCE = 0.75f;

    public C2SMessageReloadByproduct() {}
    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageReloadByproduct message) {
        // No data needed to send
    }
    private static C2SMessageReloadByproduct decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageReloadByproduct();
    }
    public static void handle(C2SMessageReloadByproduct message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if (player != null && !player.isSpectator()) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof GunItem gunItem) {
                    Gun gun = gunItem.getModifiedGun(stack);
                    Item byproduct = gun.getReloads().getReloadByproduct();
                    if (byproduct != null && player.level().random.nextFloat() < BYPRODUCT_CHANCE) {
                        ItemStack byproductStack = new ItemStack(byproduct);
                        boolean added = player.getInventory().add(byproductStack);

                        if (!added) {
                            ItemEntity itemEntity = new ItemEntity(
                                    player.level(),
                                    player.getX(),
                                    player.getY(),
                                    player.getZ(),
                                    byproductStack
                            );

                            itemEntity.setDeltaMovement(
                                    player.level().random.nextDouble() * 0.2 - 0.1,
                                    0.2,
                                    player.level().random.nextDouble() * 0.2 - 0.1
                            );
                            player.level().addFreshEntity(itemEntity);
                        }
                    }
                }
            }
        });
        context.setHandled(true);
    }
}
