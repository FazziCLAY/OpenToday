package com.fazziclay.opentoday.app.items;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.TickSession;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemController;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class SimpleItemsStorage implements ItemsStorage {
    private final List<Item> items;
    private final ItemController simpleItemController;
    private final CallbackStorage<OnItemsStorageUpdate> onUpdateCallbacks = new CallbackStorage<>();

    public SimpleItemsStorage(List<Item> items) {
        this.items = items;
        this.simpleItemController = new SimpleItemController();
    }

    public SimpleItemsStorage() {
        this(new ArrayList<>());
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
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public void addItem(Item item) {
        addItem(item, items.size());
    }

    @Override
    public void addItem(Item item, int position) {
        ItemsUtils.checkAllowedItems(item);
        ItemsUtils.checkAttached(item);
        item.attach(simpleItemController);
        items.add(position, item);
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onAdded(item, getItemPosition(item)));
        save();
    }

    @Override
    public Item getItemById(UUID id) {
        return ItemsUtils.getItemByIdRoot(getAllItems(), id);
    }

    @Override
    public void deleteItem(Item item) {
        int position = getItemPosition(item);
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onDeleted(item, position)); // TODO: 5/9/23 Remove deprecated
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onPreDeleted(item, position));

        items.remove(item);
        item.detach();

        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onPostDeleted(item, position));
        save();
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        Item copy = ItemsRegistry.REGISTRY.get(item.getClass()).copy(item);
        addItem(copy, getItemPosition(item) + 1);
        return copy;
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        ItemsUtils.moveItems(this.items, positionFrom, positionTo, onUpdateCallbacks);
        save();
    }

    // NOTE: No use 'for-loop' (self-delete item in tick => ConcurrentModificationException)
    @Override
    public void tick(TickSession tickSession) {
        int i = items.size() - 1;
        while (i >= 0) {
            items.get(i).tick(tickSession);
            i--;
        }
    }

    @Override
    public int getItemPosition(Item item) {
        return items.indexOf(item);
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnUpdateCallbacks() {
        return onUpdateCallbacks;
    }

    public void importData(List<Item> items) {
        Item[] allImportItems = ItemsUtils.getAllItemsInTree(items.toArray(new Item[0]));
        for (Item check1 : allImportItems) {
            if (check1.getId() == null) {
                check1.regenerateId();
            }
            for (Item check2 : allImportItems) {
                if (check1.getId().equals(check2.getId()) && check1 != check2) {
                    check2.regenerateId();
                }
            }
        }

        for (Item item : items) {
            try {
                ItemsUtils.checkAllowedItems(item);
                ItemsUtils.checkAttached(item);
                item.setController(simpleItemController);
                if (item.getId() == null) {
                    item.regenerateId();
                }
                this.items.add(item);
            } catch (Exception ignored) {}
        }
    }

    private class SimpleItemController extends ItemController {
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
    }
}
