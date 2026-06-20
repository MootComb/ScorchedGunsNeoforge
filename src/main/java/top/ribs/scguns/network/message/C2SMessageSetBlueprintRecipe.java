package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.BlueprintItem;
import top.ribs.scguns.util.BlueprintRecipeData;

public class C2SMessageSetBlueprintRecipe
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageSetBlueprintRecipe> STREAM_CODEC = StreamCodec.of(
            C2SMessageSetBlueprintRecipe::encode,
            C2SMessageSetBlueprintRecipe::decode
    );

    private InteractionHand hand;
    private String recipeId;

    public C2SMessageSetBlueprintRecipe() {}

    public C2SMessageSetBlueprintRecipe(InteractionHand hand, String recipeId) {
        this.hand = hand;
        this.recipeId = recipeId;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageSetBlueprintRecipe message) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.recipeId);
    }
    private static C2SMessageSetBlueprintRecipe decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageSetBlueprintRecipe(buffer.readEnum(InteractionHand.class), buffer.readUtf());
    }
    public static void handle(C2SMessageSetBlueprintRecipe message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if (player != null) {
                ItemStack blueprint = player.getItemInHand(message.hand);

                if (blueprint.getItem() instanceof BlueprintItem) {
                    BlueprintRecipeData.saveActiveRecipe(blueprint, message.recipeId);
                }
            }
        });
        context.setHandled(true);
    }
}
