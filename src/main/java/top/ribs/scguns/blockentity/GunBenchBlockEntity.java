package top.ribs.scguns.blockentity;


import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.client.screen.GunBenchMenu;
import top.ribs.scguns.init.ModBlockEntities;


public class GunBenchBlockEntity extends BlockEntity implements MenuProvider {
    private final SimpleContainer inventory = new SimpleContainer(12) {
        @Override
        public void setChanged() {
            super.setChanged();
            GunBenchBlockEntity.this.setChanged();  // Mark the block entity as dirty
        }
    };

    public GunBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GUN_BENCH.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.gun_bench");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GunBenchMenu(id, playerInventory, this.inventory, ContainerLevelAccess.create(this.level, this.worldPosition));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Load inventory from NBT
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            CompoundTag itemTag = tag.getCompound("Item" + i);
            if (!itemTag.isEmpty()) {
                this.inventory.setItem(i, ItemStack.parseOptional(registries, itemTag));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Save inventory to NBT
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack itemstack = this.inventory.getItem(i);
            CompoundTag itemTag = new CompoundTag();
            if (!itemstack.isEmpty()) {
                itemstack.save(registries, itemTag);
            }
            tag.put("Item" + i, itemTag);
        }
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public void dropContents(Player player) {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                if (player != null) {
                    player.drop(itemstack, false);
                } else {
                    this.level.addFreshEntity(new ItemEntity(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), itemstack));
                }
            }
        }
        this.inventory.clearContent();
    }
}
