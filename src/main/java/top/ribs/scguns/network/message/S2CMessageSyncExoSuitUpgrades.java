package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import top.ribs.scguns.client.network.ClientPlayHandler;

import java.util.UUID;

public class S2CMessageSyncExoSuitUpgrades
{
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageSyncExoSuitUpgrades> STREAM_CODEC = StreamCodec.of(
            S2CMessageSyncExoSuitUpgrades::encode,
            S2CMessageSyncExoSuitUpgrades::decode
    );


    private UUID playerId;
    private EquipmentSlot armorSlot;
    private CompoundTag upgradeData;

    public S2CMessageSyncExoSuitUpgrades() {}

    public S2CMessageSyncExoSuitUpgrades(UUID playerId, EquipmentSlot armorSlot, CompoundTag upgradeData) {
        this.playerId = playerId;
        this.armorSlot = armorSlot;
        this.upgradeData = upgradeData;
    }
    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageSyncExoSuitUpgrades message) {
        buffer.writeUUID(message.playerId);
        buffer.writeEnum(message.armorSlot);
        buffer.writeNbt(message.upgradeData);
    }
    private static S2CMessageSyncExoSuitUpgrades decode(RegistryFriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        EquipmentSlot armorSlot = buffer.readEnum(EquipmentSlot.class);
        CompoundTag upgradeData = buffer.readNbt();
        return new S2CMessageSyncExoSuitUpgrades(playerId, armorSlot, upgradeData);
    }
    public static void handle(S2CMessageSyncExoSuitUpgrades message, MessageContext context) {
        context.execute(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientPlayHandler.handleSyncExoSuitUpgrades(message);
            }
        });
        context.setHandled(true);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public EquipmentSlot getArmorSlot() {
        return armorSlot;
    }

    public CompoundTag getUpgradeData() {
        return upgradeData;
    }
}
