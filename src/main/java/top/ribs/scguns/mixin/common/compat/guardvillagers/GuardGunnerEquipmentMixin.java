package top.ribs.scguns.mixin.common.compat.guardvillagers;

import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.config.GunnerMobSpawner;

@Pseudo
@Mixin(targets = "tallestegg.guardvillagers.common.entities.Guard", remap = false)
public class GuardGunnerEquipmentMixin {
    @Inject(method = "aiStep", at = @At("TAIL"), remap = false)
    private void scguns$checkGeneratedGuardEquipment(CallbackInfo ci) {
        GunnerMobSpawner.checkGuardVillager((PathfinderMob) (Object) this);
    }
}
