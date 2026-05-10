package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.ribs.scguns.init.ModParticleTypes;

import javax.annotation.Nullable;

public class GuanoCandleBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 9.0D, 11.0D);

    private static final int CHECK_INTERVAL = 200;
    private static final int SPAWN_CHANCE = 90;
    private static final int SEARCH_RADIUS = 32;
    private static final int MAX_BATS = 8;

    public GuanoCandleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(LIT, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!state.getValue(LIT) && stack.is(Items.FLINT_AND_STEEL)) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
            level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            if (!level.isClientSide) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                level.scheduleTick(pos, this, CHECK_INTERVAL);
            }
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(LIT) && player.isShiftKeyDown()) {
            level.setBlock(pos, state.setValue(LIT, false), 3);
            level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);

        if (random.nextInt(10) == 0) {
            level.addParticle(ModParticleTypes.SULFUR_DUST.get(), x + random.nextGaussian() * 0.15D, y, z + random.nextGaussian() * 0.15D, 0.0D, 0.03D, 0.0D);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (state.getValue(LIT) && !level.isClientSide) {
            level.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            if (random.nextInt(SPAWN_CHANCE) == 0) {
                this.trySpawnBat(level, pos, random);
            }
            level.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    private void trySpawnBat(ServerLevel level, BlockPos pos, RandomSource random) {
        AABB searchArea = new AABB(pos).inflate(SEARCH_RADIUS);
        if (level.getEntitiesOfClass(Bat.class, searchArea).size() >= MAX_BATS) {
            return;
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos spawnPos = pos.offset(random.nextInt(11) - 5, random.nextInt(6), random.nextInt(11) - 5);
            if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(spawnPos.below()).isFaceSturdy(level, spawnPos.below(), net.minecraft.core.Direction.UP)) {
                continue;
            }

            Bat bat = EntityType.BAT.create(level);
            if (bat != null) {
                bat.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(bat);
                level.playSound(null, spawnPos, SoundEvents.BAT_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
                return;
            }
        }
    }
}
