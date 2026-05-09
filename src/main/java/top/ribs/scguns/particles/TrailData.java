package top.ribs.scguns.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import top.ribs.scguns.init.ModParticleTypes;

/**
 * Author: MrCrayfish
 */
public class TrailData implements ParticleOptions
{
    public static final MapCodec<TrailData> CODEC = RecordCodecBuilder.mapCodec((builder) -> {
        return builder.group(Codec.BOOL.fieldOf("enchanted").forGetter((data) -> {
            return data.enchanted;
        })).apply(builder, TrailData::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, TrailData> STREAM_CODEC = StreamCodec.ofMember(TrailData::writeToNetwork, TrailData::readFromNetwork);

    private final boolean enchanted;

    public TrailData(boolean enchanted)
    {
        this.enchanted = enchanted;
    }

    public boolean isEnchanted()
    {
        return this.enchanted;
    }

    @Override
    public ParticleType<?> getType()
    {
        return ModParticleTypes.TRAIL.get();
    }

    public static TrailData readFromNetwork(RegistryFriendlyByteBuf buffer)
    {
        return new TrailData(buffer.readBoolean());
    }

    public void writeToNetwork(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.enchanted);
    }

    public String writeToString()
    {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.enchanted;
    }
}
