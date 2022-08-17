package ru.fazziclay.opentoday.app.items.item;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.DataTransferPacket;
import ru.fazziclay.opentoday.app.items.ItemIEManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.SimpleItemStorage;
import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdateRunnable;
import ru.fazziclay.opentoday.callback.CallbackImportance;

public class CycleListItem extends TextItem {
    // START - Save
    public final static CycleListItemIETool IE_TOOL = new CycleListItemIETool();
    public static class CycleListItemIETool extends TextItem.TextItemIETool {
        @Override
        public JSONObject exportItem(Item item) throws Exception {
            CycleListItem cycleListItem = (CycleListItem) item;
            return super.exportItem(item)
                    .put("currentItemPosition", cycleListItem.currentItemPosition)
                    .put("itemsCycle", ItemIEManager.exportItemList(cycleListItem.itemsCycleStorage.exportData().items))
                    .put("tickBehavior", cycleListItem.tickBehavior.name());
        }

        private final CycleListItem defaultValues = new CycleListItem("<import_error>");
        @Override
        public Item importItem(JSONObject json) throws Exception {
            CycleListItem o = new CycleListItem((TextItem) super.importItem(json));

            JSONArray jsonItemsCycle = json.getJSONArray("itemsCycle");
            if (jsonItemsCycle == null) jsonItemsCycle = new JSONArray();
            DataTransferPacket dataTransferPacket = new DataTransferPacket();
            dataTransferPacket.items = ItemIEManager.importItemList(jsonItemsCycle);
            o.itemsCycleStorage.importData(dataTransferPacket);
            o.currentItemPosition = json.optInt("currentItemPosition", defaultValues.currentItemPosition);
            try {
                o.tickBehavior = TickBehavior.valueOf(json.optString("tickBehavior", defaultValues.tickBehavior.name()).toUpperCase());
            } catch (Exception e) {
                o.tickBehavior = defaultValues.tickBehavior;
            }
            return o;
        }
    }
    // END - Save

    public static CycleListItem createEmpty() {
        return new CycleListItem("");
    }

    @JSONName(name = "itemsCycle") @RequireSave private final SimpleItemStorage itemsCycleStorage;
    @JSONName(name = "currentItemPosition") @RequireSave private int currentItemPosition = 0;
    @JSONName(name = "tickBehavior") @RequireSave private TickBehavior tickBehavior = TickBehavior.CURRENT;

    public CycleListItem(String text) {
        super(text);
        itemsCycleStorage = new CycleItemStorage();
    }

    public CycleListItem(TextItem textItem) {
        super(textItem);
        itemsCycleStorage = new CycleItemStorage();
    }

    public CycleListItem(CycleListItem copy) {
        super(copy);
        this.itemsCycleStorage = new CycleItemStorage();
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
            return itemsCycleStorage.getItems()[currentItemPosition];
        } catch (Exception i) {
            return null;
        }
    }

    public void next() {
        currentItemPosition++;
        if (currentItemPosition >= itemsCycleStorage.getItems().length) {
            currentItemPosition = 0;
        }
        save();
        updateUi();
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
        updateUi();
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

    @Getter public ItemStorage getItemsCycleStorage() { return itemsCycleStorage; }
    @Getter public TickBehavior getTickBehavior() { return tickBehavior; }
    @Setter public void setTickBehavior(TickBehavior tickBehavior) { this.tickBehavior = tickBehavior; }

    private class CycleItemStorage extends SimpleItemStorage {
        public CycleItemStorage() {
            super();
            getOnUpdateCallbacks().addCallback(CallbackImportance.DEFAULT, new OnItemStorageUpdateRunnable(CycleListItem.this::updateUi));
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
