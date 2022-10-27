package ru.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.ItemsStorage;
import ru.fazziclay.opentoday.app.items.ItemsUtils;
import ru.fazziclay.opentoday.app.items.SimpleItemsStorage;
import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdate;
import ru.fazziclay.opentoday.callback.CallbackStorage;

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
            groupItem.itemStorage.importData(ItemIEUtil.importItemList(jsonItems));

            return groupItem;
        }
    }
    // END - Save

    public static GroupItem createEmpty() {
        return new GroupItem("");
    }

    @SaveKey(key = "items") @RequireSave private final SimpleItemsStorage itemStorage = new GroupItemsStorage();

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
        this.itemStorage.importData(ItemsUtils.copy(copy.getAllItems()));
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

    @NonNull
    @Override
    public CallbackStorage<OnItemStorageUpdate> getOnUpdateCallbacks() {
        return itemStorage.getOnUpdateCallbacks();
    }

    @Override
    public Item getItemById(UUID itemId) {
        return itemStorage.getItemById(itemId);
    }

    @NonNull
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

    @NonNull
    @Override
    public Item copyItem(Item item) {
        return itemStorage.copyItem(item);
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        itemStorage.move(positionFrom, positionTo);
    }

    @Getter public ItemsStorage getItemStorage() { return itemStorage; }

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
