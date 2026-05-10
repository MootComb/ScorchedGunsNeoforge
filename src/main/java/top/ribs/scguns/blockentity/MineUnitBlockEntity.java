package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.throwable.ThrowableBeaconGrenadeEntity;
import top.ribs.scguns.entity.throwable.ThrowableChokeBombEntity;
import top.ribs.scguns.entity.throwable.ThrowableGasGrenadeEntity;
import top.ribs.scguns.entity.throwable.ThrowableGrenadeEntity;
import top.ribs.scguns.entity.throwable.ThrowableMolotovCocktailEntity;
import top.ribs.scguns.entity.throwable.ThrowableNailBombEntity;
import top.ribs.scguns.entity.throwable.ThrowableStunGrenadeEntity;
import top.ribs.scguns.entity.throwable.ThrowableSwarmBombEntity;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.BeaconGrenadeItem;
import top.ribs.scguns.item.ChokeBombItem;
import top.ribs.scguns.item.GasGrenadeItem;
import top.ribs.scguns.item.GrenadeItem;
import top.ribs.scguns.item.MolotovCocktailItem;
import top.ribs.scguns.item.NailBombItem;
import top.ribs.scguns.item.StunGrenadeItem;
import top.ribs.scguns.item.SwarmBombItem;

import java.util.List;
import java.util.UUID;

public class MineUnitBlockEntity extends BlockEntity {
    private static final double DETECTION_RADIUS = 1.25D;
    private ItemStack storedGrenade = ItemStack.EMPTY;
    private UUID placerUUID;
    private boolean primed;

