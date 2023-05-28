package com.fazziclay.opentoday.app.items;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.ContainerItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemCodecUtil;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import kotlin.collections.ArraysKt;

public class ItemsUtils {
    /**
     * <h1>WARNING! Coping include ID!!!!!</h1>
     */
    @NonNull
    public static List<Item> copy(Item[] items) {
        return ItemCodecUtil.importItemList(ItemCodecUtil.exportItemList(items));
    }

    public static ItemsStorage[] getPathToItem(Item item) {
        if (!item.isAttached()) throw new IllegalArgumentException("Item not attached.");
        List<ItemsStorage> path = new ArrayList<>();
        ItemsStorage temp = item.getParentItemsStorage();
        while (true) {
            path.add(temp);
            if (temp instanceof Item i) {
                temp = i.getParentItemsStorage();
            } else {
                break;
            }
        }
        ItemsStorage[] result = path.toArray(new ItemsStorage[0]);
        ArraysKt.reverse(result);
        return result;
    }

    @NonNull
    public static Item[] getAllItemsInTree(@NonNull Item[] list) {
        List<Item> ret = new ArrayList<>();
        for (Item item : list) {
            ret.add(item);
            if (item instanceof ContainerItem containerItem) {
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
    public static Item getItemByIdRecursive(Item[] rootArray, UUID id) {
        return getItemById(getAllItemsInTree(rootArray), id);
    }

    @Nullable
    public static Item getItemById(@NonNull Item[] allItems, @NonNull UUID id) {
        for (Item item : allItems) {
            if (id.equals(item.getId())) return item;
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

    public static UUID getId(Object o) {
        if (o instanceof Unique id) {
            return id.getId();
        }
        return null;
    }

    public static void tickDayRepeatableCheckboxes(TickSession tickSession, Item[] items) {
        tickSession.recycleSpecifiedTickTarget(TickTarget.DAY_REPEATABLE_CHECKBOX_UPDATE);

        // NOTE: No use 'for-loop' (self-delete item in tick => ConcurrentModificationException)
        int i = items.length - 1;
        while (i >= 0) {
            Item item = items[i];
            if (item != null && item.isAttached() && tickSession.isAllowed(item)) {
                item.tick(tickSession);
            }
            i--;
        }

        tickSession.recycleSpecifiedTickTarget();
    }
}
