package top.ribs.scguns.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class NiterGlassBlock extends TransparentBlock {
    public static final BooleanProperty TRANSPARENT = BooleanProperty.create("transparent");

    public NiterGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TRANSPARENT, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TRANSPARENT);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            updateStateAndNeighbors(world, pos);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean isPowered = hasRedstoneSignal(context.getLevel(), context.getClickedPos());
        return this.defaultBlockState().setValue(TRANSPARENT, isPowered);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (adjacentState.is(this)) {
            return true;
        }
        return super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(TRANSPARENT);
    }

    @Override
    public int getLightBlock(BlockState state, @NotNull BlockGetter world, BlockPos pos) {
        return state.getValue(TRANSPARENT) ? 0 : world.getMaxLightLevel();
    }

    private static boolean hasRedstoneSignal(Level world, BlockPos pos) {
        return world.hasNeighborSignal(pos) || world.getDirectSignalTo(pos) > 0;
    }

    private void updateStateAndNeighbors(Level world, BlockPos pos) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(pos);
        visited.add(pos);
        boolean isPowered = false;

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            BlockState currentState = world.getBlockState(currentPos);

            if (hasRedstoneSignal(world, currentPos)) {
                isPowered = true;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);
                if (!visited.contains(neighborPos) && world.getBlockState(neighborPos).getBlock() instanceof NiterGlassBlock) {
                    queue.add(neighborPos);
                    visited.add(neighborPos);
                }
            }
        }

        for (BlockPos currentPos : visited) {
            BlockState currentState = world.getBlockState(currentPos);
            if (currentState.getBlock() instanceof NiterGlassBlock && currentState.getValue(TRANSPARENT) != isPowered) {
                world.setBlock(currentPos, currentState.setValue(TRANSPARENT, isPowered), Block.UPDATE_ALL);
            }
        }
    }
}
