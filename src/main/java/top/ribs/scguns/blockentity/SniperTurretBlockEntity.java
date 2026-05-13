package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.SniperTurretBlock;
import top.ribs.scguns.client.screen.SniperTurretMenu;
import top.ribs.scguns.init.ModBlockEntities;

public class SniperTurretBlockEntity extends TurretBlockEntity {
    public SniperTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SNIPER_TURRET.get(), pos, state, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "sniper_turret"));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.sniper_turret");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        return this.hasTargetingModule() ? new SniperTurretMenu(id, playerInventory, this) : this.missingTargetingModule(player);
    }

    @Override
    protected boolean isPowered(BlockState state) {
        return state.getValue(SniperTurretBlock.POWERED);
    }
}
