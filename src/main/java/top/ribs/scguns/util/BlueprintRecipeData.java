package top.ribs.scguns.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import top.ribs.scguns.client.screen.GunBenchRecipe;

import java.util.Optional;

public final class BlueprintRecipeData {
    public static final String ACTIVE_RECIPE_KEY = "ActiveRecipe";

    private BlueprintRecipeData() {
    }

    public static void saveActiveRecipe(ItemStack blueprint, ResourceLocation recipeId) {
        CustomData.update(DataComponents.CUSTOM_DATA, blueprint, tag -> tag.putString(ACTIVE_RECIPE_KEY, recipeId.toString()));
    }

    public static void saveActiveRecipe(ItemStack blueprint, String recipeId) {
        CustomData.update(DataComponents.CUSTOM_DATA, blueprint, tag -> tag.putString(ACTIVE_RECIPE_KEY, recipeId));
    }

    public static void clearActiveRecipe(ItemStack blueprint) {
        CompoundTag tag = blueprint.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(ACTIVE_RECIPE_KEY)) {
            return;
        }

        tag.remove(ACTIVE_RECIPE_KEY);
        if (tag.isEmpty()) {
            blueprint.remove(DataComponents.CUSTOM_DATA);
        } else {
            blueprint.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static ResourceLocation getActiveRecipe(ItemStack blueprint) {
        CustomData customData = blueprint.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return null;
        }

        CompoundTag tag = customData.copyTag();
        if (!tag.contains(ACTIVE_RECIPE_KEY)) {
            return null;
        }

        try {
            return ResourceLocation.parse(tag.getString(ACTIVE_RECIPE_KEY));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String getActiveRecipeName(ItemStack blueprint, Level level) {
        ResourceLocation recipeId = getActiveRecipe(blueprint);
        if (recipeId == null || level == null) {
            return null;
        }

        Optional<RecipeHolder<GunBenchRecipe>> recipe = level.getRecipeManager()
                .getAllRecipesFor(GunBenchRecipe.Type.INSTANCE)
                .stream()
                .filter(r -> r.id().equals(recipeId))
                .findFirst();

        return recipe.map(holder -> holder.value().getResultItem(level.registryAccess()).getDisplayName().getString()).orElse(null);
    }
}
