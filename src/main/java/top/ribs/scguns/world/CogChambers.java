package top.ribs.scguns.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import top.ribs.scguns.init.ModStructures;

import java.util.Optional;

public class CogChambers extends Structure {
    public static final MapCodec<CogChambers> CODEC = RecordCodecBuilder.<CogChambers>mapCodec((builder) -> builder.group(
            settingsCodec(builder),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Heightmap.Types.CODEC.optionalFieldOf("heightmap").forGetter(structure -> structure.heightmap)
    ).apply(builder, CogChambers::new));

    public final Holder<StructureTemplatePool> startPool;
    public final HeightProvider startHeight;
    public final Optional<Heightmap.Types> heightmap;

    public CogChambers(Structure.StructureSettings config, Holder<StructureTemplatePool> pool, HeightProvider height, Optional<Heightmap.Types> map) {
        super(config);
        this.startPool = pool;
        this.startHeight = height;
        this.heightmap = map;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        BlockPos pos = new BlockPos(
                context.chunkPos().getMinBlockX(),
                this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor())),
                context.chunkPos().getMinBlockZ()
        );
        return JigsawPlacement.addPieces(context, this.startPool, Optional.empty(), 7, pos, false, this.heightmap, 128, PoolAliasLookup.EMPTY, DimensionPadding.ZERO, LiquidSettings.APPLY_WATERLOGGING);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CHAMBER.get();
    }
}
