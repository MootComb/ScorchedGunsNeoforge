package top.ribs.scguns.client;

import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.client.resources.IDataLoader;
import com.mrcrayfish.framework.client.resources.IResourceSupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.tuple.Pair;
import top.ribs.scguns.item.IMeta;

import java.util.ArrayList;
import java.util.List;

public final class MetaLoader implements IDataLoader<MetaLoader.ItemResource>
{
    private static MetaLoader instance;

    public static MetaLoader getInstance()
    {
        if(instance == null)
        {
            instance = new MetaLoader();
        }
        return instance;
    }

    private static final Hash.Strategy<Item> IDENTITY_STRATEGY = new Hash.Strategy<>()
    {
        @Override
        public int hashCode(Item item)
        {
            return System.identityHashCode(item);
        }

        @Override
        public boolean equals(Item left, Item right)
        {
            return left == right;
        }
    };

    private final Object2ObjectMap<Item, DataObject> itemToData = new Object2ObjectOpenCustomHashMap<>(IDENTITY_STRATEGY);

    private MetaLoader()
    {
        this.itemToData.defaultReturnValue(DataObject.EMPTY);
    }

    public DataObject getData(Item item)
    {
        return this.itemToData.get(item);
    }

    @Override
    public List<ItemResource> getResourceSuppliers()
    {
        List<ItemResource> resources = new ArrayList<>();
        BuiltInRegistries.ITEM.stream().filter(item -> item instanceof IMeta).forEach(item ->
        {
            ResourceLocation key = item.builtInRegistryHolder().key().location();
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), "models/item/" + key.getPath() + ".scmeta");
            resources.add(new ItemResource(item, location));
        });
        return resources;
    }

    @Override
    public void process(List<Pair<ItemResource, DataObject>> list)
    {
        this.itemToData.clear();
        list.forEach(pair ->
        {
            DataObject object = pair.getRight();
            if(!object.isEmpty())
            {
                ItemResource resource = pair.getLeft();
                this.itemToData.put(resource.item(), object);
            }
        });
    }

    @Override
    public boolean ignoreMissing()
    {
        return true;
    }

    public record ItemResource(Item item, ResourceLocation location) implements IResourceSupplier
    {
        @Override
        public ResourceLocation getLocation()
        {
            return this.location;
        }
    }
}
