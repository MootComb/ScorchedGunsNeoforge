package top.ribs.scguns.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

public class ViciousAcidFluidType extends FluidType {
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;

    public ViciousAcidFluidType(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(Properties.create()
                .density(1100)
                .viscosity(1200)
                .temperature(300)
                .canSwim(true)
                .canDrown(true)
                .canPushEntity(true)
                .supportsBoating(false));
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ViciousAcidFluidType.this.stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ViciousAcidFluidType.this.flowingTexture;
            }
        });
    }
}
