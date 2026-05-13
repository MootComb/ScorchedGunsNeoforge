package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Optional;

public class AirCanisterItem extends Item {
    private static final String AIR_TAG = "AirStored";
    private static final int AIR_PER_USE = 50;
    private static final int USE_COOLDOWN = 10;

    private final int capacity;

    public AirCanisterItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this) || getAirStored(stack) >= getMaxAirStored(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.getFoodData().getFoodLevel() < 1 && !player.getAbilities().instabuild) {
            return InteractionResultHolder.pass(stack);
        }

        IEnergyStorage airStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        int airAdded = airStorage != null ? airStorage.receiveEnergy(AIR_PER_USE, false) : 0;
        if (airAdded <= 0) {
            return InteractionResultHolder.pass(stack);
        }

        if (!player.getAbilities().instabuild && !level.isClientSide) {
            player.causeFoodExhaustion(0.5F);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.5F, 1.2F);
        player.getCooldowns().addCooldown(this, USE_COOLDOWN);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxAir = getMaxAirStored(stack);
        return maxAir <= 0 ? 0 : Math.round(13.0F * getAirStored(stack) / maxAir);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return getAirBarColor(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("info.scguns.air_stored")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(getAirStored(stack) + " / " + getMaxAirStored(stack)).withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.translatable("info.scguns.air_canister.usage").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_COOLDOWN;
    }

    public static Optional<ItemStack> findFirstWithAir(Player player) {
        return findFirstWithAir(player, 1);
    }

    public static Optional<ItemStack> findFirstWithAir(Player player, int minAir) {
        if (player == null) {
            return Optional.empty();
        }

        for (ItemStack stack : player.getInventory().items) {
            if (hasAtLeastAir(stack, minAir)) {
                return Optional.of(stack);
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (hasAtLeastAir(stack, minAir)) {
                return Optional.of(stack);
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (hasAtLeastAir(stack, minAir)) {
                return Optional.of(stack);
            }
        }

        final ItemStack[] found = {ItemStack.EMPTY};
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (hasAtLeastAir(stack, minAir)) {
                    found[0] = stack;
                    return;
                }
            }
        });
        return found[0].isEmpty() ? Optional.empty() : Optional.of(found[0]);
    }

    public static boolean consumeAir(Player player, int amount) {
        if (amount <= 0) {
            return true;
        }
        Optional<ItemStack> canister = findFirstWithAir(player, amount);
        if (canister.isEmpty()) {
            return false;
        }
        IEnergyStorage storage = canister.get().getCapability(Capabilities.EnergyStorage.ITEM);
        return storage != null && storage.extractEnergy(amount, false) >= amount;
    }

    public static int getAirStored(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return storage != null ? storage.getEnergyStored() : 0;
    }

    public static int getMaxAirStored(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (storage != null) {
            return storage.getMaxEnergyStored();
        }
        return stack.getItem() instanceof AirCanisterItem canister ? canister.getCapacity() : 0;
    }

    public static int getAirBarColor(ItemStack stack) {
        int maxAir = getMaxAirStored(stack);
        if (maxAir <= 0) {
            return 0x808080;
        }
        float ratio = (float) getAirStored(stack) / (float) maxAir;
        if (ratio < 0.25F) {
            return 0xFF4A44;
        }
        return ratio < 0.5F ? 0xFFAA00 : Mth.hsvToRgb(0.55F, 1.0F, 1.0F);
    }

    private static boolean hasAtLeastAir(ItemStack stack, int minAir) {
        return !stack.isEmpty() && stack.getItem() instanceof AirCanisterItem && getAirStored(stack) >= minAir;
    }

    public static class AirStorage implements IEnergyStorage {
        private final ItemStack stack;
        private final int capacity;
        private int air;

        public AirStorage(ItemStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
            this.air = loadAirFromData();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int airReceived = Math.min(this.capacity - this.air, maxReceive);
            if (!simulate && airReceived > 0) {
                this.air += airReceived;
                updateAirTag();
            }
            return airReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int airExtracted = Math.min(this.air, maxExtract);
            if (!simulate && airExtracted > 0) {
                this.air -= airExtracted;
                updateAirTag();
            }
            return airExtracted;
        }

        @Override
        public int getEnergyStored() {
            return this.air;
        }

        @Override
        public int getMaxEnergyStored() {
            return this.capacity;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        private void updateAirTag() {
            CustomData.update(DataComponents.CUSTOM_DATA, this.stack, tag -> tag.putInt(AIR_TAG, this.air));
        }

        private int loadAirFromData() {
            CustomData customData = this.stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) {
                return 0;
            }
            CompoundTag tag = customData.copyTag();
            return tag.contains(AIR_TAG, Tag.TAG_INT) ? tag.getInt(AIR_TAG) : 0;
        }
    }
}
