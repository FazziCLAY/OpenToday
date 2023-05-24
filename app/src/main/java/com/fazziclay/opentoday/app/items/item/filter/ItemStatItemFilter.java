package com.fazziclay.opentoday.app.items.item.filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.stat.ItemStat;

public class ItemStatItemFilter extends ItemFilter {
    public static final FilterCodec CODEC = new FilterCodec() {
        private static final String KEY_DESCRIPTION = "description";
        private static final String KEY_ACTIVE_ITEMS = "activeItems";

        @NonNull
        @Override
        public Cherry exportFilter(@NonNull ItemFilter filter) {
            ItemStatItemFilter f = (ItemStatItemFilter) filter;

            return new Cherry()
                    .put(KEY_DESCRIPTION, f.description)
                    .put(KEY_ACTIVE_ITEMS, f.activeItems == null ? null : f.activeItems.exportCherry());
        }

        @NonNull
        @Override
        public ItemFilter importFilter(@NonNull Cherry cherry, @Nullable ItemFilter d) {
            ItemStatItemFilter i = new ItemStatItemFilter();

            i.description = cherry.optString(KEY_DESCRIPTION, "");
            i.activeItems = IntegerValue.importCherry(cherry.getCherry(KEY_ACTIVE_ITEMS));

            return i;
        }
    };

    private String description = "";
    private IntegerValue activeItems = null;

    public ItemStatItemFilter() {
    }

    @Override
    public boolean isFit(FitEquip fitEquip) {
        Item item = fitEquip.getCurrentItem();
        if (item == null) return true;
        ItemStat stat = item.getStat();

        return check(stat.getActiveItems(), activeItems);
    }

    private boolean check(int stat, IntegerValue val) {
        if (val == null) return true;
        return val.isFit(stat);
    }

    @Override
    public ItemFilter copy() {
        try {
            ItemStatItemFilter i = (ItemStatItemFilter) clone();
            i.activeItems = this.activeItems == null ? null : this.activeItems.clone();
            return i;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String s) {
        this.description = s;
    }

    public IntegerValue getActiveItems() {
        return activeItems;
    }

    public void setActiveItems(IntegerValue integerValue) {
        this.activeItems = integerValue;
    }
}
