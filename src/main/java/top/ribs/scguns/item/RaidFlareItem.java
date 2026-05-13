package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class RaidFlareItem extends Item {
    private final String raidId;

    public RaidFlareItem(Properties properties, String raidId) {
        super(properties);
        this.raidId = raidId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("item.scguns.raid_flare.need_pistol").withStyle(ChatFormatting.RED), true);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("raid.scguns." + this.raidId + ".name").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.scguns.raid_flare.requires_pistol").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    public String getRaidId() {
        return this.raidId;
    }
}
