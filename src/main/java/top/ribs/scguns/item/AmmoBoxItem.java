package top.ribs.scguns.item;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AmmoBoxItem extends Item {
    public static final String TAG_ITEMS = "Items";
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public AmmoBoxItem(Item.Properties properties) {
        super(properties);
    }


    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (stack.getCount() == 1 && action == ClickAction.SECONDARY) {
            ItemStack itemStackInSlot = slot.getItem();
            if (itemStackInSlot.isEmpty()) {
                this.playRemoveOneSound(player);
                removeOne(stack).ifPresent(removedStack -> add(stack, slot.safeInsert(removedStack)));
            } else if (itemStackInSlot.is(ItemTags.create(getAmmoTag()))) {
                int maxInsertCount = getMaxItemCount(stack) - getTotalItemCount(stack);
                int itemsToInsert = Math.min(itemStackInSlot.getCount(), maxInsertCount);
                int insertedItems = add(stack, slot.safeTake(itemStackInSlot.getCount(), itemsToInsert, player));
                if (insertedItems > 0) {
                    this.playInsertSound(player);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess) {
        if (stack.getCount() != 1) {
            return false;
        } else if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (otherStack.isEmpty()) {
                removeOne(stack).ifPresent(removedStack -> {
                    this.playRemoveOneSound(player);
                    slotAccess.set(removedStack);
                });
            } else if (otherStack.is(ItemTags.create(getAmmoTag()))) {
                int maxInsertCount = getMaxItemCount(stack) - getTotalItemCount(stack);
                int itemsToInsert = Math.min(otherStack.getCount(), maxInsertCount);
                int insertedItems = add(stack, otherStack.copyWithCount(itemsToInsert));
                if (insertedItems > 0) {
                    this.playInsertSound(player);
                    otherStack.shrink(insertedItems);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return getTotalItemCount(stack) > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.min(1 + 12 * getTotalItemCount(stack) / getMaxItemCount(stack), 13);
    }

    protected abstract ResourceLocation getAmmoTag();

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    public static int add(ItemStack pouchStack, ItemStack insertedStack) {
        if (!insertedStack.isEmpty() && insertedStack.is(ItemTags.create(((AmmoBoxItem)pouchStack.getItem()).getAmmoTag()))) {
            CompoundTag compoundTag = getCustomData(pouchStack);
            if (!compoundTag.contains(TAG_ITEMS)) {
                compoundTag.put(TAG_ITEMS, new ListTag());
            }

            int maxItemCount = getMaxItemCount(pouchStack);
            int itemsToInsert = Math.min(insertedStack.getCount(), maxItemCount - getTotalItemCount(pouchStack));

            if (itemsToInsert == 0) {
                return 0;
            }

            ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack existingStack = ItemStack.parseOptional(Gun.builtInRegistryProvider(), itemTag);
                if (ItemStack.isSameItemSameComponents(existingStack, insertedStack)) {
                    int remainingSpace = Math.min(existingStack.getMaxStackSize() - existingStack.getCount(), itemsToInsert);
                    existingStack.grow(remainingSpace);
                    itemsToInsert -= remainingSpace;
                    listTag.set(i, saveStack(existingStack));
                    if (itemsToInsert <= 0) {
                        break;
                    }
                }
            }

            while (itemsToInsert > 0) {
                int countToInsert = Math.min(insertedStack.getMaxStackSize(), itemsToInsert);
                ItemStack newItemStack = insertedStack.copyWithCount(countToInsert);
                listTag.add(saveStack(newItemStack));
                itemsToInsert -= countToInsert;
            }

            compoundTag.put(TAG_ITEMS, listTag);
            setCustomData(pouchStack, compoundTag);
            return insertedStack.getCount() - itemsToInsert;
        } else {
            return 0;
        }
    }

    public static int getTotalItemCount(ItemStack stack) {
        return getContents(stack).mapToInt(ItemStack::getCount).sum();
    }

    public static int getAmmoCount(ItemStack stack, Item ammoItem) {
        if (!(stack.getItem() instanceof AmmoBoxItem)) {
            return 0;
        }
        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
            TagKey<Item> ammoTag = ItemTags.create(((CreativeAmmoBoxItem) stack.getItem()).getAmmoTag());
            return ammoItem.builtInRegistryHolder().is(ammoTag) ? Integer.MAX_VALUE : 0;
        }
        return getContents(stack)
                .filter(contained -> !contained.isEmpty() && contained.getItem() == ammoItem)
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public static boolean containsAmmo(ItemStack stack, Item ammoItem) {
        return getAmmoCount(stack, ammoItem) > 0;
    }

    public static int shrinkAmmo(ItemStack stack, Item ammoItem, int amount, HolderLookup.Provider provider) {
        if (amount <= 0 || !(stack.getItem() instanceof AmmoBoxItem)) {
            return 0;
        }
        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
            return containsAmmo(stack, ammoItem) ? amount : 0;
        }

        int remaining = amount;
        List<ItemStack> contents = getContents(stack).collect(Collectors.toCollection(ArrayList::new));
        for (ItemStack contained : contents) {
            if (remaining <= 0) {
                break;
            }
            if (!contained.isEmpty() && contained.getItem() == ammoItem) {
                int consumed = Math.min(remaining, contained.getCount());
                contained.shrink(consumed);
                remaining -= consumed;
            }
        }

        int consumed = amount - remaining;
        if (consumed > 0) {
            setContents(stack, contents, provider);
        }
        return consumed;
    }

    public static void setContents(ItemStack stack, List<ItemStack> contents, HolderLookup.Provider provider) {
        if (!(stack.getItem() instanceof AmmoBoxItem) || stack.getItem() instanceof CreativeAmmoBoxItem) {
            return;
        }

        CompoundTag compoundTag = getCustomData(stack);
        ListTag listTag = new ListTag();
        for (ItemStack contained : contents) {
            if (!contained.isEmpty()) {
                listTag.add(saveStack(contained, provider));
            }
        }

        if (listTag.isEmpty()) {
            compoundTag.remove(TAG_ITEMS);
        } else {
            compoundTag.put(TAG_ITEMS, listTag);
        }
        setCustomData(stack, compoundTag);
    }

    private static Optional<ItemStack> removeOne(ItemStack stack) {
        CompoundTag compoundTag = getCustomData(stack);
        if (!compoundTag.contains(TAG_ITEMS)) {
            return Optional.empty();
        } else {
            ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
            if (listTag.isEmpty()) {
                return Optional.empty();
            } else {
                CompoundTag itemTag = listTag.getCompound(0);
                ItemStack itemStack = ItemStack.parseOptional(Gun.builtInRegistryProvider(), itemTag);
                listTag.remove(0);
                if (listTag.isEmpty()) {
                    compoundTag.remove(TAG_ITEMS);
                } else {
                    compoundTag.put(TAG_ITEMS, listTag);
                }
                setCustomData(stack, compoundTag);
                return Optional.of(itemStack);
            }
        }
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        getContents(stack).forEach(nonNullList::add);
        return Optional.of(new BundleTooltip(new BundleContents(nonNullList)));
    }
    @Override
    public void onDestroyed(@NotNull ItemEntity itemEntity) {
        ItemUtils.onContainerDestroyed(itemEntity, getContents(itemEntity.getItem()).toList());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        // tooltipComponents.add(Component.translatable("item.scguns.ammo_pouch.fullness").withStyle(ChatFormatting.GRAY));
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    protected void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    public static int getMaxItemCount(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof AmmoBoxItem) {
            double multiplier = Config.COMMON.gameplay.ammoBoxCapacityMultiplier.get();
            return (int) (((AmmoBoxItem) item).getBaseMaxItemCount() * multiplier);
        }
        return 256;
    }

    protected abstract int getBaseMaxItemCount();



    public static Stream<ItemStack> getContents(ItemStack stack) {
        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
            TagKey<Item> ammoTag = ItemTags.create(((CreativeAmmoBoxItem)stack.getItem()).getAmmoTag());
            return BuiltInRegistries.ITEM.stream()
                    .filter(item -> item.builtInRegistryHolder().is(ammoTag))
                    .map(item -> new ItemStack(item, Integer.MAX_VALUE));
        }
        CompoundTag compoundTag = getCustomData(stack);
        ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
        return listTag.stream()
                .map(CompoundTag.class::cast)
                .map(tag -> ItemStack.parseOptional(Gun.builtInRegistryProvider(), tag));
    }

    private static CompoundTag saveStack(ItemStack stack) {
        return saveStack(stack, Gun.builtInRegistryProvider());
    }

    private static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider provider) {
        return (CompoundTag) stack.save(provider, new CompoundTag());
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