    public MineUnitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MINE_UNIT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MineUnitBlockEntity mineUnit) {
        if (level.isClientSide || !mineUnit.hasGrenade() || !mineUnit.isPrimed()) {
            return;
        }

        AABB detectionBox = new AABB(pos).inflate(DETECTION_RADIUS);
        UUID placerUUID = mineUnit.getPlacerUUID();

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, detectionBox, entity ->
                entity.isAlive()
                        && !entity.isSpectator()
                        && (placerUUID == null || !entity.getUUID().equals(placerUUID))
                        && !entity.getType().is(ModTags.Entities.IGNORES_MINE_UNITS));
        if (!nearbyEntities.isEmpty()) {
            mineUnit.triggerGrenade(level, pos, nearbyEntities.get(0));
        }
    }

    private void triggerGrenade(Level level, BlockPos pos, @Nullable LivingEntity triggeringEntity) {
        if (this.storedGrenade.isEmpty()) {
            return;
        }

        LivingEntity placer = triggeringEntity;
        if (this.placerUUID != null && level instanceof ServerLevel serverLevel && serverLevel.getEntity(this.placerUUID) instanceof LivingEntity livingEntity) {
            placer = livingEntity;
        }
        if (placer == null) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        this.spawnTriggerEffects(level, x, y, z);
        Item grenadeItem = this.storedGrenade.getItem();
        ItemStack grenadeStack = this.storedGrenade.copy();

        if (grenadeItem instanceof StunGrenadeItem) {
            ThrowableStunGrenadeEntity grenade = new ThrowableStunGrenadeEntity(level, placer, 2);
            grenade.setPos(x, y, z);
            level.addFreshEntity(grenade);
        } else if (grenadeItem instanceof GasGrenadeItem) {
            ThrowableGasGrenadeEntity grenade = new ThrowableGasGrenadeEntity(level, placer, 2, 6.0F);
            grenade.setPos(x, y, z);
            grenade.setItem(grenadeStack);
            level.addFreshEntity(grenade);
        } else if (grenadeItem instanceof MolotovCocktailItem) {
            ThrowableMolotovCocktailEntity grenade = new ThrowableMolotovCocktailEntity(level, placer, 2);
            grenade.setPos(x, y, z);
            level.addFreshEntity(grenade);
        } else if (grenadeItem instanceof ChokeBombItem) {
            ThrowableChokeBombEntity grenade = new ThrowableChokeBombEntity(level, placer, 2, 4.0F);
            grenade.setPos(x, y, z);
            grenade.setItem(grenadeStack);
            level.addFreshEntity(grenade);
        } else if (grenadeItem instanceof BeaconGrenadeItem) {
            ThrowableBeaconGrenadeEntity grenade = new ThrowableBeaconGrenadeEntity(level, placer, 2);
            grenade.setPos(x, y, z);
            grenade.setItem(grenadeStack);
            level.addFreshEntity(grenade);
        } else if (grenadeItem instanceof NailBombItem) {
            ThrowableNailBombEntity grenade = new ThrowableNailBombEntity(level, placer, 2);
            grenade.setPos(x, y, z);
            grenade.setItem(grenadeStack);
            level.addFreshEntity(grenade);
        } else if (grenadeItem instanceof SwarmBombItem) {
            ThrowableSwarmBombEntity grenade = new ThrowableSwarmBombEntity(level, placer, 2);
            grenade.setPos(x, y, z);
            grenade.setItem(grenadeStack);
            level.addFreshEntity(grenade);
        } else if (grenadeItem instanceof GrenadeItem) {
            ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(level, placer, 2);
            grenade.setPos(x, y, z);
            grenade.setItem(grenadeStack);
            level.addFreshEntity(grenade);
        }

        this.storedGrenade = ItemStack.EMPTY;
        this.setChanged();
        level.removeBlock(this.worldPosition, false);
    }

    private void spawnTriggerEffects(Level level, double x, double y, double z) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            level.playSound(null, x, y, z, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 0.8F, 1.5F);
            level.playSound(null, x, y, z, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.6F, 1.2F);
            for (int i = 0; i < 8; i++) {
                double offsetX = (level.random.nextDouble() - 0.5D) * 0.4D;
                double offsetY = level.random.nextDouble() * 0.3D;
                double offsetZ = (level.random.nextDouble() - 0.5D) * 0.4D;
                serverLevel.sendParticles(ParticleTypes.SMOKE, x + offsetX, y + offsetY, z + offsetZ, 1, 0.0D, 0.05D, 0.0D, 0.02D);
            }
            for (int i = 0; i < 5; i++) {
                double offsetX = (level.random.nextDouble() - 0.5D) * 0.3D;
                double offsetY = level.random.nextDouble() * 0.2D;
                double offsetZ = (level.random.nextDouble() - 0.5D) * 0.3D;
                serverLevel.sendParticles(ParticleTypes.POOF, x + offsetX, y + offsetY, z + offsetZ, 1, 0.0D, 0.03D, 0.0D, 0.01D);
            }
        }
    }

    public boolean hasGrenade() {
        return !this.storedGrenade.isEmpty();
    }

    public boolean isPrimed() {
        return this.primed;
    }

    public void setPrimed(boolean primed) {
        this.primed = primed;
        this.setChanged();
        this.sync();
    }

    @Nullable
    public UUID getPlacerUUID() {
        return this.placerUUID;
    }

    public void setGrenade(ItemStack stack, LivingEntity placer) {
        this.storedGrenade = stack.copy();
        this.placerUUID = placer.getUUID();
        this.setChanged();
        this.sync();
    }

    public void dropGrenade() {
        if (this.level != null && !this.storedGrenade.isEmpty()) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.storedGrenade);
            this.storedGrenade = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.storedGrenade.isEmpty()) {
            tag.put("StoredGrenade", this.storedGrenade.save(registries, new CompoundTag()));
        }
        if (this.placerUUID != null) {
            tag.putUUID("PlacerUUID", this.placerUUID);
        }
        tag.putBoolean("Primed", this.primed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("StoredGrenade")) {
            this.storedGrenade = ItemStack.parseOptional(registries, tag.getCompound("StoredGrenade"));
        }
        if (tag.hasUUID("PlacerUUID")) {
            this.placerUUID = tag.getUUID("PlacerUUID");
        }
        this.primed = tag.getBoolean("Primed");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
