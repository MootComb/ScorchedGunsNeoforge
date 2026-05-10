package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModItems;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GuanoItemEventHandler {
    private static final Set<ItemEntity> GUANO_ITEMS = new HashSet<>();

    @SubscribeEvent
    public static void onItemEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack stack = itemEntity.getItem();
        if (stack.is(ModItems.BAT_GUANO.get())) {
            GUANO_ITEMS.add(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<ItemEntity> iterator = GUANO_ITEMS.iterator();
        while (iterator.hasNext()) {
            ItemEntity itemEntity = iterator.next();
            if (!itemEntity.isAlive() || itemEntity.getItem().isEmpty() || !itemEntity.getItem().is(ModItems.BAT_GUANO.get())) {
                iterator.remove();
                continue;
            }

            if (!itemEntity.onGround() || itemEntity.tickCount % 15 != 0) {
                continue;
            }

            if (tryFormGuanoLayer(itemEntity)) {
                iterator.remove();
            }
        }
    }

    private static boolean tryFormGuanoLayer(ItemEntity itemEntity) {
        Level level = itemEntity.level();
        if (level.isClientSide()) {
            return false;
        }

        BlockPos pos = itemEntity.blockPosition();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = itemEntity.getItem();

        if (state.is(ModBlocks.BAT_GUANO_LAYER.get())) {
            int layers = state.getValue(SnowLayerBlock.LAYERS);
            if (layers < 8) {
                level.setBlock(pos, state.setValue(SnowLayerBlock.LAYERS, layers + 1), 2);
                stack.shrink(1);
                discardIfEmpty(itemEntity);
                return itemEntity.isRemoved();
            }
            return false;
        }

        BlockState below = level.getBlockState(pos.below());
        if (state.isAir() && below.isFaceSturdy(level, pos.below(), Direction.UP)) {
            level.setBlock(pos, ModBlocks.BAT_GUANO_LAYER.get().defaultBlockState(), 2);
            stack.shrink(1);
            discardIfEmpty(itemEntity);
            return itemEntity.isRemoved();
        }

        return false;
    }

    private static void discardIfEmpty(ItemEntity itemEntity) {
        if (itemEntity.getItem().isEmpty()) {
            itemEntity.discard();
        }
    }
}
