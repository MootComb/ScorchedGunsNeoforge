package top.ribs.scguns.client.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ScGunsGeoArmorRenderProvider {
    private ScGunsGeoArmorRenderProvider() {
    }

    public static <T extends ArmorItem & GeoItem> void accept(Consumer<GeoRenderProvider> consumer, Supplier<? extends GeoArmorRenderer<T>> rendererFactory) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<T> renderer;

            @Override
            public <E extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(E livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<E> original) {
                if (this.renderer == null) {
                    this.renderer = rendererFactory.get();
                }

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }
}
