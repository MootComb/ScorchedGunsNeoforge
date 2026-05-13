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
import top.ribs.scguns.block.AutoTurretBlock;
import top.ribs.scguns.client.screen.AutoTurretMenu;
import top.ribs.scguns.init.ModBlockEntities;

public class AutoTurretBlockEntity extends TurretBlockEntity {
    public AutoTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTO_TURRET.get(), pos, state, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "auto_turret"));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.auto_turret");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        return this.hasTargetingModule() ? new AutoTurretMenu(id, playerInventory, this) : this.missingTargetingModule(player);
    }

    @Override
    protected boolean isPowered(BlockState state) {
        return state.getValue(AutoTurretBlock.POWERED);
    }
}
