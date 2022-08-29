package ru.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.DataTransferPacket;
import ru.fazziclay.opentoday.app.items.ItemIEManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.ContainerItem;
import ru.fazziclay.opentoday.app.items.SimpleItemStorage;

public class GroupItem extends TextItem implements ContainerItem {
    // START - Save
    public final static GroupItemIETool IE_TOOL = new GroupItemIETool();
    public static class GroupItemIETool extends TextItem.TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            GroupItem groupItem = (GroupItem) item;
            return super.exportItem(item)
                    .put("items", ItemIEManager.exportItemList(groupItem.itemStorage.exportData().items));
        }

        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            GroupItem groupItem = item != null ? (GroupItem) item : new GroupItem();
            super.importItem(json, groupItem);

            // Items
            JSONArray jsonItems = json.optJSONArray("items");
            if (jsonItems == null) jsonItems = new JSONArray();
            DataTransferPacket dataTransferPacket = new DataTransferPacket();
            dataTransferPacket.items = ItemIEManager.importItemList(jsonItems);
            groupItem.itemStorage.importData(dataTransferPacket);

            return groupItem;
        }
    }
    // END - Save

    public static GroupItem createEmpty() {
        return new GroupItem("");
    }

    @SaveKey(key = "items") @RequireSave private final SimpleItemStorage itemStorage = new GroupItemStorage();

    protected GroupItem() {
        super();
    }

    public GroupItem(String text) {
        super(text);
    }

    // Append
    public GroupItem(TextItem textItem) {
        super(textItem);
    }

    // Copy
    public GroupItem(GroupItem copy) {
        super(copy);
        DataTransferPacket copyData = copy.itemStorage.exportData();
        DataTransferPacket newData = new DataTransferPacket();
        try {
            newData.items = ItemIEManager.importItemList(ItemIEManager.exportItemList(copyData.items));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.itemStorage.importData(newData);
    }

    @Override
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        itemStorage.tick(tickSession);
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
        return itemStorage.getAllItems();
    }

    @Getter public ItemStorage getItemStorage() { return itemStorage; }

    private class GroupItemStorage extends SimpleItemStorage {
        @Override
        public void save() {
            GroupItem.this.save();
        }
    }
}
