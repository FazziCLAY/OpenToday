package ru.fazziclay.opentoday.app.items.item;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.app.items.DataTransferPacket;
import ru.fazziclay.opentoday.app.items.ItemIEManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.SimpleItemStorage;

public class GroupItem extends TextItem {
    // START - Save
    public final static GroupItemIETool IE_TOOL = new GroupItemIETool();
    public static class GroupItemIETool extends TextItem.TextItemIETool {
        @Override
        public JSONObject exportItem(Item item) throws Exception {
            GroupItem groupItem = (GroupItem) item;
            return super.exportItem(item)
                    .put("items", ItemIEManager.exportItemList(groupItem.itemStorage.exportData().items));
        }

        private final GroupItem defaultValues = new GroupItem("<import_error>");
        @Override
        public Item importItem(JSONObject json) throws Exception {
            GroupItem o = new GroupItem((TextItem) super.importItem(json));

            JSONArray jsonItems = json.optJSONArray("items");
            if (jsonItems == null) jsonItems = new JSONArray();
            DataTransferPacket dataTransferPacket = new DataTransferPacket();
            dataTransferPacket.items = ItemIEManager.importItemList(jsonItems);

            o.itemStorage.importData(dataTransferPacket);
            return o;
        }
    }
    // END - Save

    public static GroupItem createEmpty() {
        return new GroupItem("");
    }


    @JSONName(name = "items") @RequireSave private final SimpleItemStorage itemStorage;

    public GroupItem(String text) {
        super(text);
        itemStorage = new GroupItemStorage();
    }

    // Append
    public GroupItem(TextItem textItem) {
        super(textItem);
        itemStorage = new GroupItemStorage();
    }

    // Copy
    public GroupItem(GroupItem copy) {
        super(copy);
        this.itemStorage = new GroupItemStorage();
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
    public void tick() {
        itemStorage.tick();
    }

    @Getter public ItemStorage getItemStorage() { return itemStorage; }

    private class GroupItemStorage extends SimpleItemStorage {
        @Override
        public void save() {
            GroupItem.this.save();
        }
    }
}
