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
import com.fazziclay.opentoday.util.annotation.Setter;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.callback.Status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CycleListItem extends TextItem implements ContainerItem, ItemsStorage, CurrentItemStorage {
    public static final CycleListItemCodec CODEC = new CycleListItemCodec();
    public static final ItemFactory<CycleListItem> FACTORY = new CycleListItemFactory();

    private final CycleItemsStorage itemsCycleStorage = new CycleItemsStorage();
    private int currentItemPosition = 0;
    private TickBehavior tickBehavior = TickBehavior.CURRENT;
    private final CallbackStorage<OnCurrentItemStorageUpdate> onCurrentItemStorageUpdateCallback = new CallbackStorage<>();

    public CycleListItem() {
        super();
    }

    public CycleListItem(@NotNull String text) {
        super(text);
    }

    // append
    public CycleListItem(@Nullable TextItem textItem) {
        super(textItem);
    }

    public CycleListItem(@Nullable TextItem textItem, @Nullable ContainerItem containerItem) {
        super(textItem);
        if (containerItem != null) this.itemsCycleStorage.copyData(containerItem.getAllItems());
    }

    // copy
    public CycleListItem(@Nullable CycleListItem copy) {
        super(copy);
        if (copy != null) {
            this.itemsCycleStorage.copyData(copy.getAllItems());
            this.currentItemPosition = copy.currentItemPosition;
            this.tickBehavior = copy.tickBehavior;
        }
    }

    @Override
    public Item getCurrentItem() {
        if (isEmpty()) {
            return null;
        }
        try {
            processOverDownHead();
            return getAllItems()[currentItemPosition];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void next() {
        if (isEmpty()) {
            return;
        }
        currentItemPosition++;
        processOverDownHead();
        onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
        save();
    }

    public void previous() {
        if (isEmpty()) {
            return;
        }
        currentItemPosition--;
        processOverDownHead();
        onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
        save();
    }

    private void processOverDownHead() {
        if (currentItemPosition >= size()) currentItemPosition = 0;
        if (currentItemPosition < 0) currentItemPosition = size() - 1;
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




    // Import - Export - Factory
    public static class CycleListItemCodec extends TextItemCodec {
        private static final String KEY_CURRENT = "cycle_current";
        private static final String KEY_ITEMS = "cycle_items";
        private static final String KEY_TICK_BEHAVIOR = "cycle_tick_behavior";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            var cycleListItem = (CycleListItem) item;
            return super.exportItem(item)
                    .put(KEY_CURRENT, cycleListItem.currentItemPosition)
                    .put(KEY_ITEMS, ItemCodecUtil.exportItemList(cycleListItem.itemsCycleStorage.getAllItems()))
                    .put(KEY_TICK_BEHAVIOR, cycleListItem.tickBehavior);
        }

        private final CycleListItem DEFAULT_VALUE = new CycleListItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            var cycleListItem = fallback(item, CycleListItem::new);
            super.importItem(cherry, cycleListItem);

            cycleListItem.currentItemPosition = cherry.optInt(KEY_CURRENT, DEFAULT_VALUE.currentItemPosition);
            cycleListItem.itemsCycleStorage.importData(ItemCodecUtil.importItemList(cherry.optOrchard(KEY_ITEMS)));
            cycleListItem.tickBehavior = cherry.optEnum(KEY_TICK_BEHAVIOR, DEFAULT_VALUE.tickBehavior);
            return cycleListItem;
        }
    }

    private static class CycleListItemFactory implements ItemFactory<CycleListItem> {
        @Override
        public CycleListItem create() {
            return new CycleListItem();
        }

        @Override
        public CycleListItem copy(Item item) {
            return new CycleListItem((CycleListItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new CycleListItem(textItem, ItemUtil.getAsContainer(from)));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
