package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.CurrentItemStorage;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CycleListItem extends TextItem implements ContainerItem, ItemsStorage, CurrentItemStorage {
    // START - Save
    public final static CycleListItemCodec CODEC = new CycleListItemCodec();
    public static class CycleListItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            CycleListItem cycleListItem = (CycleListItem) item;
            return super.exportItem(item)
                    .put("currentItemPosition", cycleListItem.currentItemPosition)
                    .put("itemsCycle", ItemCodecUtil.exportItemList(cycleListItem.getAllItems()))
                    .put("tickBehavior", cycleListItem.tickBehavior);
        }

        private final CycleListItem defaultValues = new CycleListItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            CycleListItem cycleListItem = item != null ? (CycleListItem) item : new CycleListItem();
            super.importItem(cherry, cycleListItem);
            cycleListItem.itemsCycleStorage.importData(ItemCodecUtil.importItemList(cherry.optOrchard("itemsCycle")));
            cycleListItem.currentItemPosition = cherry.optInt("currentItemPosition", defaultValues.currentItemPosition);
            cycleListItem.tickBehavior = cherry.optEnum("tickBehavior", defaultValues.tickBehavior);
            return cycleListItem;
        }
    }
    // END - Save

    @NonNull
    public static CycleListItem createEmpty() {
        return new CycleListItem("");
    }

    @SaveKey(key = "itemsCycle") @RequireSave private final CycleItemsStorage itemsCycleStorage = new CycleItemsStorage();
    @SaveKey(key = "currentItemPosition") @RequireSave private int currentItemPosition = 0;
    @SaveKey(key = "tickBehavior") @RequireSave private TickBehavior tickBehavior = TickBehavior.CURRENT;
    private final CallbackStorage<OnCurrentItemStorageUpdate> onCurrentItemStorageUpdateCallback = new CallbackStorage<>();

    protected CycleListItem() {}

    public CycleListItem(String text) {
        super(text);
    }

    public CycleListItem(TextItem textItem) {
        super(textItem);
    }

    public CycleListItem(TextItem textItem, ContainerItem containerItem) {
        super(textItem);
        if (containerItem != null) this.itemsCycleStorage.copyData(containerItem.getAllItems());
    }

    public CycleListItem(CycleListItem copy) {
        super(copy);
        if (copy != null) {
            this.itemsCycleStorage.copyData(copy.getAllItems());
            this.currentItemPosition = copy.currentItemPosition;
            this.tickBehavior = copy.tickBehavior;
        }
    }

    @Override
    public ItemType getItemType() {
        return ItemType.CYCLE_LIST;
    }

    @Override
    public Item getCurrentItem() {
        if (size() == 0) {
            return null;
        }

        if (currentItemPosition > size()-1) {
            currentItemPosition = 0;
        }

        try {
            return getAllItems()[currentItemPosition];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void next() {
        if (size() == 0) {
            return;
        }
        currentItemPosition++;
        if (currentItemPosition >= getAllItems().length) {
            currentItemPosition = 0;
        }
        onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
        visibleChanged();
        save();
    }

    public void previous() {
        if (size() == 0) {
            return;
        }
        currentItemPosition--;
        if (currentItemPosition < 0) {
            currentItemPosition = size() - 1;
        }
        onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
        visibleChanged();
        save();
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;
        super.tick(tickSession);
        final List<Item> planned = new ArrayList<>();
        if (tickBehavior == TickBehavior.ALL) {
            planned.addAll(Arrays.asList(getAllItems()));

        } else if (tickBehavior == TickBehavior.CURRENT) {
            Item c = getCurrentItem();
            if (c != null && tickSession.isAllowed(c)) {
                planned.add(c);
            }
        } else if (tickBehavior == TickBehavior.NOT_CURRENT) {
            Item c = getCurrentItem();
            for (Item item : getAllItems()) {
                if (item != c && tickSession.isAllowed(item)) {
                    planned.add(item);
                }
            }
        }

        if (tickBehavior != TickBehavior.ALL) {
            tickSession.runWithPlannedNormalTick(planned, null, () -> ItemUtil.tickOnlyImportantTargets(tickSession, getAllItems()));
        }

        for (Item item : planned) {
            item.tick(tickSession);
        }
    }

    @Override
    protected void regenerateId() {
        super.regenerateId();
        for (Item item : getAllItems()) {
            item.regenerateId();
        }
    }

    @Override
    public CallbackStorage<OnCurrentItemStorageUpdate> getOnCurrentItemStorageUpdateCallbacks() {
        return onCurrentItemStorageUpdateCallback;
    }

    @Override
    public int getItemPosition(Item item) {
        return itemsCycleStorage.getItemPosition(item);
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnItemsStorageCallbacks() {
        return itemsCycleStorage.getOnItemsStorageCallbacks();
    }

    @Override
    public boolean isEmpty() {
        return itemsCycleStorage.isEmpty();
    }

    @Override
    public Item getItemAt(int position) {
        return itemsCycleStorage.getItemAt(position);
    }

    @Override
    public Item getItemById(UUID itemId) {
        return itemsCycleStorage.getItemById(itemId);
    }

    @NonNull
    @Override
    public Item[] getAllItems() {
        return itemsCycleStorage.getAllItems();
    }

    @Override
    public int size() {
        return itemsCycleStorage.size();
    }

    @Override
    public int totalSize() {
        return itemsCycleStorage.totalSize();
    }

    @Override
    public void addItem(Item item) {
        Item p = getCurrentItem();
        itemsCycleStorage.addItem(item);
        if (p != getCurrentItem()) onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
    }

    @Override
    public void addItem(Item item, int position) {
        Item p = getCurrentItem();
        itemsCycleStorage.addItem(item, position);
        if (p != getCurrentItem()) onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
    }

    @Override
    public void deleteItem(Item item) {
        Item p = getCurrentItem();
        itemsCycleStorage.deleteItem(item);
        if (p != getCurrentItem()) onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        Item p = getCurrentItem();
        Item copyItem = itemsCycleStorage.copyItem(item);
        if (p != getCurrentItem()) onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
        return copyItem;
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        Item p = getCurrentItem();
        itemsCycleStorage.move(positionFrom, positionTo);
        if (p != getCurrentItem()) onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
    }

    @Getter public TickBehavior getTickBehavior() { return tickBehavior; }
    @Setter public void setTickBehavior(TickBehavior tickBehavior) { this.tickBehavior = tickBehavior; }

    private class CycleItemsStorage extends SimpleItemsStorage {
        public CycleItemsStorage() {
            super(new CycleListItemController());
            getOnItemsStorageCallbacks().addCallback(CallbackImportance.DEFAULT, new OnItemsStorageUpdate() {
                @Override
                public Status onAdded(Item item, int pos) {
                    onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
                    return Status.NONE;
                }

                @Override
                public Status onPostDeleted(Item item, int pos) {
                    onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
                    return Status.NONE;
                }

                @Override
                public Status onMoved(Item item, int from, int pos) {
                    onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
                    return Status.NONE;
                }

                @Override
                public Status onUpdated(Item item, int pos) {
                    if (item == getCurrentItem()) onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
                    return Status.NONE;
                }
            });
        }

        @Override
        public void save() {
            CycleListItem.this.save();
        }
    }

    private class CycleListItemController extends ItemController {
        @Override
        public void delete(Item item) {
            CycleListItem.this.deleteItem(item);
        }

        @Override
        public void save(Item item) {
            CycleListItem.this.save();
        }

        @Override
        public void updateUi(Item item) {
            CycleListItem.this.getOnItemsStorageCallbacks().run(((callbackStorage, callback) -> callback.onUpdated(item, getItemPosition(item))));
        }

        @Override
        public ItemsStorage getParentItemsStorage(Item item) {
            return CycleListItem.this;
        }

        @Override
        public UUID generateId(Item item) {
            return ItemUtil.controllerGenerateItemId(getRoot(), item);
        }

        @Override
        public ItemsRoot getRoot() {
            return CycleListItem.this.getRoot();
        }
    }

    public enum TickBehavior {
        ALL,
        NOTHING,
        CURRENT,
        NOT_CURRENT
    }
}
