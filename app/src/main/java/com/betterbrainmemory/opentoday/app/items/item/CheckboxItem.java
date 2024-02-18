package com.betterbrainmemory.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.betterbrainmemory.opentoday.app.App;
import com.betterbrainmemory.opentoday.app.data.Cherry;
import com.betterbrainmemory.opentoday.util.annotation.Getter;
import com.betterbrainmemory.opentoday.util.annotation.Setter;

public class CheckboxItem extends TextItem {
    private static final String TAG = "CheckboxItem";
    public static final CheckboxItemCodec CODEC = new CheckboxItemCodec();
    public static final ItemFactory<CheckboxItem> FACTORY = new CheckboxItemFactory();


    private boolean checked;

    public CheckboxItem() {
        super();
    }

    public CheckboxItem(String text, boolean checked) {
        super(text);
        this.checked = checked;
    }

    // Append
    public CheckboxItem(TextItem textItem, boolean checked) {
        super(textItem);
        this.checked = checked;
    }

    // Copy
    public CheckboxItem(@Nullable CheckboxItem copy) {
        super(copy);
        if (copy != null) {
            this.checked = copy.checked;
        }
    }

    @Override
    protected void updateStat() {
        super.updateStat();
        getStat().setChecked(isChecked());
    }

    @Getter public boolean isChecked() { return checked; }
    @Setter public void setChecked(boolean s) {
        this.checked = s;
        updateStat();

        // TODO: 13.06.2023 fix this crunch.
        // instantly tick in setChecked for instantly-filtergroup-reload
        if (isAttached() && ItemUtil.isTypeContainsInParents(this, FilterGroupItem.class)) {
            App.get().getTickThread().instantlyCheckboxTick(this);
        }
    }



    // Import - Export - Factory
    public static class CheckboxItemCodec extends TextItemCodec {
        private static final String KEY_CHECKED = "checked";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            final CheckboxItem checkboxItem = (CheckboxItem) item;
            return super.exportItem(checkboxItem)
                    .put(KEY_CHECKED, checkboxItem.checked);
        }

        private final CheckboxItem DEFAULT_VALUES = new CheckboxItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            final CheckboxItem checkboxItem = fallback(item, CheckboxItem::new);
            super.importItem(cherry, checkboxItem);

            checkboxItem.checked = cherry.optBoolean(KEY_CHECKED, DEFAULT_VALUES.checked);
            checkboxItem.getStat().setChecked(checkboxItem.checked);
            return checkboxItem;
        }
    }

    private static class CheckboxItemFactory implements ItemFactory<CheckboxItem> {
        @Override
        public CheckboxItem create() {
            return new CheckboxItem("", false);
        }

        @Override
        public CheckboxItem copy(Item item) {
            return new CheckboxItem((CheckboxItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof CheckboxItem checkboxItem) {
                return Transform.Result.allow(() -> new CheckboxItem(checkboxItem));

            } else if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new CheckboxItem(textItem, false));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
