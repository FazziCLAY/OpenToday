package ru.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.UUID;

import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.SimpleItemsStorage;
import ru.fazziclay.opentoday.app.items.callback.OnItemStorageUpdate;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.ItemIEUtil;
import ru.fazziclay.opentoday.callback.CallbackStorage;

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
            localItemsTab.simpleItemsStorage.importData(ItemIEUtil.importItemList(json.getJSONArray("items")));
            return localItemsTab;
        }
    }

    private final SimpleItemsStorage simpleItemsStorage = new SimpleItemsStorage() {
        @Override
        public void save() {
            LocalItemsTab.this.save();
        }
    };

    public LocalItemsTab(UUID id, String name) {
        super(id, name);
    }

    protected LocalItemsTab() {

    }

    @Override
    public void addItem(Item item) {
        simpleItemsStorage.addItem(item);
    }

    @Override
    public void deleteItem(Item item) {
        simpleItemsStorage.deleteItem(item);
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        return simpleItemsStorage.copyItem(item);
    }

    @Override
    public int getItemPosition(Item item) {
        return simpleItemsStorage.getItemPosition(item);
    }

    @Nullable
    @Override
    public Item getItemById(UUID itemId) {
        return simpleItemsStorage.getItemById(itemId);
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        simpleItemsStorage.move(positionFrom, positionTo);
    }

    @NonNull
    @Override
    public Item[] getAllItems() {
        return simpleItemsStorage.getAllItems();
    }

    @Override
    public void tick(TickSession tickSession) {
        simpleItemsStorage.tick(tickSession);
    }

    @Override
    public int size() {
        return simpleItemsStorage.size();
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemStorageUpdate> getOnUpdateCallbacks() {
        return simpleItemsStorage.getOnUpdateCallbacks();
    }
}
