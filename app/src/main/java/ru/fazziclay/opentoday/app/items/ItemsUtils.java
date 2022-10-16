package ru.fazziclay.opentoday.app.items;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ru.fazziclay.opentoday.app.items.item.ContainerItem;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.ItemIEUtil;

public class ItemsUtils {
    public static List<Item> copy(Item[] items) {
        try {
            return ItemIEUtil.importItemList(ItemIEUtil.exportItemList(items));
        } catch (Exception e) {
            throw new RuntimeException("Clone exception!", e);
        }
    }

    @NonNull
    public static Item[] getAllItemsInTree(@NonNull Item[] list) {
        List<Item> ret = new ArrayList<>();
        for (Item item : list) {
            ret.add(item);
            if (item instanceof ContainerItem) {
                ContainerItem containerItem = (ContainerItem) item;
                Item[] r = getAllItemsInTree(containerItem.getAllItems());
                ret.addAll(Arrays.asList(r));
            }
        }

        return ret.toArray(new Item[0]);
    }

    /**
    * Get item in rootArray and subItems (recursive)
    * */
    @Nullable
    public static Item getItemByIdRoot(Item[] rootArray, UUID id) {
        return getItemById(getAllItemsInTree(rootArray), id);
    }

    @Nullable
    public static Item getItemById(@NonNull Item[] allItems, UUID id) {
        for (Item item : allItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
