package top.ribs.scguns.blockentity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.Config;
import top.ribs.scguns.block.DamageModuleBlock;
import top.ribs.scguns.block.FireRateModuleBlock;
import top.ribs.scguns.block.HostileTurretTargetingBlock;
import top.ribs.scguns.block.PlayerTurretTargetingBlock;
import top.ribs.scguns.block.RangeModuleBlock;
import top.ribs.scguns.block.ShellCatcherModuleBlock;
import top.ribs.scguns.block.TurretTargetingBlock;
import top.ribs.scguns.common.Turret;
import top.ribs.scguns.common.TurretManager;
import top.ribs.scguns.entity.projectile.turret.TurretProjectileEntity;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.EnemyLogItem;
import top.ribs.scguns.item.TeamLogItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageGunSound;
import top.ribs.scguns.network.message.S2CMessageMuzzleFlash;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public abstract class TurretBlockEntity extends BlockEntity implements MenuProvider {
    private static final int DAMAGE_MODULE_BONUS = 2;
    private static final double RANGE_MODULE_BONUS = 8.0D;
    private static final int IDLE_BEFORE_SCAN = 60;
    private static final float SCAN_ANGLE = 60.0F;
    private static final float SCAN_SPEED = 0.02F;
    private static final float SCAN_PITCH = 0.0F;

    protected final ResourceLocation turretId;
    protected Turret config;
    protected double targetingRadius;
    protected int cooldown;
    public final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    protected LivingEntity target;
    protected UUID ownerUUID;
    protected String ownerName;
    protected float yaw;
    protected float pitch;
    protected float previousYaw;
    protected float previousPitch;
    protected double smoothedTargetX;
    protected double smoothedTargetY;
    protected double smoothedTargetZ;
    public float recoilPitchOffset = 0.0F;
    protected boolean hasFireRateModule;
    protected boolean hasDamageModule;
    protected boolean hasRangeModule;
    protected boolean hasShellCatchingModule;
    public boolean disabled = false;
    public int disableCooldown = 0;
    private int idleTicks = 0;
    private boolean isScanning = false;
    private boolean scanningRight = true;
    private float scanStartYaw = 0.0F;
    private boolean returningToScanPitch = false;

    protected TurretBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ResourceLocation turretId) {
        super(type, pos, state);
        this.turretId = turretId;
        this.reloadConfig();
    }

    public void reloadConfig() {
        this.config = TurretManager.getTurret(this.turretId);
        if (this.config != null) {
            this.targetingRadius = this.config.getTargeting().getRange();
            this.cooldown = Math.max(this.cooldown, 0);
        } else {
            this.targetingRadius = 12.0D;
            this.cooldown = Math.max(this.cooldown, 16);
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof TurretBlockEntity turret) {
            turret.tickTurret(level, pos, state);
        }
    }

    protected void tickTurret(Level level, BlockPos pos, BlockState state) {
        if (this.config == null) {
            this.reloadConfig();
            if (this.config == null) {
                return;
            }
        }

        this.hasFireRateModule = this.isAdjacentToFireRateModule(level, pos);
        this.hasDamageModule = this.isAdjacentToDamageModule(level, pos);
        this.hasRangeModule = this.isAdjacentToRangeModule(level, pos);
        this.hasShellCatchingModule = this.isAdjacentToShellCatchingModule();

        int fireRateModifier = this.hasFireRateModule ? 2 : 1;
        int damageModifier = this.hasDamageModule ? DAMAGE_MODULE_BONUS : 0;
        double rangeModifier = this.hasRangeModule ? RANGE_MODULE_BONUS : 0.0D;

        if (this.cooldown > 0) {
            this.cooldown -= fireRateModifier;
        }
        this.tickRecoil();

        if (this.disabled) {
            this.disableCooldown--;
            if (this.disableCooldown <= 0) {
                this.disabled = false;
                this.disableCooldown = 0;
            }
            this.resetToRestPosition();
            this.idleTicks = 0;
            this.isScanning = false;
            return;
        }

        if (this.isPowered(state)) {
            this.resetToRestPosition();
            this.idleTicks = 0;
            this.isScanning = false;
            this.returningToScanPitch = false;
            return;
        }

        this.updateTargetRange(rangeModifier);
        if (!this.isTargetValid()) {
            this.target = null;
        }
        this.findTarget(level, pos);

        if (this.target != null) {
            this.idleTicks = 0;
            this.isScanning = false;
            this.returningToScanPitch = false;
            this.updateYaw();
            this.updatePitch();

            if (this.cooldown <= 0 && this.isReadyToFire()) {
                this.fireWeapon(damageModifier);
            }
        } else {
            this.idleTicks++;
            if (this.idleTicks < IDLE_BEFORE_SCAN) {
                this.previousYaw = this.yaw;
                this.previousPitch = this.pitch;
            } else {
                this.updateScanningBehavior();
            }
        }
    }

    protected abstract boolean isPowered(BlockState state);

    public boolean hasTargetingModule() {
        if (this.level == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (this.level.getBlockState(this.worldPosition.relative(direction)).getBlock() instanceof TurretTargetingBlock) {
                return true;
            }
        }
        return false;
    }

    protected @Nullable net.minecraft.world.inventory.AbstractContainerMenu missingTargetingModule(Player player) {
        if (this.level != null && !this.level.isClientSide) {
            player.sendSystemMessage(Component.translatable("message.scguns.turret_needs_targeting_module").withStyle(ChatFormatting.YELLOW));
        }
        return null;
    }

    protected void updateScanningBehavior() {
        if (this.config == null) {
            return;
        }
        this.previousYaw = this.yaw;
        this.previousPitch = this.pitch;
        if (!this.returningToScanPitch) {
            float pitchDiff = SCAN_PITCH - this.pitch;
            if (Math.abs(pitchDiff) > 0.5F) {
                this.pitch += pitchDiff * this.config.getTargeting().getRotationSpeed();
                return;
            }
            this.pitch = SCAN_PITCH;
            this.returningToScanPitch = true;
        }

        this.pitch = SCAN_PITCH;
        if (!this.isScanning) {
            this.isScanning = true;
            this.scanStartYaw = this.yaw;
            this.scanningRight = true;
        }

        float targetYaw = this.scanningRight ? this.scanStartYaw + SCAN_ANGLE : this.scanStartYaw - SCAN_ANGLE;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.yaw);
        this.yaw += yawDiff * SCAN_SPEED;
        this.yaw %= 360.0F;
        if (this.yaw < 0.0F) {
            this.yaw += 360.0F;
        }

        float currentDiff = Math.abs(Mth.wrapDegrees(this.yaw - this.scanStartYaw));
        if (currentDiff >= SCAN_ANGLE - 1.0F) {
            this.scanningRight = !this.scanningRight;
        }
    }

    protected void fireWeapon(int damageModifier) {
        Turret.Ammunition.AmmoType ammoType = this.findAndConsumeAmmo();
        if (ammoType != null) {
            this.fire(ammoType, damageModifier);
            this.cooldown = this.config.getCombat().getCooldown();
        }
    }

    protected void fire(Turret.Ammunition.AmmoType ammoType, int damageModifier) {
        if (this.level == null || this.target == null || this.config == null) {
            return;
        }

        float yaw = this.getYaw();
        float pitch = this.getPitch();
        Vec3 muzzlePos = this.getMuzzlePosition(yaw, pitch);

        if (!this.level.isClientSide) {
            PacketHandler.getPlayChannel().sendToTrackingChunk(() -> level.getChunkAt(worldPosition), new S2CMessageMuzzleFlash(muzzlePos, yaw, pitch));
        }

        this.playFireSound(muzzlePos);

        Vec3 targetPos = new Vec3(this.target.getX(), this.target.getY() + this.target.getEyeHeight() * 0.5, this.target.getZ());
        Vec3 direction = targetPos.subtract(muzzlePos).normalize();
        float inaccuracy = this.config.getCombat().getInaccuracy();
        if (inaccuracy > 0.0F) {
            direction = direction.add(
                    this.level.random.triangle(0.0D, inaccuracy),
                    this.level.random.triangle(0.0D, inaccuracy),
                    this.level.random.triangle(0.0D, inaccuracy)
            ).normalize();
        }

        int pelletCount = Math.max(1, this.config.getCombat().getPelletCount());
        if (pelletCount > 1) {
            this.fireCluster(ammoType, muzzlePos, direction, damageModifier, pelletCount);
        } else {
            this.fireSingleProjectile(ammoType, muzzlePos, direction, damageModifier);
        }

        this.recoilPitchOffset = this.config.getCombat().getRecoilMax();
        this.handleCasingEjection(ammoType);
    }

    private void playFireSound(Vec3 muzzlePos) {
        if (!(this.level instanceof ServerLevel serverLevel) || this.config == null || this.config.getCombat().getFireSound() == null) {
            return;
        }

        S2CMessageGunSound message = new S2CMessageGunSound(
                this.config.getCombat().getFireSound(),
                SoundSource.BLOCKS,
                (float) muzzlePos.x,
                (float) muzzlePos.y,
                (float) muzzlePos.z,
                0.7F,
                0.7F,
                -1,
                false,
                false
        );
        double radius = Config.SERVER.gunShotMaxDistance.get();
        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(serverLevel, muzzlePos.x, muzzlePos.y, muzzlePos.z, radius),
                message
        );
    }

    protected void fireSingleProjectile(Turret.Ammunition.AmmoType ammoType, Vec3 muzzlePos, Vec3 direction, int damageModifier) {
        TurretProjectileEntity projectile = new TurretProjectileEntity(this.level);
        projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);
        projectile.shoot(direction.x, direction.y, direction.z, (float) this.config.getCombat().getProjectileSpeed(), 0.0F);
        projectile.setBaseDamage(this.getScaledDamage(ammoType.getDamage()) + damageModifier);
        this.level.addFreshEntity(projectile);
    }

    protected void fireCluster(Turret.Ammunition.AmmoType ammoType, Vec3 muzzlePos, Vec3 baseDirection, int damageModifier, int pelletCount) {
        double finalDamage = this.getScaledDamage(ammoType.getDamage()) + damageModifier;
        double pelletDamage = finalDamage / pelletCount;

        for (int i = 0; i < pelletCount; i++) {
            Vec3 spreadDirection = this.applySpread(baseDirection, this.config.getCombat().getSpreadAngle());
            TurretProjectileEntity projectile = new TurretProjectileEntity(this.level);
            projectile.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);
            projectile.shoot(spreadDirection.x, spreadDirection.y, spreadDirection.z, (float) this.config.getCombat().getProjectileSpeed(), 0.0F);
            projectile.setBaseDamage(pelletDamage);
            this.level.addFreshEntity(projectile);
        }
    }

    protected Vec3 applySpread(Vec3 baseDirection, float spreadAngle) {
        double yawRad = Math.toRadians(this.level.random.triangle(0.0D, spreadAngle));
        double pitchRad = Math.toRadians(this.level.random.triangle(0.0D, spreadAngle));
        double x = baseDirection.x;
        double y = baseDirection.y;
        double z = baseDirection.z;
        double tempX = x * Math.cos(yawRad) - z * Math.sin(yawRad);
        double tempZ = x * Math.sin(yawRad) + z * Math.cos(yawRad);
        double tempY = y * Math.cos(pitchRad) - tempZ * Math.sin(pitchRad);
        tempZ = y * Math.sin(pitchRad) + tempZ * Math.cos(pitchRad);
        return new Vec3(tempX, tempY, tempZ).normalize();
    }

    private double getScaledDamage(double baseDamage) {
        if (this.level != null && Config.COMMON.turret.enableDamageScaling.get()) {
            long daysInWorld = this.level.getDayTime() / 24000L;
            double scalingRate = Config.COMMON.turret.damageScalingRate.get();
            double maxDamage = Config.COMMON.turret.maxScaledDamage.get();
            return Math.min(baseDamage + (scalingRate * daysInWorld), maxDamage);
        }
        return baseDamage;
    }

    protected void handleCasingEjection(Turret.Ammunition.AmmoType ammoType) {
        if (this.hasShellCatchingModule) {
            boolean inserted = this.tryInsertIntoShellCatcher(ammoType);
            if (!inserted) {
                this.spawnCasing(ammoType);
            }
        } else if (this.level != null && this.level.random.nextFloat() < this.config.getAmmunition().getCasingEjectChance()) {
            this.spawnCasing(ammoType);
        }
    }

    protected void spawnCasing(Turret.Ammunition.AmmoType ammoType) {
        if (this.level == null || ammoType.getCasingType() == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(ammoType.getCasingType());
        if (item == Items.AIR) {
            return;
        }
        ItemEntity casingEntity = new ItemEntity(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5, new ItemStack(item));
        casingEntity.setDeltaMovement(Direction.NORTH.getStepX() * 0.1D, 0.15D, Direction.NORTH.getStepZ() * 0.1D);
        this.level.addFreshEntity(casingEntity);
    }

    protected boolean tryInsertIntoShellCatcher(Turret.Ammunition.AmmoType ammoType) {
        if (this.level == null || ammoType.getCasingType() == null) {
            return false;
        }
        Item item = BuiltInRegistries.ITEM.get(ammoType.getCasingType());
        if (item == Items.AIR) {
            return false;
        }
        ItemStack casingStack = new ItemStack(item);
        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.worldPosition.relative(direction));
            if (blockEntity instanceof ShellCatcherModuleBlockEntity shellCatcher) {
                for (int i = 0; i < shellCatcher.getContainerSize(); i++) {
                    ItemStack existingStack = shellCatcher.getItemStackHandler().getStackInSlot(i);
                    if (existingStack.isEmpty()) {
                        shellCatcher.getItemStackHandler().setStackInSlot(i, casingStack);
                        return true;
                    }
                    if (ItemStack.isSameItemSameComponents(existingStack, casingStack) && existingStack.getCount() < existingStack.getMaxStackSize()) {
                        existingStack.grow(1);
                        shellCatcher.getItemStackHandler().setStackInSlot(i, existingStack);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected Vec3 getMuzzlePosition(float yaw, float pitch) {
        double muzzleLength = this.config.getDisplay().getMuzzleLength();
        double muzzleOffsetY = this.config.getDisplay().getMuzzleOffsetY();
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double muzzleX = -Math.sin(yawRad) * Math.cos(pitchRad) * muzzleLength;
        double muzzleY = Math.sin(pitchRad) * muzzleLength + muzzleOffsetY;
        double muzzleZ = -Math.cos(yawRad) * Math.cos(pitchRad) * muzzleLength;
        return new Vec3(this.worldPosition.getX() + 0.5 + muzzleX, this.worldPosition.getY() + muzzleY, this.worldPosition.getZ() + 0.5 + muzzleZ);
    }

    protected void updateTargetRange(double rangeModifier) {
        this.targetingRadius = this.config.getTargeting().getRange() + rangeModifier;
    }

    public boolean isReadyToFire() {
        if (this.target == null || this.config == null) {
            return false;
        }
        double dx = this.smoothedTargetX - (this.worldPosition.getX() + 0.5);
        double dy = this.smoothedTargetY - (this.worldPosition.getY() + 1.0);
        double dz = this.smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Math.atan2(dx, dz) * (180.0D / Math.PI)) + 180.0F;
        targetYaw = (targetYaw + 360.0F) % 360.0F;
        float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180.0D / Math.PI));
        targetPitch = Mth.clamp(targetPitch, this.config.getTargeting().getMinPitch(), this.config.getTargeting().getMaxPitch());
        float yawDifference = Math.abs(targetYaw - this.yaw);
        if (yawDifference > 180.0F) {
            yawDifference = 360.0F - yawDifference;
        }
        float pitchDifference = Math.abs(targetPitch - this.pitch);
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double minDistance = this.config.getTargeting().getMinFiringDistance();
        return distanceSquared >= minDistance * minDistance && yawDifference < 2.0F && pitchDifference < 2.0F;
    }

    public void tickRecoil() {
        if (this.config != null && this.recoilPitchOffset > 0.0F) {
            this.recoilPitchOffset -= this.config.getCombat().getRecoilSpeed();
            if (this.recoilPitchOffset < 0.0F) {
                this.recoilPitchOffset = 0.0F;
            }
        }
    }

    public void resetToRestPosition() {
        if (this.config == null) {
            return;
        }
        this.target = null;
        this.previousYaw = this.yaw;
        this.previousPitch = this.pitch;
        float yawDifference = Mth.wrapDegrees(this.config.getBehavior().getRestingYaw() - this.yaw);
        this.yaw += yawDifference * this.config.getTargeting().getRotationSpeed();
        this.yaw %= 360.0F;
        if (this.yaw < 0.0F) {
            this.yaw += 360.0F;
        }
        float pitchDifference = this.config.getBehavior().getRestingPitch() - this.pitch;
        this.pitch += pitchDifference * this.config.getTargeting().getRotationSpeed();
        this.smoothedTargetX = 0.0D;
        this.smoothedTargetY = 0.0D;
        this.smoothedTargetZ = 0.0D;
    }

    public void onHitByLightningProjectile() {
        this.disabled = true;
        this.disableCooldown = this.config != null ? this.config.getBehavior().getDisableTime() : 200;
        this.resetToRestPosition();
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.spawnDisableParticles();
        }
    }

    protected void spawnDisableParticles() {
        if (this.level instanceof ServerLevel serverLevel) {
            double x = this.worldPosition.getX() + 0.5D;
            double y = this.worldPosition.getY() + 1.0D;
            double z = this.worldPosition.getZ() + 0.5D;
            for (int i = 0; i < 20; i++) {
                double offsetX = this.level.random.nextDouble() * 0.5D - 0.25D;
                double offsetY = this.level.random.nextDouble() * 0.5D;
                double offsetZ = this.level.random.nextDouble() * 0.5D - 0.25D;
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x + offsetX, y + offsetY, z + offsetZ, 1, 0.0D, 0.0D, 0.0D, 0.05D);
            }
            serverLevel.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Nullable
    protected Turret.Ammunition.AmmoType findAndConsumeAmmo() {
        if (this.config == null) {
            return null;
        }
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            ItemStack stack = this.itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            for (Turret.Ammunition.AmmoType ammoType : this.config.getAmmunition().getAcceptedAmmo()) {
                Item item = ammoType.getItem();
                if (item != null && stack.getItem() == item) {
                    this.consumeAmmo(i);
                    return ammoType;
                }
            }
        }
        return null;
    }

    protected void consumeAmmo(int slot) {
        ItemStack stack = this.itemHandler.getStackInSlot(slot);
        stack.shrink(1);
        if (stack.isEmpty()) {
            this.itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    protected void findTarget(Level level, BlockPos pos) {
        if (this.config == null) {
            return;
        }
        this.target = null;
        boolean hasTargetingModule = false;
        boolean isPlayerTargetingModule = false;
        boolean isHostileTargetingModule = false;

        for (Direction direction : Direction.values()) {
            BlockState blockState = level.getBlockState(pos.relative(direction));
            if (blockState.getBlock() instanceof TurretTargetingBlock) {
                hasTargetingModule = true;
                if (blockState.getBlock() instanceof PlayerTurretTargetingBlock) {
                    isPlayerTargetingModule = true;
                } else if (blockState.getBlock() instanceof HostileTurretTargetingBlock) {
                    isHostileTargetingModule = true;
                }
                break;
            }
        }

        if (!hasTargetingModule) {
            return;
        }

        ItemStack logStack = this.itemHandler.getStackInSlot(9);
        boolean hasTeamLog = logStack.getItem() instanceof TeamLogItem && !(logStack.getItem() instanceof EnemyLogItem);
        boolean hasEnemyLog = logStack.getItem() instanceof EnemyLogItem;
        List<UUID> loggedEntityUUIDs = new ArrayList<>();
        List<String> blacklistedEntityTypes = new ArrayList<>();
        List<UUID> whitelistedEntityUUIDs = new ArrayList<>();
        List<String> whitelistedEntityTypes = new ArrayList<>();

        if (hasTeamLog || hasEnemyLog) {
            CompoundTag tag = logStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (!tag.isEmpty()) {
                if (hasTeamLog) {
                    if (tag.contains("Entities", Tag.TAG_LIST)) {
                        ListTag listTag = tag.getList("Entities", Tag.TAG_COMPOUND);
                        for (int i = 0; i < listTag.size(); i++) {
                            loggedEntityUUIDs.add(listTag.getCompound(i).getUUID("UUID"));
                        }
                    }
                    if (tag.contains("Blacklist", Tag.TAG_LIST)) {
                        ListTag blacklistTag = tag.getList("Blacklist", Tag.TAG_STRING);
                        for (int i = 0; i < blacklistTag.size(); i++) {
                            blacklistedEntityTypes.add(blacklistTag.getString(i));
                        }
                    }
                } else {
                    if (tag.contains("Whitelist", Tag.TAG_LIST)) {
                        ListTag listTag = tag.getList("Whitelist", Tag.TAG_COMPOUND);
                        for (int i = 0; i < listTag.size(); i++) {
                            whitelistedEntityUUIDs.add(listTag.getCompound(i).getUUID("UUID"));
                        }
                    }
                    if (tag.contains("WhitelistEntityTypes", Tag.TAG_LIST)) {
                        ListTag whitelistTag = tag.getList("WhitelistEntityTypes", Tag.TAG_STRING);
                        for (int i = 0; i < whitelistTag.size(); i++) {
                            whitelistedEntityTypes.add(whitelistTag.getString(i));
                        }
                    }
                }
            }
        }

        Vec3 turretPos = new Vec3(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        AABB searchBox = new AABB(pos).inflate(this.targetingRadius, this.config.getTargeting().getVerticalRange(), this.targetingRadius);
        boolean playerTargeting = isPlayerTargetingModule;
        boolean hostileTargeting = isHostileTargetingModule;

        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != null
                        && entity.isAlive()
                        && !this.isOwner(entity)
                        && ((!hasTeamLog && !hasEnemyLog)
                        || (hasTeamLog && !loggedEntityUUIDs.contains(entity.getUUID()) && !blacklistedEntityTypes.contains(EntityType.getKey(entity.getType()).toString()))
                        || (hasEnemyLog && (whitelistedEntityUUIDs.contains(entity.getUUID()) || whitelistedEntityTypes.contains(EntityType.getKey(entity.getType()).toString()))))
                        && !(entity instanceof EnderMan)
                        && (!entity.isInvisible() || this.hasRangeModule)
                        && (!playerTargeting || (entity instanceof Player player && !player.isCreative()))
                        && (!hostileTargeting || entity.getType().getCategory() == MobCategory.MONSTER || entity.getType().is(ModTags.Entities.TURRET_ENEMY_WHITELIST))
                        && !entity.getType().is(ModTags.Entities.TURRET_BLACKLIST));

        if (potentialTargets.isEmpty()) {
            return;
        }

        if (this.config.getTargeting().requiresLineOfSight()) {
            this.target = potentialTargets.stream()
                    .filter(entity -> this.hasLineOfSight(level, turretPos, entity))
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(turretPos)))
                    .orElse(null);
        } else {
            this.target = potentialTargets.stream()
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(turretPos)))
                    .orElse(null);
        }

        if (this.target != null) {
            int predictionMultiplier = this.config.getTargeting().getPredictionMultiplier();
            double predictedX = this.target.getX() + this.target.getDeltaMovement().x * predictionMultiplier;
            double predictedY = this.target.getY() + (this.target.getBbHeight() / 2.0F);
            double predictedZ = this.target.getZ() + this.target.getDeltaMovement().z * predictionMultiplier;
            float smoothing = this.config.getTargeting().getPositionSmoothing();
            this.smoothedTargetX = lerp(this.smoothedTargetX, predictedX, smoothing);
            this.smoothedTargetY = lerp(this.smoothedTargetY, predictedY, smoothing);
            this.smoothedTargetZ = lerp(this.smoothedTargetZ, predictedZ, smoothing);
        }
    }

    protected boolean hasLineOfSight(Level level, Vec3 turretPos, LivingEntity target) {
        Vec3 targetPos = target.getEyePosition();
        Vec3 toTarget = targetPos.subtract(turretPos);
        Vec3 adjustedTurretPos = turretPos.add(0.0D, 0.5D, 0.0D);
        ClipContext clipContext = new ClipContext(adjustedTurretPos, adjustedTurretPos.add(toTarget), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        BlockHitResult hitResult = level.clip(clipContext);
        return hitResult.getType() == HitResult.Type.MISS;
    }

    protected boolean isTargetValid() {
        if (this.target == null || !this.target.isAlive() || this.target.isRemoved() || this.level == null) {
            return false;
        }
        ChunkPos targetChunkPos = new ChunkPos(this.target.blockPosition());
        if (!this.level.hasChunk(targetChunkPos.x, targetChunkPos.z)) {
            return false;
        }
        double distanceSquared = this.target.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D);
        return distanceSquared <= this.targetingRadius * this.targetingRadius;
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    protected void updateYaw() {
        this.previousYaw = this.yaw;
        if (this.config != null && (this.smoothedTargetX != 0.0D || this.smoothedTargetZ != 0.0D)) {
            double dx = this.smoothedTargetX - (this.worldPosition.getX() + 0.5D);
            double dz = this.smoothedTargetZ - (this.worldPosition.getZ() + 0.5D);
            float targetYaw = (float) (Math.atan2(dx, dz) * (180.0D / Math.PI)) + 180.0F;
            targetYaw = (targetYaw + 360.0F) % 360.0F;
            this.yaw = (this.yaw + 360.0F) % 360.0F;
            this.yaw += Mth.wrapDegrees(targetYaw - this.yaw) * this.config.getTargeting().getRotationSpeed();
            this.yaw %= 360.0F;
            if (this.yaw < 0.0F) {
                this.yaw += 360.0F;
            }
        }
    }

    protected void updatePitch() {
        this.previousPitch = this.pitch;
        if (this.config != null && this.smoothedTargetY != 0.0D) {
            double dx = this.smoothedTargetX - (this.worldPosition.getX() + 0.5D);
            double dy = this.smoothedTargetY - (this.worldPosition.getY() + 1.0D);
            double dz = this.smoothedTargetZ - (this.worldPosition.getZ() + 0.5D);
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180.0D / Math.PI));
            targetPitch = Mth.clamp(targetPitch, this.config.getTargeting().getMinPitch(), this.config.getTargeting().getMaxPitch());
            this.pitch += (targetPitch - this.pitch) * this.config.getTargeting().getRotationSpeed();
        }
    }

    private boolean isAdjacentToFireRateModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (world.getBlockState(pos.relative(direction)).getBlock() instanceof FireRateModuleBlock) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdjacentToDamageModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (world.getBlockState(pos.relative(direction)).getBlock() instanceof DamageModuleBlock) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdjacentToRangeModule(BlockGetter world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (world.getBlockState(pos.relative(direction)).getBlock() instanceof RangeModuleBlock) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdjacentToShellCatchingModule() {
        if (this.level == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (this.level.getBlockState(this.worldPosition.relative(direction)).getBlock() instanceof ShellCatcherModuleBlock) {
                return true;
            }
        }
        return false;
    }

    private boolean isOwner(LivingEntity entity) {
        return this.ownerUUID != null && entity.getUUID().equals(this.ownerUUID);
    }

    public float getPreviousYaw() {
        return this.previousYaw;
    }

    public float getPreviousPitch() {
        return this.previousPitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getRecoilPitchOffset() {
        return this.recoilPitchOffset;
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        if (this.level != null) {
            Containers.dropContents(this.level, this.worldPosition, inventory);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", this.itemHandler.serializeNBT(registries));
        tag.putFloat("Yaw", this.yaw);
        tag.putFloat("Pitch", this.pitch);
        tag.putBoolean("Disabled", this.disabled);
        tag.putInt("DisableCooldown", this.disableCooldown);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
            tag.putString("OwnerName", this.ownerName == null ? "" : this.ownerName);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.yaw = tag.getFloat("Yaw");
        this.previousYaw = this.yaw;
        this.pitch = tag.getFloat("Pitch");
        this.previousPitch = this.pitch;
        this.disabled = tag.getBoolean("Disabled");
        this.disableCooldown = tag.getInt("DisableCooldown");
        if (tag.contains("Inventory", Tag.TAG_COMPOUND)) {
            this.itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
            this.ownerName = tag.getString("OwnerName");
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

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    public SimpleContainer getContainer() {
        SimpleContainer container = new SimpleContainer(10);
        for (int i = 0; i < 10; i++) {
            container.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        return container;
    }

    public ItemStackHandler getItemStackHandler() {
        return this.itemHandler;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void setOwner(ServerPlayer player) {
        this.ownerUUID = player.getUUID();
        this.ownerName = player.getName().getString();
    }

    public String getOwnerName() {
        return this.ownerName == null ? "" : this.ownerName;
    }
}
