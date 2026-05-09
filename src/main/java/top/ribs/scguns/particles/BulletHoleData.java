package top.ribs.scguns.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import top.ribs.scguns.init.ModParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

/**
 * Author: MrCrayfish
 */
public class BulletHoleData implements ParticleOptions
{
    public static final MapCodec<BulletHoleData> CODEC = RecordCodecBuilder.mapCodec((builder) -> {
        return builder.group(Codec.INT.fieldOf("dir").forGetter((data) -> {
            return data.direction.ordinal();
        }), Codec.LONG.fieldOf("pos").forGetter((p_239806_0_) -> {
            return p_239806_0_.pos.asLong();
        })).apply(builder, BulletHoleData::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, BulletHoleData> STREAM_CODEC = StreamCodec.ofMember(BulletHoleData::writeToNetwork, BulletHoleData::readFromNetwork);

    private final Direction direction;
    private final BlockPos pos;

    public BulletHoleData(int dir, long pos)
    {
        this.direction = Direction.values()[dir];
        this.pos = BlockPos.of(pos);
    }

    public BulletHoleData(Direction dir, BlockPos pos)
    {
        this.direction = dir;
        this.pos = pos;
    }

    public Direction getDirection()
    {
        return this.direction;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    @Override
    public ParticleType<?> getType()
    {
        return ModParticleTypes.BULLET_HOLE.get();
    }

    public static BulletHoleData readFromNetwork(RegistryFriendlyByteBuf buffer)
    {
        return new BulletHoleData(buffer.readEnum(Direction.class), buffer.readBlockPos());
    }

    public void writeToNetwork(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeEnum(this.direction);
        buffer.writeBlockPos(this.pos);
    }

    public String writeToString()
    {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.direction.getName();
    }

    public static MapCodec<BulletHoleData> codec(ParticleType<BulletHoleData> type)
    {
        return CODEC;
    }
}
