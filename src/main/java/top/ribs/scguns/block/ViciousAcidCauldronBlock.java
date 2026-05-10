package top.ribs.scguns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.init.ModCauldronInteractions;
import top.ribs.scguns.init.ModParticleTypes;

public class ViciousAcidCauldronBlock extends AbstractCauldronBlock {
    public static final MapCodec<ViciousAcidCauldronBlock> CODEC = simpleCodec(ViciousAcidCauldronBlock::new);
    private static final float ENCHANTMENT_REMOVAL_CHANCE = 0.2F;

    public ViciousAcidCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, ModCauldronInteractions.VICIOUS_ACID);
    }

    @Override
    protected MapCodec<? extends AbstractCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return 0.9375D;
    }

    @Override
    public boolean isFull(BlockState state) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.isEntityInsideContent(state, pos, entity)) {
            ViciousAcidBlock.applyAcidEffects(level, pos, entity, ENCHANTMENT_REMOVAL_CHANCE);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + 0.95D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(ModParticleTypes.ACID_BUBBLE.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
