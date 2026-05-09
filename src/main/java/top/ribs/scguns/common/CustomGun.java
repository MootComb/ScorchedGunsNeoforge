package top.ribs.scguns.common;

import top.ribs.scguns.annotation.Ignored;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Author: MrCrayfish
 */
public class CustomGun implements INBTSerializable<CompoundTag>
{
    @Ignored
    public ItemStack model;
    public Gun gun;

    public ItemStack getModel()
    {
        return this.model;
    }

    public Gun getGun()
    {
        return this.gun;
    }

    public CompoundTag serializeNBT()
    {
        return this.serializeNBT(Gun.builtInRegistryProvider());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries)
    {
        CompoundTag compound = new CompoundTag();
        compound.put("Model", this.model.save(registries));
        compound.put("Gun", this.gun.serializeNBT(registries));
        return compound;
    }

    public void deserializeNBT(CompoundTag compound)
    {
        this.deserializeNBT(Gun.builtInRegistryProvider(), compound);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag compound)
    {
        this.model = ItemStack.parseOptional(registries, compound.getCompound("Model"));
        this.gun = Gun.create(registries, compound.getCompound("Gun"));
    }
}
