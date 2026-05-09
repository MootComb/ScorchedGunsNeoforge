package top.ribs.scguns.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import top.ribs.scguns.item.attachment.IMagazine;
import top.ribs.scguns.item.attachment.impl.Magazine;

public class MagazineItem extends AttachmentItem implements IMagazine, IColored
{
    private final Magazine magazine;
    private final boolean colored;

    public MagazineItem(Magazine magazine, Properties properties)
    {
        super(properties);
        this.magazine = magazine;
        this.colored = true;
    }

    public MagazineItem(Magazine magazine, Properties properties, boolean colored)
    {
        super(properties);
        this.magazine = magazine;
        this.colored = colored;
    }

    @Override
    public Magazine getProperties()
    {
        return this.magazine;
    }

    @Override
    public boolean canColor(ItemStack stack)
    {
        return this.colored;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment)
    {
        return enchantment.is(Enchantments.BINDING_CURSE) || super.supportsEnchantment(stack, enchantment);
    }
}
