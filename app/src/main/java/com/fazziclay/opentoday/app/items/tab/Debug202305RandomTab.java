package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Readonly;
import com.fazziclay.opentoday.app.items.SimpleItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemType;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import java.util.UUID;

public class Debug202305RandomTab extends Tab implements Readonly {
    public static final TabCodec CODEC = new TabCodec() {
        @NonNull
        @Override
        public Cherry exportTab(@NonNull Tab tab) {
            return super.exportTab(tab);
        }

        @NonNull
        @Override
        public Tab importTab(@NonNull Cherry cherry, @Nullable Tab tab) {
            Debug202305RandomTab t = tab == null ? new Debug202305RandomTab() : (Debug202305RandomTab) tab;
            super.importTab(cherry, t);
            return t;
        }
    };

    private final SimpleItemsStorage itemsStorage = new SimpleItemsStorage() {
        @Override
        public void save() {
            // do nothing in Debug tab
        }
    };

    public Debug202305RandomTab() {
        super("Debug202305RandomTab");
    }

    @Override
    public void addItem(Item item) {

    }

    @Override
    public void addItem(Item item, int position) {

    }

    @Override
    public void deleteItem(Item item) {

    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        return new TextItem("Coped item lol");
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

    }


    @NonNull
    @Override
    public Item[] getAllItems() {
        return itemsStorage.getAllItems();
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;

        tick111(itemsStorage);
        for (Item item : getAllItems()) {
            if (item instanceof ItemsStorage) {
                tick111((ItemsStorage) item);
            }
        }
    }

    public void tick111(ItemsStorage itemsStorage) {
        byte mode = (byte) (RandomUtil.nextBoolean() ? 0 : 1); // 0 remove;  1 add
        if (itemsStorage.isEmpty()) mode = 1;

        if (mode == 0) {
            itemsStorage.deleteItem(itemsStorage.getAllItems()[RandomUtil.nextInt(itemsStorage.size())]);
        } else {
            Item item = ItemsRegistry.REGISTRY.get(ItemType.values()[RandomUtil.nextInt(ItemType.values().length)]).create();
            item.setViewBackgroundColor(RandomUtil.nextInt());
            item.setViewCustomBackgroundColor(true);
            item.setMinimize(RandomUtil.nextBoolean());
            if (item instanceof TextItem textItem) {
                textItem.setText(RandomUtil.nextIntPositive()+"");
            }
            if (item instanceof CounterItem counterItem) {
                counterItem.setCounter(RandomUtil.nextInt());
            }
            if (item instanceof CycleListItem cycleListItem) {
                cycleListItem.next();
            }
            if (item instanceof CheckboxItem checkboxItem) {
                checkboxItem.setChecked(RandomUtil.nextBoolean());
            }
            itemsStorage.addItem(item);
        }
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

    @Override
    public boolean isEmpty() {
        return itemsStorage.isEmpty();
    }
}
