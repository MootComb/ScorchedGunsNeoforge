package top.ribs.scguns.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class LateRaidHumanoidRenderer<T extends Mob> extends HumanoidMobRenderer<T, LateRaidHumanoidModel<T>> {
    private final ResourceLocation texture;
    private final float scale;

    public LateRaidHumanoidRenderer(EntityRendererProvider.Context context, ResourceLocation texture, float shadowRadius, float scale) {
        super(context, new LateRaidHumanoidModel<>(context.bakeLayer(ModModelLayers.LATE_RAID_HUMANOID_LAYER)), shadowRadius);
        this.texture = texture;
        this.scale = scale;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }

    @Override
    protected void scale(T livingEntity, PoseStack poseStack, float partialTickTime) {
        if (this.scale != 1.0F) {
            poseStack.scale(this.scale, this.scale, this.scale);
        }
    }
}
