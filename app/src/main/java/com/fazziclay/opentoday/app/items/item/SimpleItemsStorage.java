package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.CrashReportContext;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class SimpleItemsStorage implements ItemsStorage {
    private static final String TAG = "SimpleItemsStorage";
    private final List<Item> items = new ArrayList<>();
    private final ItemController itemController;
    private final CallbackStorage<OnItemsStorageUpdate> onUpdateCallbacks = new CallbackStorage<>();


    public SimpleItemsStorage(ItemsRoot root) {
        this.itemController = new SimpleItemController(root);
    }

    public SimpleItemsStorage(ItemController customController) {
        this.itemController = customController;
    }

    @NonNull
    @Override
    public Item[] getAllItems() {
        return items.toArray(new Item[0]);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public int totalSize() {
        int c = 0;
        for (Item item : items) {
            c++;
            c+= item.getChildrenItemCount();
        }
        return c;
    }

    @Override
    public Item getItemAt(int position) {
        return items.get(position);
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public void addItem(Item item) {
        addItem(item, items.size());
    }

    @Override
    public void addItem(Item item, int position) {
        ItemUtil.throwIsBreakType(item);
        ItemUtil.throwIsAttached(item);
        items.add(position, item);
        item.attach(itemController);
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onAdded(item, getItemPosition(item)));
        save();
    }

    @Override
    public Item getItemById(UUID id) {
        return ItemUtil.getItemByIdRecursive(getAllItems(), id);
    }

    @Override
    public void deleteItem(Item item) {
        int position = getItemPosition(item);
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onPreDeleted(item, position));

        items.remove(item);
        item.detach();

        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onPostDeleted(item, position));
        save();
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        Item copy = ItemUtil.copyItem(item);
        addItem(copy, getItemPosition(item) + 1);
        return copy;
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        ItemUtil.moveItems(this.items, positionFrom, positionTo, onUpdateCallbacks);
        save();
    }

    // NOTE: No use 'for-loop' (self-delete item in tick => ConcurrentModificationException)
    @Override
    public void tick(TickSession tickSession) {
        int i = items.size() - 1;
        while (i >= 0) {
            Item item = items.get(i);
            if (tickSession.isAllowed(item)) {
                CrashReportContext.BACK.push("SimpleItemStorage.tick.itemTick_"+item.getId());
                item.tick(tickSession);
                CrashReportContext.BACK.pop();
            }
            i--;
        }
    }

    @Override
    public int getItemPosition(Item item) {
        return items.indexOf(item);
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnItemsStorageCallbacks() {
        return onUpdateCallbacks;
    }

    public void importData(List<Item> items) {
        Item[] allImportItems = ItemUtil.getAllItemsInTree(items.toArray(new Item[0]));
        for (Item check1 : allImportItems) {
            ItemUtil.throwIsBreakType(check1);

            if (check1.getId() == null) {
                check1.regenerateId();
                Logger.d(TAG, "importData: check1 id is null! regenerated.");
            }
            for (Item check2 : allImportItems) {
                if (check2.getId() == null) {
                    check2.regenerateId();
                    Logger.d(TAG, "importData: check2 id is null! regenerated.");
                }
                if (check1.getId().equals(check2.getId()) && check1 != check2) {
                    check2.regenerateId();
                    Logger.d(TAG, "importData: check1.id equals check2.id && check1 != check2. id regenerated.");
                }
            }
        }

        for (Item item : items) {
            ItemUtil.throwIsAttached(item);
            if (item.getId() == null) {
                item.regenerateId();
                Logger.d(TAG, "importData: item.id is null. regenerated.");
            }
            item.setController(itemController);
            this.items.add(item);
        }
    }

    public void copyData(Item[] items) {
        items = ItemUtil.copyItemsList(items).toArray(new Item[0]);

        for (Item item : items) {
            ItemUtil.throwIsBreakType(item);
            ItemUtil.throwIsAttached(item);
            item.setController(itemController);
            item.regenerateId();
            this.items.add(item);
        }
    }

    private class SimpleItemController extends ItemController {
        private final ItemsRoot root;

        public SimpleItemController(ItemsRoot root) {
            this.root = root;
        }

        @Override
        public void delete(Item item) {
            SimpleItemsStorage.this.deleteItem(item);
        }

        @Override
        public void save(Item item) {
            SimpleItemsStorage.this.save();
        }

        @Override
        public void updateUi(Item item) {
            SimpleItemsStorage.this.onUpdateCallbacks.run((callbackStorage, callback) -> callback.onUpdated(item, getItemPosition(item)));
        }

        @Override
        public ItemsStorage getParentItemsStorage(Item item) {
            return SimpleItemsStorage.this;
        }

        @Override
        public UUID generateId(Item item) {
            return ItemUtil.controllerGenerateItemId(getRoot(), item);
        }

        @Override
        public ItemsRoot getRoot() {
            return root;
        }
    }
}
