package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.ribs.scguns.Reference;
import top.ribs.scguns.fluid.ViciousAcidFluid;
import top.ribs.scguns.fluid.ViciousAcidFluidType;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Reference.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Reference.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> VICIOUS_ACID_FLUID_TYPE = FLUID_TYPES.register("vicious_acid",
            () -> new ViciousAcidFluidType(
                    ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/vicious_acid_still"),
                    ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/vicious_acid_flow")));

    public static final DeferredHolder<Fluid, FlowingFluid> VICIOUS_ACID_SOURCE = FLUIDS.register("vicious_acid_source",
            () -> new ViciousAcidFluid.Source(viciousAcidProperties()));

    public static final DeferredHolder<Fluid, FlowingFluid> VICIOUS_ACID_FLOWING = FLUIDS.register("vicious_acid_flowing",
            () -> new ViciousAcidFluid.Flowing(viciousAcidProperties()));

    private static BaseFlowingFluid.Properties viciousAcidProperties() {
        return new BaseFlowingFluid.Properties(VICIOUS_ACID_FLUID_TYPE, VICIOUS_ACID_SOURCE, VICIOUS_ACID_FLOWING)
                .bucket(ModItems.VICIOUS_ACID_BUCKET)
                .block(ModBlocks.VICIOUS_ACID_BLOCK)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(5)
                .explosionResistance(100.0F);
    }
}
