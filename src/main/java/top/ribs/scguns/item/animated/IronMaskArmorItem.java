package top.ribs.scguns.item.animated;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import top.ribs.scguns.client.render.armor.IronMaskArmorRenderer;
import top.ribs.scguns.entity.monster.PraetorEntity;
import top.ribs.scguns.init.ModArmorMaterials;
import top.ribs.scguns.init.ModEntities;

import java.util.function.Consumer;

public class IronMaskArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public IronMaskArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, ModArmorMaterials.withDurability(properties, material, type));
    }

    @Override
    public void createGeoRenderer(Consumer<software.bernie.geckolib.animatable.client.GeoRenderProvider> consumer) {
        top.ribs.scguns.client.render.armor.ScGunsGeoArmorRenderProvider.accept(consumer, IronMaskArmorRenderer::new);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player != null && player.isShiftKeyDown() && this.isValidPraetorRitualStructure(level, pos)) {
            if (!level.isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }

                BlockPos centerPos = this.findCenterPosition(level, pos);
                level.setBlock(centerPos, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above().north(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above().south(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above().east(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above().west(), Blocks.AIR.defaultBlockState(), 3);

                this.spawnCreationEffects((ServerLevel) level, centerPos.above());
                level.playSound(null, centerPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 0.6F);
                level.playSound(null, centerPos, SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.BLOCKS, 0.6F, 1.2F);

                double random = level.random.nextDouble();
                if (random < 0.5D) {
                    Vindicator vindicator = new Vindicator(EntityType.VINDICATOR, level);
                    vindicator.moveTo(centerPos.getX() + 0.5D, centerPos.getY(), centerPos.getZ() + 0.5D, 0.0F, 0.0F);
                    level.addFreshEntity(vindicator);
                    level.playSound(null, centerPos, SoundEvents.VINDICATOR_CELEBRATE, SoundSource.HOSTILE, 1.0F, 0.8F);
                } else if (random < 0.75D) {
                    PraetorEntity praetor = this.createNeutralPraetor(level, centerPos, player);
                    level.addFreshEntity(praetor);
                    level.playSound(null, centerPos, SoundEvents.RAVAGER_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.2F);
                } else {
                    PraetorEntity praetor = new PraetorEntity(ModEntities.PRAETOR.get(), level);
                    praetor.moveTo(centerPos.getX() + 0.5D, centerPos.getY(), centerPos.getZ() + 0.5D, 0.0F, 0.0F);
                    level.addFreshEntity(praetor);
                    level.playSound(null, centerPos, SoundEvents.RAVAGER_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.8F);
                }
            }

            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return player.isShiftKeyDown() ? InteractionResultHolder.pass(player.getItemInHand(hand)) : super.use(level, player, hand);
    }

    private boolean isValidPraetorRitualStructure(Level level, BlockPos pos) {
        BlockState clickedBlock = level.getBlockState(pos);
        if (!clickedBlock.is(Blocks.IRON_BLOCK)) {
            return false;
        }
        return this.isTShape(level, pos) ||
                this.isTShape(level, pos.below()) ||
                this.isTShape(level, pos.north()) ||
                this.isTShape(level, pos.south()) ||
                this.isTShape(level, pos.east()) ||
                this.isTShape(level, pos.west());
    }

    private boolean isTShape(Level level, BlockPos basePos) {
        return level.getBlockState(basePos).is(Blocks.IRON_BLOCK) &&
                level.getBlockState(basePos.above()).is(Blocks.IRON_BLOCK) &&
                ((level.getBlockState(basePos.above().north()).is(Blocks.IRON_BLOCK) &&
                        level.getBlockState(basePos.above().south()).is(Blocks.IRON_BLOCK)) ||
                        (level.getBlockState(basePos.above().east()).is(Blocks.IRON_BLOCK) &&
                                level.getBlockState(basePos.above().west()).is(Blocks.IRON_BLOCK)));
    }

    private BlockPos findCenterPosition(Level level, BlockPos pos) {
        if (this.isTShape(level, pos)) {
            return pos;
        } else if (this.isTShape(level, pos.below())) {
            return pos.below();
        } else if (this.isTShape(level, pos.north())) {
            return pos.north();
        } else if (this.isTShape(level, pos.south())) {
            return pos.south();
        } else if (this.isTShape(level, pos.east())) {
            return pos.east();
        } else if (this.isTShape(level, pos.west())) {
            return pos.west();
        }
        return pos;
    }

    private void spawnCreationEffects(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 30; i++) {
            double x = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 2.5D;
            double y = pos.getY() + level.random.nextDouble() * 2.5D;
            double z = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 2.5D;
            level.sendParticles(ParticleTypes.ASH, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.05D);
        }

        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 2.0D;
            double y = pos.getY() + level.random.nextDouble() * 2.0D;
            double z = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 2.0D;
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.0D, 0.1D, 0.0D, 0.05D);
        }
    }

    private PraetorEntity createNeutralPraetor(Level level, BlockPos centerPos, Player creator) {
        PraetorEntity praetor = new PraetorEntity(ModEntities.PRAETOR.get(), level);
        praetor.moveTo(centerPos.getX() + 0.5D, centerPos.getY(), centerPos.getZ() + 0.5D, 0.0F, 0.0F);

        AttributeInstance maxHealthAttribute = praetor.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(maxHealthAttribute.getBaseValue() * 0.6D);
            praetor.setHealth((float) maxHealthAttribute.getValue());
        }

        if (creator != null) {
            praetor.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal<?>);
            praetor.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    praetor, Player.class, 10, true, false,
                    player -> player != creator && !((Player) player).isCreative() && !((Player) player).isSpectator()
            ));
        }

        return praetor;
    }

    private PlayState predicate(AnimationState<IronMaskArmorItem> animationState) {
        animationState.getController().setAnimation(RawAnimation.begin().then("animation.iron_mask.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
