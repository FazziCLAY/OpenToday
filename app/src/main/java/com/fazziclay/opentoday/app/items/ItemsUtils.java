package com.fazziclay.opentoday.app.items;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.ContainerItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemIEUtil;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemsUtils {
    @NonNull
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

    public static void moveItems(List<Item> items, int positionFrom, int positionTo, CallbackStorage<OnItemsStorageUpdate> onUpdateCallbacks) {
        Item from = items.get(positionFrom);
        items.remove(from);
        items.add(positionTo, from);
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onMoved(from, positionFrom, positionTo));
        // TODO: 27.10.2022 EXPERIMENTAL CHANGES
        //Collections.swap(this.items, positionFrom, positionTo);
    }

    public static void checkAllowedItems(Item item) {
        if (item.getClass() == Item.class) {
            throw new RuntimeException("'Item' not allowed to add (add Item parents)");
        }
    }

    public static void checkAttached(Item item) {
        if (item.isAttached()) {
            throw new RuntimeException("items already attached. Use item.delete() to detach");
        }
    }
}
