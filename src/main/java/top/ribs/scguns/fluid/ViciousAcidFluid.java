package top.ribs.scguns.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class ViciousAcidFluid extends BaseFlowingFluid {
    protected ViciousAcidFluid(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        reactWithSurroundings(level, pos);
        super.tick(level, pos, state);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    private static void reactWithSurroundings(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos targetPos = pos.relative(direction);
            FluidState fluidState = level.getFluidState(targetPos);
            BlockState blockState = level.getBlockState(targetPos);

            if (fluidState.is(FluidTags.LAVA) || blockState.is(Blocks.FIRE)) {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
                level.explode(null, targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D, 2.0F, Level.ExplosionInteraction.BLOCK);
                return;
            }

            if (fluidState.is(FluidTags.WATER)) {
                level.setBlock(targetPos, Blocks.PRISMARINE.defaultBlockState(), 3);
                level.playSound(null, targetPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                for (int i = 0; i < 8; i++) {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, targetPos.getX() + 0.5D, targetPos.getY() + 1.0D, targetPos.getZ() + 0.5D, 1, 0.3D, 0.3D, 0.3D, 0.0D);
                }
            }
        }
    }

    public static class Flowing extends ViciousAcidFluid {
        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends ViciousAcidFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
