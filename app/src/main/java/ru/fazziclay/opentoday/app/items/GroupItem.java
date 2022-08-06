package ru.fazziclay.opentoday.app.items;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;

public class GroupItem extends TextItem {
    protected final static GroupItemIETool IE_TOOL = new GroupItemIETool();
    protected static class GroupItemIETool extends TextItem.TextItemIETool {
        @Override
        protected JSONObject exportItem(Item item) throws Exception {
            GroupItem groupItem = (GroupItem) item;
            return super.exportItem(item)
                    .put("items", ItemIEManager.exportItemList(groupItem.itemStorage.exportData().items));
        }

        private final GroupItem defaultValues = new GroupItem("<import_error>");
        @Override
        protected Item importItem(JSONObject json) throws Exception {
            GroupItem o = new GroupItem((TextItem) super.importItem(json));

            JSONArray jsonItems = json.optJSONArray("items");
            if (jsonItems == null) jsonItems = new JSONArray();
            DataTransferPacket dataTransferPacket = new DataTransferPacket();
            dataTransferPacket.items = ItemIEManager.importItemList(jsonItems);

            o.itemStorage.importData(dataTransferPacket);
            return o;
        }
    }

    //
    @JSONName(name = "items") @RequireSave protected SimpleItemStorage itemStorage;

    public GroupItem(String text) {
        super(text);
        itemStorage = new GroupItemStorage();
    }

    // append
    public GroupItem(TextItem textItem) {
        super(textItem);
        itemStorage = new GroupItemStorage();
    }

    // copy
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

    public ItemStorage getItemStorage() {
        return itemStorage;
    }

    private class GroupItemStorage extends SimpleItemStorage {
        public GroupItemStorage() {
            super();
        }

        @Override
        public void save() {
            GroupItem.this.save();
        }
    }
}
