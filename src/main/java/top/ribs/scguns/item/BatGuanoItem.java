package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.init.ModBlocks;

public class BatGuanoItem extends Item {
    private static final float BASE_SUCCESS_CHANCE = 0.95F;
    private static final float ADJACENT_SPREAD_CHANCE = 0.45F;
    private static final float CASCADE_CHANCE = 0.15F;

    public BatGuanoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof BonemealableBlock && applyGuanoFertilizer(stack, level, pos)) {
            if (!level.isClientSide) {
                level.levelEvent(1505, pos, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (state.is(ModBlocks.BAT_GUANO_LAYER.get())) {
            int layers = state.getValue(SnowLayerBlock.LAYERS);
            if (layers < 8) {
                level.setBlock(pos, state.setValue(SnowLayerBlock.LAYERS, layers + 1), 2);
                shrinkUnlessCreative(stack, player);
                return InteractionResult.SUCCESS;
            }
        }

        BlockPos above = pos.above();
        if (level.getBlockState(above).isAir() && state.isFaceSturdy(level, pos, Direction.UP)) {
            level.setBlock(above, ModBlocks.BAT_GUANO_LAYER.get().defaultBlockState(), 2);
            shrinkUnlessCreative(stack, player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    public static boolean applyGuanoFertilizer(ItemStack stack, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BonemealableBlock bonemealableBlock) || !bonemealableBlock.isValidBonemealTarget(level, pos, state)) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = level.getRandom();
            if (random.nextFloat() < BASE_SUCCESS_CHANCE && bonemealableBlock.isBonemealSuccess(level, random, pos, state)) {
                bonemealableBlock.performBonemeal(serverLevel, random, pos, state);
            }
            if (random.nextFloat() < ADJACENT_SPREAD_CHANCE) {
                spreadToAdjacent(serverLevel, pos, random, CASCADE_CHANCE);
            }
            stack.shrink(1);
        }
        return true;
    }

    private static void spreadToAdjacent(ServerLevel level, BlockPos pos, RandomSource random, float cascadeChance) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (!(adjacentState.getBlock() instanceof BonemealableBlock bonemealableBlock)) {
                continue;
            }
            if (!bonemealableBlock.isValidBonemealTarget(level, adjacentPos, adjacentState)) {
                continue;
            }
            if (bonemealableBlock.isBonemealSuccess(level, random, adjacentPos, adjacentState)) {
                bonemealableBlock.performBonemeal(level, random, adjacentPos, adjacentState);
                level.levelEvent(1505, adjacentPos, 0);
                if (random.nextFloat() < cascadeChance) {
                    spreadToAdjacent(level, adjacentPos, random, cascadeChance * 0.5F);
                }
            }
        }
    }

    private static void shrinkUnlessCreative(ItemStack stack, Player player) {
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
