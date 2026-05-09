package top.ribs.scguns.init;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.ribs.scguns.item.EnergyGunItem;
import top.ribs.scguns.item.animated.AnimatedEnergyGunItem;
import top.ribs.scguns.item.exosuit.ExoSuitCoreItem;

public class ModCapabilities {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CRYONITER.get(),
                (blockEntity, side) -> blockEntity.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.THERMOLITH.get(),
                (blockEntity, side) -> blockEntity.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.VENT_COLLECTOR.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.LIGHTNING_BATTERY.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.LIGHTNING_BATTERY.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.POLAR_GENERATOR.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.POLAR_GENERATOR.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACERATOR.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MECHANICAL_PRESS.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.POWERED_MACERATOR.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.POWERED_MACERATOR.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.POWERED_MECHANICAL_PRESS.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.POWERED_MECHANICAL_PRESS.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.AUTO_TURRET.get(),
                (blockEntity, side) -> side != Direction.UP ? blockEntity.getItemStackHandler() : null
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.BASIC_TURRET.get(),
                (blockEntity, side) -> side != Direction.UP ? blockEntity.getItemStackHandler() : null
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SHOTGUN_TURRET.get(),
                (blockEntity, side) -> side != Direction.UP ? blockEntity.getItemStackHandler() : null
        );
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> new ExoSuitCoreItem.SimpleExoSuitEnergyStorage(stack),
                ModItems.EXO_SUIT_CORE.get(),
                ModItems.ADVANCED_EXO_SUIT_CORE.get()
        );
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> {
                    if (stack.getItem() instanceof EnergyGunItem energyGun) {
                        return new EnergyGunItem.ItemEnergyStorage(stack, energyGun.getCapacity());
                    }
                    if (stack.getItem() instanceof AnimatedEnergyGunItem animatedEnergyGun) {
                        return new AnimatedEnergyGunItem.ItemEnergyStorage(stack, animatedEnergyGun.getCapacity());
                    }
                    return null;
                },
                ModItems.SCRATCHES.get(),
                ModItems.GALE.get(),
                ModItems.UMAX_PISTOL.get(),
                ModItems.VENTURI.get()
        );
    }
}
