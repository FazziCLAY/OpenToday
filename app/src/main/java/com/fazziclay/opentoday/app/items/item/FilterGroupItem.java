package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.filter.FilterCodecUtil;
import com.fazziclay.opentoday.app.items.item.filter.FitEquip;
import com.fazziclay.opentoday.app.items.item.filter.ItemFilter;
import com.fazziclay.opentoday.app.items.item.filter.LogicContainerItemFilter;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;
import com.fazziclay.opentoday.util.callback.CallbackStorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

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
                ItemFilterWrapper newWrapper = new ItemFilterWrapper(ItemUtil.copyItem(item), new LogicContainerItemFilter());
                this.items.add(newWrapper);
                newWrapper.item.attach(this.groupItemController);
            }
        }
    }

    // Copy
    public FilterGroupItem(FilterGroupItem copy) {
        super(copy);
        if (copy != null) {
            this.tickBehavior = copy.tickBehavior;
            for (ItemFilterWrapper copyWrapper : copy.items) {
                ItemFilterWrapper newWrapper = ItemFilterWrapper.importWrapper(copyWrapper.exportWrapper());
                this.items.add(newWrapper);
                newWrapper.item.attach(this.groupItemController);
            }
        }
    }

    @Override
    public ItemType getItemType() {
        return ItemType.FILTER_GROUP;
    }

    @Setter public void setTickBehavior(@NonNull TickBehavior o) {this.tickBehavior = o;}
    @Getter @NonNull public TickBehavior getTickBehavior() {
        return tickBehavior;
    }

    @Nullable
    public ItemFilter getItemFilter(@NotNull Item item) {
        for (ItemFilterWrapper wrapper : getWrappers()) {
            if (wrapper.item == item) return wrapper.filter;
        }

        return null;
    }

    public void setItemFilter(Item item, ItemFilter itemFilter) {
        if (itemFilter == null) throw new NullPointerException("ItemFilter can't be null");

        for (ItemFilterWrapper wrapper : getWrappers()) {
            if (wrapper.item == item) wrapper.filter = itemFilter;
        }
        save();
    }

    private ItemFilterWrapper[] getWrappers() {
        return items.toArray(new ItemFilterWrapper[0]);
    }

    private ItemFilterWrapper[] getActiveWrappers() {
        return activeItems.toArray(new ItemFilterWrapper[0]);
    }

    public Item[] getActiveItems() {
        List<Item> ret = new ArrayList<>();
        for (ItemFilterWrapper activeItem : getActiveWrappers()) {
            ret.add(activeItem.item);
        }
        return ret.toArray(new Item[0]);
    }

    public boolean isActiveItem(Item item) {
        for (ItemFilterWrapper activeItem : getActiveWrappers()) {
            if (activeItem.item == item) return true;
        }
        return false;
    }

    @NonNull
    @Override
    public Item[] getAllItems() {
        List<Item> ret = new ArrayList<>();
        for (ItemFilterWrapper wrapper : getWrappers()) {
            ret.add(wrapper.item);
        }
        return ret.toArray(new Item[0]);
    }

    @Override
    protected void regenerateId() {
        super.regenerateId();
        for (ItemFilterWrapper item : getWrappers()) {
            item.item.regenerateId();
        }
    }

    // Item storage
    @Override
    public int size() {
        return items.size();
    }

    @Override
    public int totalSize() {
        int c = 0;
        for (ItemFilterWrapper item : items) {
            c++;
            c+= item.item.getChildrenItemCount();
        }
        return c;
    }

    private void addItem(ItemFilterWrapper item) {
        addItem(item, items.size());
    }

    private void addItem(ItemFilterWrapper item, int position) {
        ItemUtil.throwIsBreakType(item.item);
        ItemUtil.throwIsAttached(item.item);
        items.add(position, item);
        item.item.attach(groupItemController);
        itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onAdded(item.item, getItemPosition(item.item)));
        recalculate(TickSession.getLatestGregorianCalendar());
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
        ItemFilterWrapper wrapper = getWrapperForItem(item);
        if (wrapper == null) throw new IllegalArgumentException("Provided item not attached to this FilterGroupItem.");

        int position = getWrapperPosition(wrapper);
        itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onPreDeleted(item, position));

        items.remove(wrapper);
        item.detach();

        itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onPostDeleted(item, position));

        recalculate(TickSession.getLatestGregorianCalendar());
        save();
    }

    @Nullable
    private ItemFilterWrapper getWrapperForItem(Item item) {
        for (ItemFilterWrapper wrapper : getWrappers()) {
            if (wrapper.item == item) return wrapper;
        }
        return null;
    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        ItemFilter filter = getItemFilter(item);

        Item copy = ItemUtil.copyItem(item);
        ItemFilter copyFilter = filter.copy();
        addItem(new ItemFilterWrapper(copy, copyFilter), getItemPosition(item) + 1);
        return copy;
    }

    @Override
    public void move(int positionFrom, int positionTo) {
        if (positionFrom >= size() || positionTo >= size()) throw new IndexOutOfBoundsException("positions index out bounds of items list!");
        ItemFilterWrapper item = items.get(positionFrom);
        items.remove(item);
        items.add(positionTo, item);
        itemStorageUpdateCallbacks.run((callbackStorage, callback) -> callback.onMoved(item.item, positionFrom, positionTo));

        recalculate(TickSession.getLatestGregorianCalendar());
        save();
    }

    @Override
    public int getItemPosition(Item item) {
        return items.indexOf(getWrapperForItem(item));
    }

    private int getWrapperPosition(ItemFilterWrapper wrapper) {
        return items.indexOf(wrapper);
    }

    @Override
    protected void updateStat() {
        super.updateStat();
        getStat().setActiveItems(activeItems.size());
        getStat().setContainerItems(items.size());
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnItemsStorageCallbacks() {
        return itemStorageUpdateCallbacks;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public Item getItemAt(int position) {
        return items.get(position).item;
    }

    @Override
    public Item getItemById(UUID itemId) {
        return ItemUtil.getItemByIdRecursive(getAllItems(), itemId);
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;

        super.tick(tickSession);
        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_FILTER_GROUP_TICK)) {
            profPush(tickSession, "filter_group_tick");
            recalculate(tickSession.getGregorianCalendar());
            updateStat();

            final List<ItemFilterWrapper> tickList;
            switch (tickBehavior) {
                case ALL -> tickList = items;
                case ACTIVE -> tickList = activeItems;
                case NOTHING -> tickList = Collections.emptyList();
                case NOT_ACTIVE -> {
                    tickList = new ArrayList<>(items);
                    for (ItemFilterWrapper activeItem : activeItems) {
                        tickList.remove(activeItem);
                    }
                }
                default -> throw new RuntimeException(TAG + ": Unexpected tickBehavior: " + tickBehavior);
            }

            profPop(tickSession);
            if (tickBehavior != TickBehavior.ALL) {
                tickSession.runWithPlannedNormalTick(tickList, (Function<ItemFilterWrapper, Item>) itemFilterWrapper -> itemFilterWrapper.item, () -> ItemUtil.tickOnlyImportantTargets(tickSession, getAllItems()));
            }
            // NOTE: No use 'for-loop' (self-delete item in tick => ConcurrentModificationException)
            int i = tickList.size() - 1;
            while (i >= 0) {
                Item item = tickList.get(i).item;
                if (item != null && item.isAttached() && tickSession.isAllowed(item)) {
                    item.tick(tickSession);
                }
                i--;
            }
            profPush(tickSession, "filter_group_tick");

            recalculate(tickSession.getGregorianCalendar());
            updateStat();
            profPop(tickSession);
        }
    }

    public void recalculate(final GregorianCalendar gregorianCalendar) {
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
            List<ItemFilterWrapper> oldestActive = new ArrayList<>(activeItems);
            Set<ItemFilterWrapper> toUpdate = new HashSet<>(activeItems);
            activeItems.clear();
            activeItems.addAll(temps);
            toUpdate.addAll(temps);
            for (ItemFilterWrapper itemFilterWrapper : oldestActive) {
                if (temps.contains(itemFilterWrapper)) {
                    toUpdate.remove(itemFilterWrapper);
                }
            }
            toUpdate.removeIf(itemFilterWrapper -> !itemFilterWrapper.item.isAttached());

            for (ItemFilterWrapper activeItem : toUpdate) {
                Logger.d(TAG, "recalculate: update item: " + activeItem.item);
                getOnItemsStorageCallbacks().run((callbackStorage, callback) -> callback.onUpdated(activeItem.item, getWrapperPosition(activeItem)));
            }
        }
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
        }

        @Override
        public ItemsStorage getParentItemsStorage(Item item) {
            return FilterGroupItem.this;
        }

        @Override
        public UUID generateId(Item item) {
            return ItemUtil.controllerGenerateItemId(getRoot(), item);
        }

        @Override
        public ItemsRoot getRoot() {
            return FilterGroupItem.this.getRoot();
        }
    }

    public enum TickBehavior {
        ALL,
        NOTHING,
        ACTIVE,
        NOT_ACTIVE
    }
}
