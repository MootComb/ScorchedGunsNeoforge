package top.ribs.scguns.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private boolean isFrameworkInstalled;
    private boolean isEntityModelFeaturesInstalled;
    private boolean isPunchyInstalled;

    @Override
    public void onLoad(String mixinPackage) {
        isFrameworkInstalled = isClassPresent("com.mrcrayfish.framework.FrameworkNeoForge")
                || isClassPresent("com.mrcrayfish.framework.FrameworkForge")
                || isClassPresent("com.mrcrayfish.framework.api.FrameworkAPI");
        isEntityModelFeaturesInstalled = isModLoaded("entity_model_features")
                || isClassPresent("traben.entity_model_features.models.parts.EMFModelPartRoot")
                && isClassPresent("traben.entity_model_features.models.animation.EMFAnimationEntityContext");
        isPunchyInstalled = isModLoaded("punchy")
                || isClassPresent("punchy.client.render.PunchyArmRenderer")
                || isClassPresent("punchy.client.state.MovementStateMachine")
                || isClassPresent("punchy.client.render.HandRenderBobContext");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!isFrameworkInstalled) {
            return false; // this makes sure that forge's helpful mods not found screen shows up
        }
        if (mixinClassName.startsWith("top.ribs.scguns.mixin.client.compat.fa.")) {
            return isEntityModelFeaturesInstalled;
        }
        if (mixinClassName.startsWith("top.ribs.scguns.mixin.client.compat.punchy.")) {
            return isPunchyInstalled;
        }
        return true;
    }

    private boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, this.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isModLoaded(String modId) {
        try {
            Class<?> loadingModList = Class.forName("net.neoforged.fml.loading.LoadingModList", false, this.getClass().getClassLoader());
            Object modList = loadingModList.getMethod("get").invoke(null);
            return loadingModList.getMethod("getModFileById", String.class).invoke(modList, modId) != null;
        } catch (ReflectiveOperationException | LinkageError e) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
