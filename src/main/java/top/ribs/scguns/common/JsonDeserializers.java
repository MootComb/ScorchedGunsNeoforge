package top.ribs.scguns.common;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import top.ribs.scguns.client.util.Easings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Author: MrCrayfish
 */
public class JsonDeserializers
{
    public static final JsonDeserializer<ItemStack> ITEM_STACK = (json, typeOfT, context) -> {
        JsonObject object = json.getAsJsonObject();
        if (!object.has("item")) {
            return ItemStack.STRICT_CODEC.parse(Gun.builtInRegistryProvider().createSerializationContext(JsonOps.INSTANCE), json)
                    .getOrThrow(JsonParseException::new);
        }

        ResourceLocation itemId = ResourceLocation.parse(GsonHelper.getAsString(object, "item"));
        Item item = BuiltInRegistries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item, GsonHelper.getAsInt(object, "count", 1));
        if (object.has("nbt")) {
            try {
                CompoundTag tag = TagParser.parseTag(object.get("nbt").isJsonPrimitive() ? object.get("nbt").getAsString() : object.get("nbt").toString());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Invalid ItemStack nbt", e);
            }
        }
        return stack;
    };
    public static final JsonDeserializer<ResourceLocation> RESOURCE_LOCATION = (json, typeOfT, context) -> ResourceLocation.parse(json.getAsString());
    public static final JsonDeserializer<FireMode> FIRE_MODE = (json, typeOfT, context) -> FireMode.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<ReloadType> RELOAD_TYPE = (json, typeOfT, context) -> ReloadType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<GripType> GRIP_TYPE = (json, typeOfT, context) -> GripType.getType(ResourceLocation.tryParse(json.getAsString()));
    public static final JsonDeserializer<Easings> EASING = (json, typeOfT, context) -> Easings.byName(json.getAsString());
}
