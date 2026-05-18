package top.ribs.scguns.blockentity;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

class ContainerItemHandler implements IItemHandlerModifiable {
    private final Container container;

    ContainerItemHandler(Container container) {
        this.container = container;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.container.getContainerSize()) {
            return;
        }
        this.container.setItem(slot, stack);
        this.container.setChanged();
    }

    @Override
    public int getSlots() {
        return this.container.getContainerSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= this.container.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.container.getItem(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || slot < 0 || slot >= this.container.getContainerSize() || !this.isItemValid(slot, stack)) {
            return stack;
        }

        ItemStack existing = this.container.getItem(slot);
        int limit = Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }

        int inserted = Math.min(limit, stack.getCount());
        if (!simulate) {
            ItemStack target = existing.isEmpty() ? stack.copyWithCount(inserted) : existing.copy();
            if (!existing.isEmpty()) {
                target.grow(inserted);
            }
            this.container.setItem(slot, target);
            this.container.setChanged();
        }

        if (inserted == stack.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || slot < 0 || slot >= this.container.getContainerSize()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = this.container.getItem(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(extracted);
        if (!simulate) {
            existing.shrink(extracted);
            if (existing.isEmpty()) {
                this.container.setItem(slot, ItemStack.EMPTY);
            } else {
                this.container.setItem(slot, existing);
            }
            this.container.setChanged();
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.container.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot >= 0 && slot < this.container.getContainerSize() && this.container.canPlaceItem(slot, stack);
    }
}
