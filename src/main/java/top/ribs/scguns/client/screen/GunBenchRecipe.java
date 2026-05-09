package top.ribs.scguns.client.screen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;

public class GunBenchRecipe implements Recipe<GunBenchRecipe.Input> {
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;
    private final Ingredient blueprint;

    public GunBenchRecipe(ItemStack output, NonNullList<Ingredient> recipeItems, Ingredient blueprint) {
        this.output = output;
        this.recipeItems = recipeItems;
        this.blueprint = blueprint;
    }

    @Override
    public boolean matches(Input input, Level level) {
        ItemStack blueprintStack = input.getItem(GunBenchMenu.SLOT_BLUEPRINT);
        if (!blueprint.test(blueprintStack)) {
            return false;
        }

        for (int i = 0; i < recipeItems.size(); i++) {
            ItemStack stackInSlot = input.getItem(i);
            Ingredient requiredIngredient = recipeItems.get(i);
            if (!requiredIngredient.isEmpty() && !requiredIngredient.test(stackInSlot)) {
                return false;
            } else if (requiredIngredient.isEmpty() && !stackInSlot.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public NonNullList<Ingredient> getIngredients() {
        return recipeItems;
    }

    public Ingredient getBlueprint() {
        return blueprint;
    }

    private ItemStack getResultItem() {
        return output;
    }

    public record Input(Container container) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index >= 0 && index < container.getContainerSize() ? container.getItem(index) : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return container.getContainerSize();
        }
    }

    public static class Type implements RecipeType<GunBenchRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "gun_bench";
    }

    public static class Serializer implements RecipeSerializer<GunBenchRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "gun_bench");

        private static final MapCodec<Ingredients> INGREDIENTS_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.optionalFieldOf("gun_top_internal_1", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(0)),
                Ingredient.CODEC.optionalFieldOf("gun_top_internal_2", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(1)),
                Ingredient.CODEC.optionalFieldOf("gun_top_barrel_1", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(2)),
                Ingredient.CODEC.optionalFieldOf("gun_top_barrel_2", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(3)),
                Ingredient.CODEC.optionalFieldOf("gun_internal_1", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(4)),
                Ingredient.CODEC.optionalFieldOf("gun_internal_2", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(5)),
                Ingredient.CODEC.optionalFieldOf("gun_barrel_1", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(6)),
                Ingredient.CODEC.optionalFieldOf("gun_barrel_2", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(7)),
                Ingredient.CODEC.optionalFieldOf("gun_grip", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(8)),
                Ingredient.CODEC.optionalFieldOf("gun_magazine", Ingredient.EMPTY).forGetter(ingredients -> ingredients.recipeItems.get(9)),
                Ingredient.CODEC.optionalFieldOf("blueprint", Ingredient.EMPTY).forGetter(ingredients -> ingredients.blueprint)
        ).apply(instance, Ingredients::new));

        private static final MapCodec<GunBenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(Serializer::ingredientsFor),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(GunBenchRecipe::getResultItem)
        ).apply(instance, (ingredients, output) -> new GunBenchRecipe(output, ingredients.recipeItems(), ingredients.blueprint())));

        private static final StreamCodec<RegistryFriendlyByteBuf, GunBenchRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::encode,
                Serializer::decode
        );

        @Override
        public MapCodec<GunBenchRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GunBenchRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static GunBenchRecipe decode(RegistryFriendlyByteBuf buffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(10, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }

            Ingredient blueprint = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);

            return new GunBenchRecipe(output, inputs, blueprint);
        }

        private static void encode(RegistryFriendlyByteBuf buffer, GunBenchRecipe recipe) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.blueprint);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
        }

        private static Ingredients ingredientsFor(GunBenchRecipe recipe) {
            return new Ingredients(recipe.recipeItems, recipe.blueprint);
        }

        private record Ingredients(NonNullList<Ingredient> recipeItems, Ingredient blueprint) {
            private Ingredients(
                    Ingredient gunTopInternal1,
                    Ingredient gunTopInternal2,
                    Ingredient gunTopBarrel1,
                    Ingredient gunTopBarrel2,
                    Ingredient gunInternal1,
                    Ingredient gunInternal2,
                    Ingredient gunBarrel1,
                    Ingredient gunBarrel2,
                    Ingredient gunGrip,
                    Ingredient gunMagazine,
                    Ingredient blueprint
            ) {
                this(toList(
                        gunTopInternal1,
                        gunTopInternal2,
                        gunTopBarrel1,
                        gunTopBarrel2,
                        gunInternal1,
                        gunInternal2,
                        gunBarrel1,
                        gunBarrel2,
                        gunGrip,
                        gunMagazine
                ), blueprint);
            }

            private static NonNullList<Ingredient> toList(Ingredient... ingredients) {
                NonNullList<Ingredient> list = NonNullList.withSize(10, Ingredient.EMPTY);
                for (int i = 0; i < ingredients.length; i++) {
                    list.set(i, ingredients[i]);
                }
                return list;
            }
        }
    }
}
