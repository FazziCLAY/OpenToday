package ru.fazziclay.opentoday.app.items;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdate;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.ItemController;
import ru.fazziclay.opentoday.app.items.item.ItemsRegistry;
import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.util.Profiler;

public abstract class SimpleItemsStorage implements ItemsStorage {
    private final List<Item> items;
    private final ItemController simpleItemController;
    private final CallbackStorage<OnItemStorageUpdate> onUpdateCallbacks = new CallbackStorage<>();

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
    public void addItem(Item item) {
        if (item.getClass() == Item.class) {
            throw new RuntimeException("'Item' not allowed to add (add Item parents)");
        }
        item.regenerateId();
        item.setController(simpleItemController);
        items.add(item);
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onAdded(item));
        save();
    }

    @Override
    public Item getItemById(UUID id) {
        return ItemsUtils.getItemByIdRoot(getAllItems(), id);
    }

    @Override
    public void deleteItem(Item item) {
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onDeleted(item));
        item.setController(null);
        items.remove(item);
        save();
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        Item copy = ItemsRegistry.REGISTRY.getItemInfoByClass(item.getClass()).copy(item);
        addItem(copy);
        return copy;
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onMoved(getAllItems()[positionFrom], positionTo));
        Collections.swap(this.items, positionFrom, positionTo);
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
    public CallbackStorage<OnItemStorageUpdate> getOnUpdateCallbacks() {
        return onUpdateCallbacks;
    }

    public void importData(List<Item> items) {
        this.items.clear();
        Profiler profiler = new Profiler("SimpleItemStorage importData");

        profiler.point("check repeated UUIDs");
        Item[] allImportItems = ItemsUtils.getAllItemsInTree(items.toArray(new Item[0]));
        for (Item check1 : allImportItems) {
            for (Item check2 : allImportItems) {
                if (check1.getId() == null) {
                    check1.setId(UUID.randomUUID());
                }

                if (check1.getId().equals(check2.getId())) {
                    check2.setId(UUID.randomUUID());
                }
            }
        }

        profiler.point("add & setupController");
        this.items.addAll(items);
        for (Item item : this.items) {
            item.setController(simpleItemController);
        }
        profiler.end();
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
            SimpleItemsStorage.this.onUpdateCallbacks.run((callbackStorage, callback) -> callback.onUpdated(item));
        }
    }
}
