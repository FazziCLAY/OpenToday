package com.betterbrainmemory.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.betterbrainmemory.opentoday.app.data.Cherry;
import com.betterbrainmemory.opentoday.app.items.ItemsRoot;
import com.betterbrainmemory.opentoday.app.items.ItemsStorage;
import com.betterbrainmemory.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.betterbrainmemory.opentoday.app.items.tick.TickSession;
import com.betterbrainmemory.opentoday.util.callback.CallbackStorage;

import java.util.UUID;

public class GroupItem extends TextItem implements ContainerItem, ItemsStorage {
    public static final GroupItemCodec CODEC = new GroupItemCodec();
    public static final ItemFactory<GroupItem> FACTORY = new GroupItemFactory();

    private final SimpleItemsStorage itemsStorage = new GroupItemsStorage();

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
        if (containerItem != null) this.itemsStorage.copyData(containerItem.getAllItems());
    }

    // Copy
    public GroupItem(GroupItem copy) {
        super(copy);
        if (copy != null) this.itemsStorage.copyData(copy.getAllItems());
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;

        super.tick(tickSession);
        itemsStorage.tick(tickSession);
    }

    @Override
    protected void regenerateId() {
        super.regenerateId();
        for (Item item : getAllItems()) {
            item.regenerateId();
        }
    }

    @Override
    public int getItemPosition(Item item) {
        return itemsStorage.getItemPosition(item);
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
    public int totalSize() {
        return itemsStorage.totalSize();
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
        public GroupItemsStorage() {
            super(new GroupItemController());
        }

        @Override
        public void save() {
            GroupItem.this.save();
        }
    }

    private class GroupItemController extends ItemController {
        @Override
        public void delete(Item item) {
            GroupItem.this.deleteItem(item);
        }

        @Override
        public void save(Item item) {
            GroupItem.this.save();
        }

        @Override
        public void updateUi(Item item) {
            GroupItem.this.getOnItemsStorageCallbacks().run(((callbackStorage, callback) -> callback.onUpdated(item, getItemPosition(item))));
        }

        @Override
        public ItemsStorage getParentItemsStorage(Item item) {
            return GroupItem.this;
        }

        @Override
        public UUID generateId(Item item) {
            return ItemUtil.controllerGenerateItemId(getRoot(), item);
        }

        @Override
        public ItemsRoot getRoot() {
            return GroupItem.this.getRoot();
        }
    }





    // Import - Export - Factory
    public static class GroupItemCodec extends TextItemCodec {
        private static final String KEY_ITEMS = "group_items";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            GroupItem groupItem = (GroupItem) item;
            return super.exportItem(item)
                    .put(KEY_ITEMS, ItemCodecUtil.exportItemList(groupItem.getAllItems()));
        }

        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            GroupItem groupItem = fallback(item, GroupItem::new);
            super.importItem(cherry, groupItem);

            groupItem.itemsStorage.importData(ItemCodecUtil.importItemList(cherry.optOrchard(KEY_ITEMS)));
            return groupItem;
        }
    }

    private static class GroupItemFactory implements ItemFactory<GroupItem> {
        @Override
        public GroupItem create() {
            return new GroupItem();
        }

        @Override
        public GroupItem copy(Item item) {
            return new GroupItem((GroupItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new GroupItem(textItem, ItemUtil.getAsContainer(from)));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
