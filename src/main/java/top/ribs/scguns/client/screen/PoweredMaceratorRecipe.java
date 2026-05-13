package top.ribs.scguns.client.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import top.ribs.scguns.Reference;

import java.util.Iterator;
import java.util.List;

public class PoweredMaceratorRecipe implements Recipe<PoweredMaceratorRecipe.Input> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final int processingTime;
    private final int energyUse;

    public PoweredMaceratorRecipe(NonNullList<Ingredient> inputItems, ItemStack output, int processingTime, int energyUse) {
        this.inputItems = inputItems;
        this.output = output;
        this.processingTime = processingTime;
        this.energyUse = energyUse;
    }

    @Override
    public boolean matches(Input inv, Level world) {
        if (world.isClientSide()) {
            return false;
        }

        NonNullList<Ingredient> requiredIngredients = NonNullList.create();
        requiredIngredients.addAll(inputItems);

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                boolean matched = false;
                Iterator<Ingredient> iterator = requiredIngredients.iterator();
                while (iterator.hasNext()) {
                    Ingredient ingredient = iterator.next();
                    if (ingredient.test(stackInSlot)) {
                        iterator.remove();
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
        }

        return requiredIngredients.isEmpty();
    }

    @Override
    public ItemStack assemble(Input inv, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getEnergyUse() {
        return energyUse;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public NonNullList<Ingredient> getIngredients() {
        return inputItems;
    }

    private static NonNullList<Ingredient> toNonNullList(List<Ingredient> ingredients) {
        NonNullList<Ingredient> list = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
        for (int i = 0; i < ingredients.size(); i++) {
            list.set(i, ingredients.get(i));
        }
        return list;
    }

    public record Input(List<ItemStack> items) implements RecipeInput {
        public Input {
            items = List.copyOf(items);
        }

        @Override
        public ItemStack getItem(int index) {
            return index >= 0 && index < items.size() ? items.get(index) : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return items.size();
        }
    }

    public static class Type implements RecipeType<PoweredMaceratorRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "powered_macerating";
    }

    public static class Serializer implements RecipeSerializer<PoweredMaceratorRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "powered_macerating");
        private static final MapCodec<PoweredMaceratorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.LIST_CODEC_NONEMPTY.fieldOf("ingredients").forGetter(recipe -> List.copyOf(recipe.inputItems)),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                Codec.INT.optionalFieldOf("processingTime", 200).forGetter(recipe -> recipe.processingTime),
                Codec.INT.optionalFieldOf("energyUse", 1000).forGetter(recipe -> recipe.energyUse)
        ).apply(instance, (ingredients, output, processingTime, energyUse) ->
                new PoweredMaceratorRecipe(toNonNullList(ingredients), output, processingTime, energyUse)));
        private static final StreamCodec<RegistryFriendlyByteBuf, PoweredMaceratorRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::encode,
                Serializer::decode
        );

        @Override
        public MapCodec<PoweredMaceratorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PoweredMaceratorRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static PoweredMaceratorRecipe decode(RegistryFriendlyByteBuf buffer) {
            int size = buffer.readInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }

            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            int processingTime = buffer.readInt();
            int energyUse = buffer.readInt();

            return new PoweredMaceratorRecipe(ingredients, output, processingTime, energyUse);
        }

        private static void encode(RegistryFriendlyByteBuf buffer, PoweredMaceratorRecipe recipe) {
            buffer.writeInt(recipe.inputItems.size());
            for (Ingredient ingredient : recipe.inputItems) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
            buffer.writeInt(recipe.getProcessingTime());
            buffer.writeInt(recipe.getEnergyUse());
        }
    }
}
