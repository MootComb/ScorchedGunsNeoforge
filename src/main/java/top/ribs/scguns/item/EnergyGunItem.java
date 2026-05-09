package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import top.ribs.scguns.interfaces.IEnergyGun;

import java.util.List;

public class EnergyGunItem extends GunItem implements IEnergyGun {
    private static final String ENERGY_TAG = "Energy";
    private final int capacity;

    public EnergyGunItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; // Always show the energy bar
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxEnergy = getMaxEnergyStored(stack);
        if (maxEnergy == 0) return 0;
        return Math.round(13.0F * getEnergyStored(stack) / maxEnergy);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF00; // Green color for the energy bar
    }

    public int getEnergyStored(ItemStack stack) {
        IEnergyStorage energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energyStorage != null ? energyStorage.getEnergyStored() : 0;
    }

    public int getMaxEnergyStored(ItemStack stack) {
        IEnergyStorage energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energyStorage != null ? energyStorage.getMaxEnergyStored() : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        int energyStored = getEnergyStored(stack);
        int maxEnergy = getMaxEnergyStored(stack);

        tooltip.add(Component.translatable("info.scguns.energy")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(energyStored + " / " + maxEnergy + " FE").withStyle(ChatFormatting.GREEN)));
    }

    public static class ItemEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;
        private final int capacity;
        private int energy;

        public ItemEnergyStorage(ItemStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
            this.energy = loadEnergyFromNBT(); // Load energy from NBT when the capability is created
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int energyReceived = Math.min(capacity - energy, maxReceive);
            if (!simulate) {
                energy += energyReceived;
                updateEnergyTag();
            }
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int energyExtracted = Math.min(energy, maxExtract);
            if (!simulate) {
                energy -= energyExtracted;
                updateEnergyTag();
            }
            return energyExtracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        private void updateEnergyTag() {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(ENERGY_TAG, energy));
        }

        private int loadEnergyFromNBT() {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) {
                return 0;
            }

            CompoundTag tag = customData.copyTag();
            return tag.contains(ENERGY_TAG, Tag.TAG_INT) ? tag.getInt(ENERGY_TAG) : 0;
        }
    }

}
