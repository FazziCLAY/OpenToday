package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.TickSession;
import com.fazziclay.opentoday.app.items.SimpleItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemIEUtil;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.callback.Status;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class LocalItemsTab extends Tab {
    public static final LocalItemsTabIETool IE_TOOL = new LocalItemsTabIETool();
    protected static class LocalItemsTabIETool extends Tab.TabIETool {
        @NonNull
        @Override
        public JSONObject exportTab(@NonNull Tab tab) throws Exception {
            return super.exportTab(tab)
                    .put("items", ItemIEUtil.exportItemList(tab.getAllItems()));
        }

        @NonNull
        @Override
        public Tab importTab(@NonNull JSONObject json, @Nullable Tab tab) throws Exception {
            LocalItemsTab localItemsTab = tab != null ? (LocalItemsTab) tab : new LocalItemsTab();
            super.importTab(json, localItemsTab);
            localItemsTab.itemsStorage.importData(ItemIEUtil.importItemList(json.getJSONArray("items")));
            return localItemsTab;
        }
    }

    private final SimpleItemsStorage itemsStorage;

    public LocalItemsTab(UUID id, String name) {
        this(id, name, new Item[0]);
    }

    public LocalItemsTab(UUID id, String name, Item[] data) {
        super(id, name);
        itemsStorage = new SimpleItemsStorage(new ArrayList<>(Arrays.asList(data))) {
            @Override
            public void save() {
                LocalItemsTab.this.save();
            }
        };
        applyDeleteSelectionFix();
    }

    protected LocalItemsTab() {
        itemsStorage = new SimpleItemsStorage() {
            @Override
            public void save() {
                LocalItemsTab.this.save();
            }
        };
        applyDeleteSelectionFix();
    }

    private void applyDeleteSelectionFix() {
        itemsStorage.getOnUpdateCallbacks().addCallback(CallbackImportance.MIN, new OnItemsStorageUpdate() {
            @Override
            public Status onAdded(Item item, int position) {
                return Status.NONE;
            }

            @Override
            public Status onDeleted(Item item, int position) {
                App.get().getItemManager().deselectItem(item); // TODO: 31.08.2022 other fix??  !!BUGFIX!!
                return Status.NONE;
            }

            @Override
            public Status onMoved(Item item, int from, int to) {
                return Status.NONE;
            }

            @Override
            public Status onUpdated(Item item, int position) {
                return Status.NONE;
            }
        });
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
    public int getItemPosition(Item item) {
        return itemsStorage.getItemPosition(item);
    }

    @Nullable
    @Override
    public Item getItemById(UUID itemId) {
        return itemsStorage.getItemById(itemId);
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        itemsStorage.move(positionFrom, positionTo);
    }

    @NonNull
    @Override
    public Item[] getAllItems() {
        return itemsStorage.getAllItems();
    }

    @Override
    public void tick(TickSession tickSession) {
        itemsStorage.tick(tickSession);
    }

    @Override
    public int size() {
        return itemsStorage.size();
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnUpdateCallbacks() {
        return itemsStorage.getOnUpdateCallbacks();
    }
}
