package ru.fazziclay.opentoday.app.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdate;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.util.DebugUtil;
import ru.fazziclay.opentoday.util.Profiler;

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

    private void check(Item item, Item[] checkList) {
        Item[] checkAll = getAllItemsInTree(checkList);
        for (Item check : checkAll) {
            if (check.getId().equals(item.getId())) {
                throw new RuntimeException("Item is already present in this storage!");
            }
        }
    }

    private void preAddCheck(Item item) {
        check(item, items.toArray(new Item[0]));
        if (item instanceof ContainerItem) {
            ContainerItem containerItem = (ContainerItem) item;
            for (Item itemInItem : containerItem.getAllItems()) {
                check(itemInItem, items.toArray(new Item[0]));
            }
        }
    }

    public Item[] getAllItemsInTree(Item[] list) {
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

    public Item getItemById(UUID id) {
        for (Item item : getAllItemsInTree(items.toArray(new Item[0]))) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void deleteItem(Item item) {
        onUpdateCallbacks.run((callbackStorage, callback) -> callback.onDeleted(item));
        items.remove(item);
        save();
    }

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

    @Override
    public CallbackStorage<OnItemStorageUpdate> getOnUpdateCallbacks() {
        return onUpdateCallbacks;
    }

    public void importData(DataTransferPacket importPacket) {
        this.items.clear();
        Profiler profiler = new Profiler("SimpleItemStorage importData");

        profiler.point("check repeated UUIDs");
        Item[] allImportItems = getAllItemsInTree(importPacket.items.toArray(new Item[0]));
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
        this.items.addAll(importPacket.items);
        for (Item item : this.items) {
            item.setController(simpleItemController);
        }
        profiler.end();
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
