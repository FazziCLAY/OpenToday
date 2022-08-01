package ru.fazziclay.opentoday.app.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.fazziclay.opentoday.app.items.callback.OnItemAdded;
import ru.fazziclay.opentoday.app.items.callback.OnItemDeleted;
import ru.fazziclay.opentoday.app.items.callback.OnItemMoved;
import ru.fazziclay.opentoday.app.items.callback.OnItemUpdated;
import ru.fazziclay.opentoday.callback.CallbackStorage;

public abstract class SimpleItemStorage implements ItemStorage {
    private List<Item> items = new ArrayList<>();
    private final ItemController itemController;
    private final CallbackStorage<OnItemDeleted> onItemDeletedCallbackStorage = new CallbackStorage<>();
    private final CallbackStorage<OnItemUpdated> onItemUpdatedCallbackStorage = new CallbackStorage<>();
    private final CallbackStorage<OnItemAdded> onItemAddedCallbackStorage = new CallbackStorage<>();
    private final CallbackStorage<OnItemMoved> onItemMovedCallbackStorage = new CallbackStorage<>();

    public SimpleItemStorage(List<Item> items) {
        this.items = items;
        this.itemController = new SimpleItemController();
    }

    public SimpleItemStorage() {
        this.itemController = new SimpleItemController();
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
        item.controller = itemController;
        items.add(item);
        save();
        onItemAddedCallbackStorage.run((callbackStorage, callback) -> callback.run(item, getItemPosition(item)));
    }

    @Override
    public void deleteItem(Item item) {
        int pos = getItemPosition(item);
        items.remove(item);
        save();
        onItemDeletedCallbackStorage.run((callbackStorage, callback) -> callback.run(item, pos));
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        Collections.swap(this.items, positionFrom, positionTo);
        save();
        onItemMovedCallbackStorage.run((callbackStorage, callback) -> callback.run(positionFrom, positionTo));
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
    public CallbackStorage<OnItemDeleted> getOnItemDeletedCallbackStorage() {
        return onItemDeletedCallbackStorage;
    }

    @Override
    public CallbackStorage<OnItemUpdated> getOnItemUpdatedCallbackStorage() {
        return onItemUpdatedCallbackStorage;
    }

    @Override
    public CallbackStorage<OnItemAdded> getOnItemAddedCallbackStorage() {
        return onItemAddedCallbackStorage;
    }

    @Override
    public CallbackStorage<OnItemMoved> getOnItemMovedCallbackStorage() {
        return onItemMovedCallbackStorage;
    }

    public void importData(DataTransferPacket importPacket) {
        this.items.clear();
        this.items.addAll(importPacket.items);
        for (Item item : this.items) {
            item.controller = itemController;
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
            int pos = getItemPosition(item);
            SimpleItemStorage.this.onItemUpdatedCallbackStorage.run((callbackStorage, callback) -> callback.run(item, pos));
        }
    }
}
