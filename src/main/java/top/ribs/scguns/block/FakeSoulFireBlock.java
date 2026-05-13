package top.ribs.scguns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FakeSoulFireBlock extends BaseFireBlock {
    public static final MapCodec<FakeSoulFireBlock> CODEC = simpleCodec(properties -> new FakeSoulFireBlock(properties, 2.0F));

    public FakeSoulFireBlock(BlockBehaviour.Properties properties, float fireDamage) {
        super(properties, fireDamage);
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return false;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        BlockState aboveState = level.getBlockState(pos.above());
        return !(aboveState.getBlock() instanceof BaseFireBlock)
                && (belowState.isFaceSturdy(level, pos.below(), Direction.UP) || !belowState.isAir());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return this.canSurvive(state, level, currentPos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        return this.canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            level.scheduleTick(pos, this, 50 + level.random.nextInt(21));
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.fireImmune()) {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0) {
                entity.igniteForSeconds(8.0F);
            }

            entity.hurt(level.damageSources().inFire(), 2.0F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    1.0F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.3F,
                    false
            );
        }

        for (int i = 0; i < random.nextInt(2) + 2; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5 + 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        }

        if (random.nextInt(5) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5 + 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SOUL, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
