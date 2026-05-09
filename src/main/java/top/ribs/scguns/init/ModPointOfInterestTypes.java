package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.ribs.scguns.Reference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class ModPointOfInterestTypes
{
    public static final DeferredRegister<PoiType> REGISTER = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Reference.MOD_ID);


    private static DeferredHolder<PoiType, PoiType> register(String name, DeferredBlock<Block> block, int maxFreeTickets) {
        List<DeferredBlock<Block>> blocks = new ArrayList<>();
        blocks.add(block);
        return register(name, blocks, maxFreeTickets);
    }

    private static DeferredHolder<PoiType, PoiType> register(String name, Supplier<List<DeferredBlock<Block>>> supplier, int maxFreeTickets) {
        return register(name, supplier.get(), maxFreeTickets);
    }

    private static DeferredHolder<PoiType, PoiType> register(String name, List<DeferredBlock<Block>> blocks, int maxFreeTickets) {
        return REGISTER.register(name, () -> {
            Set<BlockState> blockStates = new HashSet<>();
            for (DeferredBlock<Block> block : blocks) {
                blockStates.addAll(block.get().getStateDefinition().getPossibleStates());
            }
            return new PoiType(blockStates, maxFreeTickets, 1);
        });
    }
}





