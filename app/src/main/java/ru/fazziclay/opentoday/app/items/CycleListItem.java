package ru.fazziclay.opentoday.app.items;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.callback.Status;

public class CycleListItem extends TextItem {
    // START - Save
    protected final static CycleListItemIETool IE_TOOL = new CycleListItemIETool();
    protected static class CycleListItemIETool extends TextItem.TextItemIETool {
        protected JSONObject exportItem(Item item) throws Exception {
            CycleListItem cycleListItem = (CycleListItem) item;
            return super.exportItem(item)
                    .put("currentItemPosition", cycleListItem.currentItemPosition)
                    .put("itemsCycle", ItemIEManager.exportItemList(cycleListItem.itemsCycleStorage.exportData().items));
        }

        private final CycleListItem defaultValues = new CycleListItem("<import_error>");
        protected Item importItem(JSONObject json) throws Exception {
            CycleListItem o = new CycleListItem((TextItem) super.importItem(json));

            JSONArray jsonItemsCycle = json.getJSONArray("itemsCycle");
            if (jsonItemsCycle == null) jsonItemsCycle = new JSONArray();
            DataTransferPacket dataTransferPacket = new DataTransferPacket();
            dataTransferPacket.items = ItemIEManager.importItemList(jsonItemsCycle);
            o.itemsCycleStorage.importData(dataTransferPacket);
            o.currentItemPosition = json.optInt("currentItemPosition", defaultValues.currentItemPosition);
            return o;
        }
    }

    private Status _storageUpdates() {
        updateUi();
        return new Status.Builder().build();
    }
    // END - Save

    @JSONName(name = "itemsCycle") @RequireSave protected SimpleItemStorage itemsCycleStorage;
    @JSONName(name = "currentItemPosition") @RequireSave protected int currentItemPosition = 0;

    public CycleListItem(String text) {
        super(text);
        itemsCycleStorage = new CycleItemStorage();
    }

    public CycleListItem(TextItem textItem) {
        super(textItem);
        itemsCycleStorage = new CycleItemStorage();
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

    public ItemStorage getItemsCycleStorage() {
        return itemsCycleStorage;
    }

    @Override
    public void tick() {
        super.tick();
        Item c = getCurrentItem();
        if (c != null) c.tick();
    }

    private class CycleItemStorage extends SimpleItemStorage {
        public CycleItemStorage() {
            super();
            registerCallbacks();
        }

        private void registerCallbacks() {
            getOnItemAddedCallbackStorage().addCallback(CallbackImportance.DEFAULT, (item, position) -> CycleListItem.this._storageUpdates());
            getOnItemDeletedCallbackStorage().addCallback(CallbackImportance.DEFAULT, (item, position) -> CycleListItem.this._storageUpdates());
            getOnItemMovedCallbackStorage().addCallback(CallbackImportance.DEFAULT, (from, to) -> CycleListItem.this._storageUpdates());
            getOnItemUpdatedCallbackStorage().addCallback(CallbackImportance.DEFAULT, (item, position) -> CycleListItem.this._storageUpdates());
        }

        @Override
        public void save() {
            CycleListItem.this.save();
        }
    }
}
