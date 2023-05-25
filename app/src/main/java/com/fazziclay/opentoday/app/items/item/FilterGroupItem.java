package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.tick.TickSession;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.ItemsUtils;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.filter.FilterCodecUtil;
import com.fazziclay.opentoday.app.items.item.filter.FitEquip;
import com.fazziclay.opentoday.app.items.item.filter.ItemFilter;
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class FilterGroupItem extends TextItem implements ContainerItem, ItemsStorage {
    // START - Save
    private static final String TAG = "FilterGroupItem";
    public final static FilterGroupItemCodec CODEC = new FilterGroupItemCodec();
    public static class FilterGroupItemCodec extends TextItemCodec {
        private static final String KEY_ITEMS = "items";
        private static final String KEY_TICK_BEHAVIOR = "tickBehavior";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            final FilterGroupItem filterGroupItem = (FilterGroupItem) item;

            final CherryOrchard orchard = new CherryOrchard();
            for (ItemFilterWrapper wrapper : filterGroupItem.items) {
                orchard.put(wrapper.exportWrapper());
            }

            return super.exportItem(filterGroupItem)
                    .put(KEY_ITEMS, orchard)
                    .put(KEY_TICK_BEHAVIOR, filterGroupItem.tickBehavior);
        }

        private final FilterGroupItem defaultValues = new FilterGroupItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            final FilterGroupItem filterGroupItem = item != null ? (FilterGroupItem) item : new FilterGroupItem();
            super.importItem(cherry, filterGroupItem);

            filterGroupItem.tickBehavior = cherry.optEnum(KEY_TICK_BEHAVIOR, defaultValues.tickBehavior);

            // Items
            final CherryOrchard itemsArray = cherry.optOrchard(KEY_ITEMS);
            int i = 0;
            while (i < itemsArray.length()) {
                Cherry cherryWrapper = itemsArray.getCherryAt(i);
                ItemFilterWrapper wrapper = ItemFilterWrapper.importWrapper(cherryWrapper);
                wrapper.item.setController(filterGroupItem.groupItemController);
                filterGroupItem.items.add(wrapper);
                i++;
            }

            return filterGroupItem;
        }
    }
    // END - Save

    @NonNull
    public static FilterGroupItem createEmpty() {
        return new FilterGroupItem("");
    }

    @NonNull @SaveKey(key = "items") @RequireSave private final List<ItemFilterWrapper> items = new ArrayList<>();
    @NonNull @SaveKey(key = "tickBehavior") @RequireSave private TickBehavior tickBehavior = TickBehavior.ALL;
    @NonNull private final List<ItemFilterWrapper> activeItems = new ArrayList<>();
    @NonNull private final ItemController groupItemController = new FilterGroupItemController();
    @NonNull private final CallbackStorage<OnItemsStorageUpdate> itemStorageUpdateCallbacks = new CallbackStorage<>();
    @NonNull private final FitEquip fitEquip = new FitEquip();

    protected FilterGroupItem() {
        super();
    }

    // append
    public FilterGroupItem(String text) {
        super(text);
    }

    // append
    public FilterGroupItem(TextItem textItem) {
        super(textItem);
    }

    // append
    public FilterGroupItem(TextItem textItem, ContainerItem containerItem) {
        super(textItem);
        if (containerItem != null) {
            for (Item item : containerItem.getAllItems()) {
                ItemFilterWrapper newWrapper = new ItemFilterWrapper(item, new LogicContainerItemFilter());
                newWrapper.item.attach(this.groupItemController);
                this.items.add(newWrapper);
            }
        }
    }

    // Copy
    public FilterGroupItem(FilterGroupItem copy) {
        super(copy);
        this.tickBehavior = copy.tickBehavior;
        for (ItemFilterWrapper copyWrapper : copy.items) {
            try {
                ItemFilterWrapper newWrapper = ItemFilterWrapper.importWrapper(copyWrapper.exportWrapper());
                newWrapper.item.attach(this.groupItemController);
                this.items.add(newWrapper);
            } catch (Exception e) {
                throw new RuntimeException("Copy exception", e);
            }
        }
    }

    public void setTickBehavior(@NonNull TickBehavior tickBehavior) {
        this.tickBehavior = tickBehavior;
    }

    @NonNull
    public TickBehavior getTickBehavior() {
        return tickBehavior;
    }

    @Nullable
    public ItemFilter getItemFilter(Item item) {
        for (ItemFilterWrapper wrapper : items) {
            if (wrapper.item == item) return wrapper.filter;
        }

        return null;
    }

    public void setItemFilter(Item item, ItemFilter itemFilter) {
        if (itemFilter == null) throw new NullPointerException("filter can't be null");

        for (ItemFilterWrapper wrapper : items) {
            if (wrapper.item == item) wrapper.filter = itemFilter;
        }
    }

    public Item[] getActiveItems() {
        List<Item> ret = new ArrayList<>();
        for (ItemFilterWrapper activeItem : activeItems) {
            ret.add(activeItem.item);
        }
        return ret.toArray(new Item[0]);
    }


    public boolean isActiveItem(Item item) {
        for (ItemFilterWrapper activeItem : activeItems) {
            if (activeItem.item == item) return true;
        }
        return false;
    }

    @NonNull
    @Override
    public Item[] getAllItems() {
        List<Item> ret = new ArrayList<>();
        for (ItemFilterWrapper wrapper : items) {
            ret.add(wrapper.item);
        }
        return ret.toArray(new Item[0]);
    }

    @Override
    public Item regenerateId() {
        super.regenerateId();
        for (ItemFilterWrapper item : items) {
            item.item.regenerateId();
        }
        return this;
    }

    // Item storage
    @Override
    public int size() {
        return items.size();
    }

    private void addItem(ItemFilterWrapper item) {
        addItem(item, items.size());
    }

    private void addItem(ItemFilterWrapper item, int position) {
        ItemsUtils.checkAllowedItems(item.item);
        ItemsUtils.checkAttached(item.item);
        item.item.attach(groupItemController);
        items.add(position, item);
        itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onAdded(item.item, getItemPosition(item.item)));
        if (!recalculate(TickSession.getLatestGregorianCalendar())) {
            visibleChanged();
        }
        save();
    }
    
    @Override
    public void addItem(Item item) {
        addItem(new ItemFilterWrapper(item, new LogicContainerItemFilter()));
    }

    @Override
    public void addItem(Item item, int position) {
        addItem(new ItemFilterWrapper(item, new LogicContainerItemFilter()), position);
    }

    @Override
    public void deleteItem(Item item) {
        int position = getItemPosition(item);
        itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onPreDeleted(item, position));

        ItemFilterWrapper toDel = null;
        for (ItemFilterWrapper wrapper : items) {
            if (wrapper.item == item) toDel = wrapper;
        }

        item.detach();
        items.remove(toDel);

        if (!recalculate(TickSession.getLatestGregorianCalendar())) {
            visibleChanged();
        }
        itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onPostDeleted(item, position));
        save();
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        ItemFilter filter = getItemFilter(item);

        Item copy = ItemsRegistry.REGISTRY.get(item.getClass()).copy(item);
        ItemFilter copyFilter = filter.copy();
        addItem(new ItemFilterWrapper(copy, copyFilter), getItemPosition(item) + 1);
        return copy;
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        ItemFilterWrapper item = items.get(positionFrom);
        items.remove(item);
        items.add(positionTo, item);
        // TODO: 27.10.2022 EXPERIMENTAL CHANGES
        //Collections.swap(items, positionFrom, positionTo);
        itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onMoved(item.item, positionFrom, positionTo));

        if (!recalculate(TickSession.getLatestGregorianCalendar())) {
            visibleChanged();
        }
        save();
    }

    @Override
    public int getItemPosition(Item item) {
        int i = 0;

        for (ItemFilterWrapper wrapper : items) {
            if (wrapper.item == item) return i;
            i++;
        }

        return -1; // like as List.indexOf() not found result
    }

    @Override
    protected void updateStat() {
        super.updateStat();
        getStat().setActiveItems(activeItems.size());
        getStat().setContainerItems(items.size());
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnUpdateCallbacks() {
        return itemStorageUpdateCallbacks;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public Item getItemById(UUID itemId) {
        return Logger.dur(TAG, "getItemById (recursive)", () -> ItemsUtils.getItemByIdRecursive(getAllItems(), itemId));
    }

    @Override
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        recalculate(tickSession.getGregorianCalendar());
        updateStat();

        final List<ItemFilterWrapper> tickList;
        switch (tickBehavior) {
            case ALL:
                tickList = items;
                break;
            case ACTIVE:
                tickList = activeItems;
                break;
            case NOTHING:
                tickList = Collections.emptyList();
                break;
            case NOT_ACTIVE:
                tickList = new ArrayList<>(items);
                for (ItemFilterWrapper activeItem : activeItems) {
                    tickList.remove(activeItem);
                }
                break;

            default:
                throw new RuntimeException(TAG + ": Unexpected tickBehavior: " + tickBehavior);
        }

        // NOTE: No use 'for-loop' (self-delete item in tick => ConcurrentModificationException)
        int i = tickList.size() - 1;
        while (i >= 0) {
            Item item = tickList.get(i).item;
            if (item != null && item.isAttached()) {
                item.tick(tickSession);
            }
            i--;
        }

        recalculate(tickSession.getGregorianCalendar());
        updateStat();
    }

    public boolean recalculate(final GregorianCalendar gregorianCalendar) {
        List<ItemFilterWrapper> temps = new ArrayList<>();
        fitEquip.recycle(gregorianCalendar);

        for (ItemFilterWrapper wrapper : items) {
            fitEquip.setCurrentItem(wrapper.item);
            boolean fit = wrapper.filter.isFit(fitEquip);
            if (fit) {
                temps.add(wrapper);
            }
        }
        fitEquip.clearCurrentItem();

        boolean isUpdated = activeItems.size() != temps.size();
        if (!isUpdated) {
            int i = 0;
            for (ItemFilterWrapper temp : temps) {
                ItemFilterWrapper active = activeItems.get(i);
                if (temp != active) {
                    isUpdated = true;
                    break;
                }
                i++;
            }
        }

        if (isUpdated) {
            activeItems.clear();
            activeItems.addAll(temps);
            visibleChanged();
        }
        return isUpdated;
    }

    public static class ItemFilterWrapper {
        private final Item item;
        private ItemFilter filter;

        public ItemFilterWrapper(Item item, ItemFilter filter) {
            this.item = item;
            this.filter = filter;
        }

        public Cherry exportWrapper() {
            return new Cherry()
                    .put("item", ItemCodecUtil.exportItem(item))
                    .put("filter", FilterCodecUtil.exportFilter(filter));
        }

        public static ItemFilterWrapper importWrapper(Cherry cherry) {
            return new ItemFilterWrapper(ItemCodecUtil.importItem(cherry.getCherry("item")), FilterCodecUtil.importFilter(cherry.getCherry("filter")));
        }
    }

    private class FilterGroupItemController extends ItemController {
        @Override
        public void delete(Item item) {
            FilterGroupItem.this.deleteItem(item);
        }

        @Override
        public void save(Item item) {
            FilterGroupItem.this.save();
        }

        @Override
        public void updateUi(Item item) {
            itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onUpdated(item, getItemPosition(item)));
            FilterGroupItem.this.visibleChanged();
        }

        @Override
        public ItemsStorage getParentItemsStorage(Item item) {
            return FilterGroupItem.this;
        }
    }

    public enum TickBehavior {
        ALL,
        NOTHING,
        ACTIVE,
        NOT_ACTIVE
    }
}
