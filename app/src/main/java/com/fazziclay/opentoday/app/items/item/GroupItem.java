package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.TickSession;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.ItemsUtils;
import com.fazziclay.opentoday.app.items.SimpleItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class GroupItem extends TextItem implements ContainerItem, ItemsStorage {
    // START - Save
    public final static GroupItemIETool IE_TOOL = new GroupItemIETool();
    public static class GroupItemIETool extends TextItem.TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            GroupItem groupItem = (GroupItem) item;
            return super.exportItem(item)
                    .put("items", ItemIEUtil.exportItemList(groupItem.getAllItems()));
        }

        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            GroupItem groupItem = item != null ? (GroupItem) item : new GroupItem();
            super.importItem(json, groupItem);

            // Items
            JSONArray jsonItems = json.optJSONArray("items");
            if (jsonItems == null) jsonItems = new JSONArray();
            groupItem.itemsStorage.importData(ItemIEUtil.importItemList(jsonItems));

            return groupItem;
        }
    }
    // END - Save

    @NonNull
    public static GroupItem createEmpty() {
        return new GroupItem("");
    }

    @SaveKey(key = "items") @RequireSave private final SimpleItemsStorage itemsStorage = new GroupItemsStorage();

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

    // Append
    public GroupItem(TextItem textItem, ContainerItem containerItem) {
        super(textItem);
        if (containerItem != null) this.itemsStorage.importData(ItemsUtils.copy(containerItem.getAllItems()));
    }

    // Copy
    public GroupItem(GroupItem copy) {
        super(copy);
        this.itemsStorage.importData(ItemsUtils.copy(copy.getAllItems()));
    }

    @Override
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        itemsStorage.tick(tickSession);
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
        return itemsStorage.getItemPosition(item);
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnUpdateCallbacks() {
        return itemsStorage.getOnUpdateCallbacks();
    }

    @Override
    public Item getItemById(UUID itemId) {
        return itemsStorage.getItemById(itemId);
    }

    @NonNull
    @Override
    public Item[] getAllItems() {
        return itemsStorage.getAllItems();
    }

    @Override
    public int size() {
        return itemsStorage.size();
    }

    @Override
    public void addItem(Item item) {
        itemsStorage.addItem(item);
    }

    @Override
    public void addItem(Item item, int position) {
        itemsStorage.addItem(item, position);
    }

    @Override
    public void deleteItem(Item item) {
        itemsStorage.deleteItem(item);
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        return itemsStorage.copyItem(item);
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        itemsStorage.move(positionFrom, positionTo);
    }


    private class GroupItemsStorage extends SimpleItemsStorage {
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
