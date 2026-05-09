package top.ribs.scguns.client.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

public class LightningBatteryRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack output;
    private final int processingTime;
    private final int energyUse;

    public LightningBatteryRecipe(Ingredient input, ItemStack output, int processingTime, int energyUse) {
        this.input = input;
        this.output = output;
        this.processingTime = processingTime;
        this.energyUse = energyUse;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
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

    public Ingredient getInput() {
        return input;
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

    public static class Type implements RecipeType<LightningBatteryRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "lightning_battery";
    }

    public static class Serializer implements RecipeSerializer<LightningBatteryRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("scguns", "lightning_battery");
        private static final MapCodec<LightningBatteryRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.LIST_CODEC_NONEMPTY.fieldOf("ingredients").forGetter(recipe -> List.of(recipe.input)),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                Codec.INT.fieldOf("processingTime").forGetter(recipe -> recipe.processingTime),
                Codec.INT.fieldOf("requiredEnergy").forGetter(recipe -> recipe.energyUse)
        ).apply(instance, (ingredients, output, processingTime, requiredEnergy) ->
                new LightningBatteryRecipe(ingredients.get(0), output, processingTime, requiredEnergy)));
        private static final StreamCodec<RegistryFriendlyByteBuf, LightningBatteryRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::encode,
                Serializer::decode
        );

        @Override
        public MapCodec<LightningBatteryRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LightningBatteryRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static LightningBatteryRecipe decode(RegistryFriendlyByteBuf buffer) {
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            int processingTime = buffer.readInt();
            int requiredEnergy = buffer.readInt();
            return new LightningBatteryRecipe(input, output, processingTime, requiredEnergy);
        }

        private static void encode(RegistryFriendlyByteBuf buffer, LightningBatteryRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInput());
            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
            buffer.writeInt(recipe.getProcessingTime());
            buffer.writeInt(recipe.getEnergyUse());
        }
    }
}
