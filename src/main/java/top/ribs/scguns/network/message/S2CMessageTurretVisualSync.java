package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BlockEntity;
import top.ribs.scguns.blockentity.TurretBlockEntity;

public class S2CMessageTurretVisualSync {
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageTurretVisualSync> STREAM_CODEC = StreamCodec.of(
            S2CMessageTurretVisualSync::encode,
            S2CMessageTurretVisualSync::decode
    );

    private final BlockPos pos;
    private final float yaw;
    private final float pitch;
    private final float recoilPitchOffset;

    public S2CMessageTurretVisualSync(BlockPos pos, float yaw, float pitch, float recoilPitchOffset) {
        this.pos = pos;
        this.yaw = yaw;
        this.pitch = pitch;
        this.recoilPitchOffset = recoilPitchOffset;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, S2CMessageTurretVisualSync message) {
        buffer.writeBlockPos(message.pos);
        buffer.writeFloat(message.yaw);
        buffer.writeFloat(message.pitch);
        buffer.writeFloat(message.recoilPitchOffset);
    }

    private static S2CMessageTurretVisualSync decode(RegistryFriendlyByteBuf buffer) {
        return new S2CMessageTurretVisualSync(
                buffer.readBlockPos(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }

    public static void handle(S2CMessageTurretVisualSync message, MessageContext context) {
        context.execute(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null || !level.hasChunkAt(message.pos)) {
                return;
            }
            BlockEntity blockEntity = level.getBlockEntity(message.pos);
            if (blockEntity instanceof TurretBlockEntity turret) {
                turret.applyRemoteVisualState(message.yaw, message.pitch, message.recoilPitchOffset);
            }
        });
        context.setHandled(true);
    }
}
