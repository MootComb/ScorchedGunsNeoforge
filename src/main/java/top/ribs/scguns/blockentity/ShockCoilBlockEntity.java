package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.block.HostileTurretTargetingBlock;
import top.ribs.scguns.block.PlayerTurretTargetingBlock;
import top.ribs.scguns.block.ShockCoilBlock;
import top.ribs.scguns.block.TurretTargetingBlock;
import top.ribs.scguns.config.ShockCoilConfig;
import top.ribs.scguns.init.ModBlockEntities;

import java.util.List;
import java.util.function.Predicate;

public class ShockCoilBlockEntity extends BlockEntity {
    private final EnergyStorage energyStorage = new EnergyStorage(ShockCoilConfig.getMaxEnergy()) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                ShockCoilBlockEntity.this.setChanged();
                ShockCoilBlockEntity.this.updateBlockState();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                ShockCoilBlockEntity.this.setChanged();
                ShockCoilBlockEntity.this.updateBlockState();
            }
            return extracted;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    private int zapCooldown;
    private boolean redstoneDisabled;
    private TargetingMode targetingMode = TargetingMode.HOSTILE;

    public ShockCoilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOCK_COIL.get(), pos, state);
    }

    public void setRedstoneDisabled(boolean disabled) {
        this.redstoneDisabled = disabled;
        this.updateBlockState();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ShockCoilBlockEntity blockEntity) {
        blockEntity.updateTargetingMode();
        if (blockEntity.zapCooldown > 0) {
            blockEntity.zapCooldown--;
            return;
        }
        if (blockEntity.redstoneDisabled) {
            blockEntity.updateBlockState();
            return;
        }

        int energyPerZap = ShockCoilConfig.getEnergyPerZap();
        if (blockEntity.energyStorage.getEnergyStored() < energyPerZap) {
            blockEntity.updateBlockState();
            return;
        }

        Predicate<LivingEntity> targetPredicate = blockEntity.getTargetPredicate();
        List<LivingEntity> nearbyTargets = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(pos).inflate(ShockCoilConfig.getZapRange()),
                entity -> entity.isAlive() && !entity.isSpectator() && targetPredicate.test(entity)
        );
        if (nearbyTargets.isEmpty()) {
            return;
        }

        int targetsZapped = 0;
        Vec3 coilCenter = Vec3.atCenterOf(pos);
        for (LivingEntity target : nearbyTargets) {
            if (targetsZapped >= ShockCoilConfig.getMaxTargetsPerZap() || blockEntity.energyStorage.getEnergyStored() < energyPerZap) {
                break;
            }
            Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
            blockEntity.zapTarget(target, coilCenter, targetPos);
            blockEntity.energyStorage.extractEnergy(energyPerZap, false);
            targetsZapped++;
        }

        if (targetsZapped > 0) {
            blockEntity.zapCooldown = ShockCoilConfig.getZapCooldown();
            blockEntity.setChanged();
        }
    }

    private void updateTargetingMode() {
        if (this.level == null) {
            return;
        }

        boolean hasPlayerModule = false;
        boolean hasBaseModule = false;
        for (Direction direction : Direction.values()) {
            BlockState adjacentState = this.level.getBlockState(this.worldPosition.relative(direction));
            if (adjacentState.getBlock() instanceof PlayerTurretTargetingBlock) {
                hasPlayerModule = true;
            } else if (adjacentState.getBlock() instanceof HostileTurretTargetingBlock) {
                // Hostile is the default mode; presence is kept for parity with turret module behavior.
            } else if (adjacentState.getBlock() instanceof TurretTargetingBlock) {
                hasBaseModule = true;
            }
        }

        TargetingMode newMode = hasPlayerModule ? TargetingMode.PLAYER : hasBaseModule ? TargetingMode.ALL : TargetingMode.HOSTILE;
        if (this.targetingMode != newMode) {
            this.targetingMode = newMode;
            this.setChanged();
        }
    }

    private Predicate<LivingEntity> getTargetPredicate() {
        return switch (this.targetingMode) {
            case HOSTILE -> entity -> entity instanceof Monster;
            case PLAYER -> entity -> entity instanceof Player;
            case ALL -> entity -> true;
        };
    }

    private void zapTarget(LivingEntity target, Vec3 start, Vec3 end) {
        if (this.level instanceof ServerLevel serverLevel) {
            this.spawnLightningArc(serverLevel, start, end);
            SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.parse(ShockCoilConfig.getZapSound())).orElse(null);
            if (soundEvent != null) {
                serverLevel.playSound(null, this.worldPosition, soundEvent, SoundSource.BLOCKS, 0.8F, 1.0F);
            }

            target.hurt(this.level.damageSources().generic(), ShockCoilConfig.getBaseDamage());
            for (ShockCoilConfig.StatusEffect statusEffect : ShockCoilConfig.getStatusEffects()) {
                if (this.level.random.nextFloat() < statusEffect.chance()) {
                    target.addEffect(new MobEffectInstance(statusEffect.effect(), statusEffect.duration(), statusEffect.amplifier()));
                }
            }
        }
    }

    private void spawnLightningArc(ServerLevel serverLevel, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        direction = direction.normalize();
        for (double d = 0.0D; d < distance; d += 0.15D) {
            Vec3 particlePos = start.add(direction.scale(d));
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, particlePos.x, particlePos.y, particlePos.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
        }
    }

    private void updateBlockState() {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(ShockCoilBlock.POWERED)) {
                boolean powered = !this.redstoneDisabled && this.energyStorage.getEnergyStored() >= ShockCoilConfig.getEnergyPerZap();
                this.level.setBlock(this.worldPosition, state.setValue(ShockCoilBlock.POWERED, powered), 3);
            }
        }
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        return this.energyStorage;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Energy", this.energyStorage.serializeNBT(registries));
        tag.putInt("ZapCooldown", this.zapCooldown);
        tag.putBoolean("RedstoneDisabled", this.redstoneDisabled);
        tag.putString("TargetingMode", this.targetingMode.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.energyStorage.deserializeNBT(registries, tag.get("Energy"));
        this.zapCooldown = tag.getInt("ZapCooldown");
        this.redstoneDisabled = tag.getBoolean("RedstoneDisabled");
        if (tag.contains("TargetingMode")) {
            try {
                this.targetingMode = TargetingMode.valueOf(tag.getString("TargetingMode"));
            } catch (IllegalArgumentException ignored) {
                this.targetingMode = TargetingMode.HOSTILE;
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public void drops() {
        if (this.level != null) {
            Containers.dropContents(this.level, this.worldPosition, new SimpleContainer(0));
        }
    }

    public int getEnergy() {
        return this.energyStorage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return this.energyStorage.getMaxEnergyStored();
    }

    public TargetingMode getTargetingMode() {
        return this.targetingMode;
    }

    public enum TargetingMode {
        HOSTILE,
        PLAYER,
        ALL
    }
}
