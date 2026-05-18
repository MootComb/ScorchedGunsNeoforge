package top.ribs.scguns.common;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import top.ribs.scguns.init.ModEntities;

import java.util.ArrayList;
import java.util.List;

public final class MobGuideHelper {
    private MobGuideHelper() {
    }

    public static ItemStack createGuideBook(EntityType<?> entityType) {
        if (entityType == ModEntities.VIVENTRUM.get()) {
            return createGuideBook("info.scguns.viventrum_guide", "Viventrum Companion Guide", "Asgharian Automaton Unit", 5);
        }
        if (entityType == ModEntities.SUPPLY_SCAMP.get()) {
            return createGuideBook("info.scguns.supply_scamp_guide", "Supply Scamp Helper Guide", "COG Robotics Division", 6);
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack createGuideBook(String keyPrefix, String fallbackTitle, String fallbackAuthor, int pageCount) {
        List<Filterable<Component>> pages = new ArrayList<>(pageCount);
        for (int i = 1; i <= pageCount; i++) {
            pages.add(Filterable.passThrough(Component.translatable(keyPrefix + ".page" + i)));
        }

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(localizedString(keyPrefix + ".title", fallbackTitle)),
                localizedString(keyPrefix + ".author", fallbackAuthor),
                0,
                pages,
                true
        ));
        return book;
    }

    private static String localizedString(String key, String fallback) {
        String value = Component.translatable(key).getString();
        if (value.equals(key) || value.isBlank()) {
            return fallback;
        }
        return value.length() > WrittenBookContent.TITLE_MAX_LENGTH ? value.substring(0, WrittenBookContent.TITLE_MAX_LENGTH) : value;
    }
}
