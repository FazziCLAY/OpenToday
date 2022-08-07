package ru.fazziclay.opentoday.app.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdate;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.CallbackStorage;

public abstract class SimpleItemStorage implements ItemStorage {
    private final List<Item> items;
    private final ItemController simpleItemController;
    private final CallbackStorage<OnItemStorageUpdate> onUpdateCallbacks = new CallbackStorage<>();


    public SimpleItemStorage(List<Item> items) {
        this.items = items;
        this.simpleItemController = new SimpleItemController();
    }

    public SimpleItemStorage() {
        this.items = new ArrayList<>();
        this.simpleItemController = new SimpleItemController();
    }

    @Override
    public Item[] getItems() {
        return items.toArray(new Item[0]);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public void addItem(Item item) {
        if (items.contains(item)) {
            throw new RuntimeException("Item is already present in this storage!");
        }
        if (item.getClass() == Item.class) {
            throw new RuntimeException("'Item' not allowed to add (add Item parents)");
        }
        item.setController(simpleItemController);
        items.add(item);
        save();
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onAdded(item));
    }

    @Override
    public void deleteItem(Item item) {
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onDeleted(item));
        items.remove(item);
        save();
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onMoved(getItems()[positionFrom], positionTo));
        Collections.swap(this.items, positionFrom, positionTo);
        save();
    }

    // NOTE: No use 'for-loop' (self-delete item in tick => ConcurrentModificationException)
    @Override
    public void tick() {
        int i = items.size() - 1;
        while (i >= 0) {
            items.get(i).tick();
            i--;
        }
    }

    @Override
    public int getItemPosition(Item item) {
        return items.indexOf(item);
    }

    @Override
    public CallbackStorage<OnItemStorageUpdate> getOnUpdateCallbacks() {
        return onUpdateCallbacks;
    }

    public void importData(DataTransferPacket importPacket) {
        this.items.clear();
        this.items.addAll(importPacket.items);
        for (Item item : this.items) {
            item.setController(simpleItemController);
        }
    }

    public DataTransferPacket exportData() {
        DataTransferPacket packet = new DataTransferPacket();
        packet.items.clear();
        packet.items.addAll(this.items);
        return packet;
    }

    private class SimpleItemController extends ItemController {
        @Override
        public void delete(Item item) {
            SimpleItemStorage.this.deleteItem(item);
        }

        @Override
        public void save(Item item) {
            SimpleItemStorage.this.save();
        }

        @Override
        public void updateUi(Item item) {
            SimpleItemStorage.this.onUpdateCallbacks.run((callbackStorage, callback) -> callback.onUpdated(item));
        }
    }
}
