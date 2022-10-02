package ru.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.DataTransferPacket;
import ru.fazziclay.opentoday.app.items.ItemIEUtil;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.ContainerItem;
import ru.fazziclay.opentoday.app.items.SimpleItemStorage;
import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdate;
import ru.fazziclay.opentoday.callback.CallbackStorage;

public class GroupItem extends TextItem implements ContainerItem, ItemStorage {
    // START - Save
    public final static GroupItemIETool IE_TOOL = new GroupItemIETool();
    public static class GroupItemIETool extends TextItem.TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            GroupItem groupItem = (GroupItem) item;
            return super.exportItem(item)
                    .put("items", ItemIEUtil.exportItemList(groupItem.itemStorage.exportData().items));
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
            dataTransferPacket.items = ItemIEUtil.importItemList(jsonItems);
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
            newData.items = ItemIEUtil.importItemList(ItemIEUtil.exportItemList(copyData.items));
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
    public int getItemPosition(Item item) {
        return itemStorage.getItemPosition(item);
    }

    @Override
    public CallbackStorage<OnItemStorageUpdate> getOnUpdateCallbacks() {
        return itemStorage.getOnUpdateCallbacks();
    }

    @Override
    public Item[] getAllItems() {
        return itemStorage.getAllItems();
    }

    @Override
    public int size() {
        return itemStorage.size();
    }

    @Override
    public void addItem(Item item) {
        itemStorage.addItem(item);
    }

    @Override
    public void deleteItem(Item item) {
        itemStorage.deleteItem(item);
    }

    @Override
    public Item copyItem(Item item) {
        return itemStorage.copyItem(item);
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        itemStorage.move(positionFrom, positionTo);
    }

    @Getter public ItemStorage getItemStorage() { return itemStorage; }

    private class GroupItemStorage extends SimpleItemStorage {
        @Override
        public void save() {
            GroupItem.this.save();
        }

        @Override
        public void deleteItem(Item item) {
            App.get().getItemManager().deselectItem(item); // TODO: 31.08.2022 other fix??  !!BUGFIX!!
            super.deleteItem(item);
        }
    }
}
