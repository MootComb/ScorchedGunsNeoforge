package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.blockentity.MineUnitBlockEntity;
import top.ribs.scguns.init.ModTags;

public class MetalDetectorItem extends Item {
    private static final int DETECTION_RADIUS = 16;
    private static final int COOLDOWN_TICKS = 30;
    private static final double BASE_PULL_STRENGTH = 0.5D;
    private static final double EXTRA_PULL_STRENGTH = 0.8D;

    public MetalDetectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        BlockPos nearestMetal = this.findNearestMetal(level, player.blockPosition());
        if (nearestMetal != null) {
            if (!level.isClientSide) {
                double distance = Math.sqrt(player.blockPosition().distSqr(nearestMetal));
                this.pullPlayerToward(player, nearestMetal, distance);
                this.spawnTargetHint((ServerLevel) level, nearestMetal);
                level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.5F, 1.0F + (float) (distance / DETECTION_RADIUS) * 0.5F);
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            return InteractionResultHolder.success(stack);
        }

        if (level.isClientSide) {
            level.playSound(player, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.PLAYERS, 0.3F, 0.8F);
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS / 2);
        return InteractionResultHolder.fail(stack);
    }

    private void spawnTargetHint(ServerLevel level, BlockPos targetPos) {
        BlockState state = level.getBlockState(targetPos);
        boolean isMine = level.getBlockEntity(targetPos) instanceof MineUnitBlockEntity;
        double baseY = isMine ? targetPos.getY() + 0.5D : targetPos.getY() + 1.0D;
        int particleCount = isMine ? 3 : 2;
        for (int i = 0; i < particleCount; i++) {
            double offsetX = targetPos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.3D;
            double offsetY = baseY + level.random.nextDouble() * 0.1D;
            double offsetZ = targetPos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.3D;
            level.sendParticles(ParticleTypes.ENCHANT, offsetX, offsetY, offsetZ, 1, 0.0D, 0.0D, 0.0D, 0.01D);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 0;
    }

    @Nullable
    private BlockPos findNearestMetal(Level level, BlockPos playerPos) {
        BlockPos nearestPos = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (int x = -DETECTION_RADIUS; x <= DETECTION_RADIUS; x++) {
            for (int y = -DETECTION_RADIUS; y <= DETECTION_RADIUS; y++) {
                for (int z = -DETECTION_RADIUS; z <= DETECTION_RADIUS; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (state.is(ModTags.Blocks.METAL_DETECTABLE)) {
                        double distSq = playerPos.distSqr(checkPos);
                        if (distSq < nearestDistSq) {
                            nearestDistSq = distSq;
                            nearestPos = checkPos;
                        }
                    }
                }
            }
        }
        return nearestPos;
    }

    private void pullPlayerToward(Player player, BlockPos targetPos, double distance) {
        Vec3 playerPos = player.position();
        Vec3 targetVec = Vec3.atCenterOf(targetPos);
        Vec3 direction = targetVec.subtract(playerPos).normalize();
        double pullStrength = BASE_PULL_STRENGTH + EXTRA_PULL_STRENGTH * (1.0D - Math.min(distance / DETECTION_RADIUS, 1.0D));
        player.setDeltaMovement(player.getDeltaMovement().add(direction.scale(pullStrength)));
        player.hurtMarked = true;
    }
}
