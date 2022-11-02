package ru.fazziclay.opentoday.app.items.item;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.CurrentItemStorage;
import ru.fazziclay.opentoday.app.items.ItemsStorage;
import ru.fazziclay.opentoday.app.items.ItemsUtils;
import ru.fazziclay.opentoday.app.items.SimpleItemsStorage;
import ru.fazziclay.opentoday.app.items.callback.OnCurrentItemStorageUpdate;
import ru.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.callback.Status;

public class CycleListItem extends TextItem implements ContainerItem, ItemsStorage, CurrentItemStorage {
    // START - Save
    public final static CycleListItemIETool IE_TOOL = new CycleListItemIETool();
    public static class CycleListItemIETool extends TextItem.TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            CycleListItem cycleListItem = (CycleListItem) item;
            return super.exportItem(item)
                    .put("currentItemPosition", cycleListItem.currentItemPosition)
                    .put("itemsCycle", ItemIEUtil.exportItemList(cycleListItem.getAllItems()))
                    .put("tickBehavior", cycleListItem.tickBehavior.name());
        }

        private final CycleListItem defaultValues = new CycleListItem();
        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            CycleListItem cycleListItem = item != null ? (CycleListItem) item : new CycleListItem();
            super.importItem(json, cycleListItem);

            // Items cycle
            JSONArray jsonItemsCycle = json.getJSONArray("itemsCycle");
            if (jsonItemsCycle == null) jsonItemsCycle = new JSONArray();
            cycleListItem.itemsCycleStorage.importData(ItemIEUtil.importItemList(jsonItemsCycle));

            // Current item pos
            cycleListItem.currentItemPosition = json.optInt("currentItemPosition", defaultValues.currentItemPosition);

            // Tick behavior
            try {
                cycleListItem.tickBehavior = TickBehavior.valueOf(json.optString("tickBehavior", defaultValues.tickBehavior.name()).toUpperCase());
            } catch (Exception e) {
                cycleListItem.tickBehavior = defaultValues.tickBehavior;
            }
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

    public CycleListItem(CycleListItem copy) {
        super(copy);
        this.itemsCycleStorage.importData(ItemsUtils.copy(copy.getAllItems()));
        this.currentItemPosition = copy.currentItemPosition;
        this.tickBehavior = copy.tickBehavior;
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
        super.tick(tickSession);
        if (tickBehavior == TickBehavior.ALL) {
            itemsCycleStorage.tick(tickSession);
        } else if (tickBehavior == TickBehavior.CURRENT) {
            Item c = getCurrentItem();
            if (c != null) c.tick(tickSession);
        }
    }

    @Override
    public Item regenerateId() {
        super.regenerateId();
        for (Item item : getAllItems()) {
            item.regenerateId();
        }
        return this;
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
    public CallbackStorage<OnItemsStorageUpdate> getOnUpdateCallbacks() {
        return itemsCycleStorage.getOnUpdateCallbacks();
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
        return itemsCycleStorage.copyItem(item);
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
            getOnUpdateCallbacks().addCallback(CallbackImportance.DEFAULT, new OnItemsStorageUpdate() {
                @Override
                public Status onAdded(Item item, int pos) {
                    onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
                    return Status.NONE;
                }

                @Override
                public Status onDeleted(Item item, int pos) {
                    // TODO: 01.11.2022 WTF This?
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        onCurrentItemStorageUpdateCallback.run((callbackStorage, callback) -> callback.onCurrentChanged(getCurrentItem()));
                    }, 250);
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

    public enum TickBehavior {
        ALL,
        CURRENT
    }
}
