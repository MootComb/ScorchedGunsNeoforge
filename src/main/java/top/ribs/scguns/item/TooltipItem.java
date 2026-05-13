package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TooltipItem extends Item {
    private final String tooltipKey;
    private final String secondaryTooltipKey;

    public TooltipItem(Properties properties, String tooltipKey) {
        this(properties, tooltipKey, null);
    }

    public TooltipItem(Properties properties, String tooltipKey, String secondaryTooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
        this.secondaryTooltipKey = secondaryTooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (this.tooltipKey != null && !this.tooltipKey.isEmpty()) {
            tooltip.add(Component.translatable(this.tooltipKey).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        if (this.secondaryTooltipKey != null && !this.secondaryTooltipKey.isEmpty()) {
            tooltip.add(Component.translatable(this.secondaryTooltipKey).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
