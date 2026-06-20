package top.ribs.scguns.client.render.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import net.minecraft.world.item.ItemDisplayContext;


public class AmmoBoxRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                          SlotContext slotContext,
                                                                          PoseStack matrixStack,
                                                                          RenderLayerParent<T, M> renderLayerParent,
                                                                          MultiBufferSource bufferSource,
                                                                          int light, float limbSwing,
                                                                          float limbSwingAmount,
                                                                          float partialTicks,
                                                                          float ageInTicks,
                                                                          float netHeadYaw,
                                                                          float headPitch) {
        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            ICurioRenderer.translateIfSneaking(matrixStack, entity);
            humanoidModel.body.translateAndRotate(matrixStack);
        }

        double xOffset = slotContext.index() == 0 ? -0.18D : 0.18D;
        matrixStack.translate(xOffset, 0.55D, 0.17D);
        matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        matrixStack.scale(0.8f, 0.8f, 0.8f);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(stack, null, null, 0);

        itemRenderer.render(stack, ItemDisplayContext.GROUND, false, matrixStack, bufferSource, light, OverlayTexture.NO_OVERLAY, bakedModel);

        matrixStack.popPose();
    }
}
