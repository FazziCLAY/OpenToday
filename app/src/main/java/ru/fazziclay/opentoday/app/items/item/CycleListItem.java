package ru.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.DataTransferPacket;
import ru.fazziclay.opentoday.app.items.ItemIEManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.ContainerItem;
import ru.fazziclay.opentoday.app.items.SimpleItemStorage;
import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdateRunnable;
import ru.fazziclay.opentoday.callback.CallbackImportance;

public class CycleListItem extends TextItem implements ContainerItem {
    // START - Save
    public final static CycleListItemIETool IE_TOOL = new CycleListItemIETool();
    public static class CycleListItemIETool extends TextItem.TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            CycleListItem cycleListItem = (CycleListItem) item;
            return super.exportItem(item)
                    .put("currentItemPosition", cycleListItem.currentItemPosition)
                    .put("itemsCycle", ItemIEManager.exportItemList(cycleListItem.itemsCycleStorage.exportData().items))
                    .put("tickBehavior", cycleListItem.tickBehavior.name());
        }

        private final CycleListItem defaultValues = new CycleListItem();
        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            CycleListItem cycleListItem = item != null ? (CycleListItem) item : new CycleListItem();

            // Items cycle
            JSONArray jsonItemsCycle = json.getJSONArray("itemsCycle");
            if (jsonItemsCycle == null) jsonItemsCycle = new JSONArray();
            DataTransferPacket dataTransferPacket = new DataTransferPacket();
            dataTransferPacket.items = ItemIEManager.importItemList(jsonItemsCycle);
            cycleListItem.itemsCycleStorage.importData(dataTransferPacket);

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

    public static CycleListItem createEmpty() {
        return new CycleListItem("");
    }

    @SaveKey(key = "itemsCycle") @RequireSave private final SimpleItemStorage itemsCycleStorage = new CycleItemStorage();
    @SaveKey(key = "currentItemPosition") @RequireSave private int currentItemPosition = 0;
    @SaveKey(key = "tickBehavior") @RequireSave private TickBehavior tickBehavior = TickBehavior.CURRENT;

    protected CycleListItem() {}

    public CycleListItem(String text) {
        super(text);
    }

    public CycleListItem(TextItem textItem) {
        super(textItem);
    }

    public CycleListItem(CycleListItem copy) {
        super(copy);
        DataTransferPacket copyData = copy.itemsCycleStorage.exportData();
        DataTransferPacket newData = new DataTransferPacket();
        try {
            newData.items = ItemIEManager.importItemList(ItemIEManager.exportItemList(copyData.items));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.itemsCycleStorage.importData(newData);

        this.currentItemPosition = copy.currentItemPosition;
        this.tickBehavior = copy.tickBehavior;
    }

    public Item getCurrentItem() {
        if (currentItemPosition > itemsCycleStorage.size()-1) {
            currentItemPosition = 0;
        }

        try {
            return itemsCycleStorage.getAllItems()[currentItemPosition];
        } catch (Exception i) {
            return null;
        }
    }

    public void next() {
        currentItemPosition++;
        if (currentItemPosition >= itemsCycleStorage.getAllItems().length) {
            currentItemPosition = 0;
        }
        save();
        visibleChanged();
    }

    public void previous() {
        if (itemsCycleStorage.size()-1 < currentItemPosition) {
            return;
        }
        currentItemPosition--;
        if (currentItemPosition < 0) {
            currentItemPosition = itemsCycleStorage.size() - 1;
        }
        save();
        visibleChanged();
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
    public Item[] getAllItems() {
        return itemsCycleStorage.getAllItems();
    }

    @Getter public ItemStorage getItemsCycleStorage() { return itemsCycleStorage; }
    @Getter public TickBehavior getTickBehavior() { return tickBehavior; }
    @Setter public void setTickBehavior(TickBehavior tickBehavior) { this.tickBehavior = tickBehavior; }

    private class CycleItemStorage extends SimpleItemStorage {
        public CycleItemStorage() {
            super();
            getOnUpdateCallbacks().addCallback(CallbackImportance.DEFAULT, new OnItemStorageUpdateRunnable(CycleListItem.this::visibleChanged));
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
