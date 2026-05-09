package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadTracker;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.network.PacketHandler;

public class C2SMessageGunLoaded
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageGunLoaded> STREAM_CODEC = StreamCodec.of(
            C2SMessageGunLoaded::encode,
            C2SMessageGunLoaded::decode
    );

    public C2SMessageGunLoaded() {}

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageGunLoaded message) {}

    private static C2SMessageGunLoaded decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageGunLoaded();
    }
    public static void handle(C2SMessageGunLoaded message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if (player != null && !player.isSpectator()) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof GunItem) {
                    if (!heldItem.getItem().getClass().getPackageName().startsWith("top.ribs.scguns")) {
                        return;
                    }
                    Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                    ReloadTracker tracker = new ReloadTracker(player);

                    if (gun.getReloads().getReloadType() == ReloadType.MAG_FED) {
                        tracker.increaseMagAmmo(player);
                        CompoundTag tag = getCustomData(heldItem);
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        tag.remove("IsReloading");
                        tag.remove("scguns:IsReloading");
                        tag.remove("InCriticalReloadPhase");
                        setCustomData(heldItem, tag);

                        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                                () -> LevelLocation.create(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 64),
                                new S2CMessageUpdateAmmo(tag.getInt("AmmoCount"))
                        );

                        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                                () -> LevelLocation.create(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 64),
                                new S2CMessageReload(false)
                        );

                    } else if (gun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
                        tracker.reloadItem(player);
                        CompoundTag tag = getCustomData(heldItem);
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        tag.remove("IsReloading");
                        tag.remove("scguns:IsReloading");
                        tag.remove("InCriticalReloadPhase");
                        setCustomData(heldItem, tag);

                        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                                () -> LevelLocation.create(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 64),
                                new S2CMessageUpdateAmmo(tag.getInt("AmmoCount"))
                        );
                        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                                () -> LevelLocation.create(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 64),
                                new S2CMessageReload(false)
                        );

                    } else if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                        tracker.increaseAmmo(player);
                        CompoundTag tag = getCustomData(heldItem);

                        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                                () -> LevelLocation.create(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 64),
                                new S2CMessageUpdateAmmo(tag.getInt("AmmoCount"))
                        );

                        boolean weaponFull = tracker.isWeaponFull(player);
                        boolean hasNoAmmo = tracker.hasNoAmmo(player);


                        if (weaponFull || hasNoAmmo) {
                            ModSyncedDataKeys.RELOADING.setValue(player, false);
                            tag.remove("IsReloading");
                            tag.remove("scguns:IsReloading");

                            if (player.getMainHandItem().getItem() instanceof AnimatedGunItem) {
                                tag.putString("scguns:ReloadState", "STOPPING");
                                tag.putBoolean("scguns:IsPlayingReloadStop", true);
                                PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageStopReload());
                            }
                            setCustomData(heldItem, tag);

                            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                                    () -> LevelLocation.create(player.serverLevel(), player.getX(), player.getY(), player.getZ(), 64),
                                    new S2CMessageReload(false)
                            );
                        } else {
                            setCustomData(heldItem, tag);
                        }
                    }
                }
            }
        });
        context.setHandled(true);
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
