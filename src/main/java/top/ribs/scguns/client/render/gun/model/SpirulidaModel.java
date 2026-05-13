package top.ribs.scguns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.client.SpecialModels;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.attachment.IAttachment;

public class SpirulidaModel implements IOverrideModel {
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        RenderUtil.renderModel(SpecialModels.SPIRULIDA_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);
        if (Gun.getScope(stack) == null) {
            RenderUtil.renderModel(SpecialModels.SPIRULIDA_SIGHTS.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {
            ItemStack stock = Gun.getAttachment(IAttachment.Type.STOCK, stack);
            if (stock.getItem() == ModItems.WEIGHTED_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.SPIRULIDA_STOCK_WEIGHTED.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (stock.getItem() == ModItems.LIGHT_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.SPIRULIDA_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
            } else if (stock.getItem() == ModItems.WOODEN_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.SPIRULIDA_STOCK_WOODEN.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        }
    }
}
