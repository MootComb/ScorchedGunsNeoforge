package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import top.ribs.scguns.init.ModParticleTypes;

import java.util.ArrayList;
import java.util.List;

public class ViciousAcidBlock extends LiquidBlock {
    private static final int DAMAGE_INTERVAL = 10;
    private static final float DAMAGE_AMOUNT = 2.0F;
    private static final int ARMOR_DAMAGE = 3;
    private static final int HELD_ITEM_DAMAGE = 5;
    private static final int DROPPED_ITEM_DAMAGE = 5;
    private static final float ENCHANTMENT_REMOVAL_CHANCE = 0.15F;

    public ViciousAcidBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || entity.tickCount % DAMAGE_INTERVAL != 0) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.hurt(level.damageSources().magic(), DAMAGE_AMOUNT);
            damageArmor(livingEntity, level, pos);
            damageHeldItems(livingEntity, level, pos);
        } else if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (!tryRemoveEnchantment(stack, level, pos)) {
                damageDroppedStack(stack, DROPPED_ITEM_DAMAGE);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) == 0 && random.nextInt(5) == 0) {
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + 0.95D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(ModParticleTypes.ACID_BUBBLE.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    static boolean applyAcidEffects(Level level, BlockPos pos, Entity entity, float enchantmentRemovalChance) {
        if (level.isClientSide || entity.tickCount % DAMAGE_INTERVAL != 0) {
            return false;
        }

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.hurt(level.damageSources().magic(), DAMAGE_AMOUNT);
            damageArmor(livingEntity, level, pos, enchantmentRemovalChance);
            damageHeldItems(livingEntity, level, pos, enchantmentRemovalChance);
            return true;
        }

        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (!tryRemoveEnchantment(stack, level, pos, enchantmentRemovalChance)) {
                damageDroppedStack(stack, DROPPED_ITEM_DAMAGE);
            }
            return true;
        }

        return false;
    }

    private void damageArmor(LivingEntity livingEntity, Level level, BlockPos pos) {
        damageArmor(livingEntity, level, pos, ENCHANTMENT_REMOVAL_CHANCE);
    }

    private static void damageArmor(LivingEntity livingEntity, Level level, BlockPos pos, float enchantmentRemovalChance) {
        for (ItemStack stack : livingEntity.getArmorSlots()) {
            if (stack.isEmpty() || tryRemoveEnchantment(stack, level, pos, enchantmentRemovalChance) || !stack.isDamageableItem()) {
                continue;
            }
            damageLivingStack(stack, ARMOR_DAMAGE, livingEntity, null);
        }
    }

    private void damageHeldItems(LivingEntity livingEntity, Level level, BlockPos pos) {
        damageHeldItems(livingEntity, level, pos, ENCHANTMENT_REMOVAL_CHANCE);
    }

    private static void damageHeldItems(LivingEntity livingEntity, Level level, BlockPos pos, float enchantmentRemovalChance) {
        damageHeldItem(livingEntity.getMainHandItem(), livingEntity, EquipmentSlot.MAINHAND, level, pos, enchantmentRemovalChance);
        damageHeldItem(livingEntity.getOffhandItem(), livingEntity, EquipmentSlot.OFFHAND, level, pos, enchantmentRemovalChance);
    }

    private static void damageHeldItem(ItemStack stack, LivingEntity livingEntity, EquipmentSlot slot, Level level, BlockPos pos, float enchantmentRemovalChance) {
        if (stack.isEmpty() || tryRemoveEnchantment(stack, level, pos, enchantmentRemovalChance) || !stack.isDamageableItem()) {
            return;
        }
        damageLivingStack(stack, HELD_ITEM_DAMAGE, livingEntity, slot);
    }

    private static void damageLivingStack(ItemStack stack, int amount, LivingEntity livingEntity, EquipmentSlot slot) {
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return;
        }
        int newDamage = stack.getDamageValue() + amount;
        if (newDamage >= stack.getMaxDamage() - 1) {
            stack.setDamageValue(stack.getMaxDamage() - 1);
        } else {
            stack.hurtAndBreak(amount, livingEntity, slot);
        }
    }

    private static void damageDroppedStack(ItemStack stack, int amount) {
        if (stack.isEmpty() || !stack.isDamageableItem() || stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return;
        }
        stack.setDamageValue(Mth.clamp(stack.getDamageValue() + amount, 0, stack.getMaxDamage() - 1));
    }

    private boolean tryRemoveEnchantment(ItemStack stack, Level level, BlockPos pos) {
        return tryRemoveEnchantment(stack, level, pos, ENCHANTMENT_REMOVAL_CHANCE);
    }

    private static boolean tryRemoveEnchantment(ItemStack stack, Level level, BlockPos pos, float chance) {
        if (stack.isEmpty() || level.random.nextFloat() > chance) {
            return false;
        }

        if (tryRemoveFromComponent(stack, DataComponents.ENCHANTMENTS, level, pos)) {
            return true;
        }

        return tryRemoveFromComponent(stack, DataComponents.STORED_ENCHANTMENTS, level, pos);
    }

    private static boolean tryRemoveFromComponent(ItemStack stack, net.minecraft.core.component.DataComponentType<ItemEnchantments> componentType, Level level, BlockPos pos) {
        ItemEnchantments enchantments = stack.getOrDefault(componentType, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            return false;
        }

        List<Holder<Enchantment>> keys = new ArrayList<>(enchantments.keySet());
        Holder<Enchantment> removed = keys.get(level.random.nextInt(keys.size()));
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
        mutable.removeIf(enchantment -> enchantment.equals(removed));
        stack.set(componentType, mutable.toImmutable());
        spawnEnchantmentRemovalParticles(level, pos);
        return true;
    }

    private static void spawnEnchantmentRemovalParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 8, 0.35D, 0.35D, 0.35D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.0D);
        }
    }
}
