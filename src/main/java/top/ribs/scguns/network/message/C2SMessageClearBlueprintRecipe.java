package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import top.ribs.scguns.item.BlueprintItem;

public class C2SMessageClearBlueprintRecipe
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageClearBlueprintRecipe> STREAM_CODEC = StreamCodec.of(
            C2SMessageClearBlueprintRecipe::encode,
            C2SMessageClearBlueprintRecipe::decode
    );

    private InteractionHand hand;

    public C2SMessageClearBlueprintRecipe() {}

    public C2SMessageClearBlueprintRecipe(InteractionHand hand) {
        this.hand = hand;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageClearBlueprintRecipe message) {
        buffer.writeEnum(message.hand);
    }
    private static C2SMessageClearBlueprintRecipe decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageClearBlueprintRecipe(buffer.readEnum(InteractionHand.class));
    }
    public static void handle(C2SMessageClearBlueprintRecipe message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if (player != null) {
                ItemStack blueprint = player.getItemInHand(message.hand);

                if (blueprint.getItem() instanceof BlueprintItem) {
                    CompoundTag tag = blueprint.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    if (tag.contains("ActiveRecipe")) {
                        tag.remove("ActiveRecipe");

                        if (tag.isEmpty()) {
                            blueprint.remove(DataComponents.CUSTOM_DATA);
                        } else {
                            blueprint.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                        }
                    }
                }
            }
        });
        context.setHandled(true);
    }
}
