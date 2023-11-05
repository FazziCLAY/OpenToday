package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemCodecUtil;
import com.fazziclay.opentoday.app.items.item.ItemController;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.app.items.item.SimpleItemsStorage;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.Tickable;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LocalItemsTab extends Tab implements Tickable {
    public static final LocalItemsTabCodec CODEC = new LocalItemsTabCodec();
    protected static class LocalItemsTabCodec extends TabCodec {
        @NonNull
        @Override
        public Cherry exportTab(@NonNull Tab tab) {
            return super.exportTab(tab)
                    .put("items", ItemCodecUtil.exportItemList(tab.getAllItems()));
        }

        @NonNull
        @Override
        public Tab importTab(@NonNull Cherry cherry, @Nullable Tab tab) {
            LocalItemsTab localItemsTab = tab != null ? (LocalItemsTab) tab : new LocalItemsTab();
            super.importTab(cherry, localItemsTab);
            localItemsTab.itemsStorage.importData(ItemCodecUtil.importItemList(cherry.optOrchard("items")));
            return localItemsTab;
        }
    }

    private final SimpleItemsStorage itemsStorage;

    protected LocalItemsTab() {
        itemsStorage = new SimpleItemsStorage(new LocalItemsTabController()) {
            @Override
            public void save() {
                LocalItemsTab.this.save();
            }
        };
    }

    public LocalItemsTab(String name) {
        super(name);
        itemsStorage = new SimpleItemsStorage(new LocalItemsTabController()) {
            @Override
            public void save() {
                LocalItemsTab.this.save();
            }
        };
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
        if (!tickSession.isAllowed(this)) return;

        itemsStorage.tick(tickSession);
    }

    @Override
    public int size() {
        return itemsStorage.size();
    }

    @Override
    public int totalSize() {
        return itemsStorage.totalSize();
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnItemsStorageCallbacks() {
        return itemsStorage.getOnItemsStorageCallbacks();
    }

    @Override
    public boolean isEmpty() {
        return itemsStorage.isEmpty();
    }

    @Override
    public Item getItemAt(int position) {
        return itemsStorage.getItemAt(position);
    }

    @NotNull
    @Override
    public String toString() {
        return "LocalItemsTab@[ID:"+getId()+" NAME:'"+getName()+"'}";
    }

    private class LocalItemsTabController extends ItemController {
        @Override
        public void delete(Item item) {
            LocalItemsTab.this.deleteItem(item);
        }

        @Override
        public void save(Item item) {
            LocalItemsTab.this.save();
        }

        @Override
        public void updateUi(Item item) {
            LocalItemsTab.this.getOnItemsStorageCallbacks().run(((callbackStorage, callback) -> callback.onUpdated(item, getItemPosition(item))));
        }

        @Override
        public ItemsStorage getParentItemsStorage(Item item) {
            return LocalItemsTab.this;
        }

        @Override
        public UUID generateId(Item item) {
            return ItemUtil.controllerGenerateItemId(getRoot(), item);
        }

        @Override
        public ItemsRoot getRoot() {
            return LocalItemsTab.this.getRoot();
        }
    }
}
