package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.entity.monster.ViventrumEntity;

public class ViventrumRenderer extends MobRenderer<ViventrumEntity, ViventrumModel<ViventrumEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("scguns", "textures/entity/viventrum.png");

    public ViventrumRenderer(Context context) {
        super(context, new ViventrumModel<>(context.bakeLayer(ModModelLayers.VIVENTRUM_LAYER)), 0.3F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ViventrumEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(ViventrumEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.45D, 0.0D);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
