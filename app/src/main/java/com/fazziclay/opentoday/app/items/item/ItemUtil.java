package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.ImportantDebugCallback;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import kotlin.collections.ArraysKt;

public class ItemUtil {
    private static final String TAG = "ItemUtil";

    public static void throwIsBreakType(Item item) {
        if (item.getClass() == Item.class) {
            throw new RuntimeException("'Item' not allowed to add (use Item children's)");
        }
    }

    public static void throwIsAttached(Item item) {
        if (item.isAttached()) {
            throw new RuntimeException("items already attached. Use item.delete() to detach");
        }
    }

    public static void throwIsIdNull(Item item) {
        if (item.getId() == null) {
            throw new RuntimeException("Item id is null!");
        }
    }

    public static boolean isTypeContainsInParents(Item item, ItemType itemType) {
        ItemsStorage[] pathToItem = getPathToItemNoReverse(item);
        for (ItemsStorage itemsStorage : pathToItem) {
            if (itemsStorage instanceof Item _item) {
                ItemType t = ItemType.byClass(_item.getClass());
                if (t == itemType) return true;
            }
        }
        return false;
    }

    public static ItemsStorage[] getPathToItem(Item item) {
        ItemsStorage[] result = getPathToItemNoReverse(item);
        ArraysKt.reverse(result);
        return result;
    }

    public static ItemsStorage[] getPathToItemNoReverse(Item item) {
        if (!item.isAttached()) throw new IllegalArgumentException("getPathToItem: Item is not attached.");
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
        return path.toArray(new ItemsStorage[0]);
    }

    @NotNull
    public static Item[] getAllItemsInTree(@NotNull Item[] list) {
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
        if (id == null) return null;
        return getItemById(getAllItemsInTree(rootArray), id);
    }

    @Nullable
    public static Item getItemById(@NonNull Item[] allItems, @NonNull UUID id) {
        if (id == null) return null;
        UUID find = null;
        Item findItem = null;

        for (Item item : allItems) {
            if (id.equals(item.getId())) {
                if (find != null) {
                    ImportantDebugCallback.pushStatic(TAG + " getItemById id duplicate: findItem="+findItem+" find="+find + "item="+item);
                }

                find = id;
                findItem = item;
            }
        }
        return findItem;
    }

    public static void moveItems(List<Item> items, int positionFrom, int positionTo, CallbackStorage<OnItemsStorageUpdate> onUpdateCallbacks) {
        if (positionFrom >= items.size() || positionTo >= items.size()) throw new IndexOutOfBoundsException("Attempt to move an item outside the list");
        Item from = items.get(positionFrom);
        items.remove(from);
        items.add(positionTo, from);
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onMoved(from, positionFrom, positionTo));
    }

    public static UUID getId(Object o) {
        if (o instanceof Unique unique) {
            return unique.getId();
        }
        return null;
    }

    private static final List<TickTarget> IMPORTANT_TICK_TARGETS = List.of(
            TickTarget.ITEM_FILTER_GROUP_TICK,
            TickTarget.ITEM_DAY_REPEATABLE_CHECKBOX_UPDATE,
            TickTarget.ITEM_NOTIFICATIONS,
            TickTarget.ITEM_NOTIFICATION_SCHEDULE,
            TickTarget.ITEM_MATH_GAME_UPDATE);
    public static void tickOnlyImportantTargets(TickSession tickSession, Item[] items) {
        // NOTE: No use 'for-loop' (self-delete item in tick => ConcurrentModificationException)
        int i = items.length - 1;
        while (i >= 0) {
            Item item = items[i];
            if (item != null && item.isAttached() && tickSession.isAllowed(item)) {
                tickSession.runWithSpecifiedTickTargets(IMPORTANT_TICK_TARGETS, () -> item.tick(tickSession));
            }
            i--;
        }
    }

    @NotNull
    public static List<Item> copyItemsList(Item[] items) {
        List<Item> ret = new ArrayList<>();
        for (Item item : items) {
            ret.add(copyItem(item));
        }
        return ret;
    }


    public static Item copyItem(Item item) {
        return ItemsRegistry.REGISTRY.copyItem(item);
    }

    public static ItemType getItemType(Item item) {
        throwIsBreakType(item);
        return ItemsRegistry.REGISTRY.get(item.getClass()).getItemType();
    }

    public static UUID controllerGenerateItemId(ItemsRoot root, Item item) {
        if (root != null) {
            return root.generateUniqueId();
        }
        Logger.w(TAG, "controllerGenerateItemId: root is null... item.attached="+item.isAttached()+" item="+item);
        return UUID.randomUUID();
    }

    /**
     * <h1>Sensitive!!!</h1>
     * <h2>Do not use this method. It is only needed to call the PROTECTED method by the tab</h2>
     * @param item item to call PROTECTED regenerateId();
     */
    public static void regenerateIdForItem(Item item) {
        item.regenerateId();
    }
}
