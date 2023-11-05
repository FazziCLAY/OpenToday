package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;

public class CheckboxItem extends TextItem {
    // START - Save
    public final static CheckboxItemCodec CODEC = new CheckboxItemCodec();
    public static class CheckboxItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            CheckboxItem checkboxItem = (CheckboxItem) item;
            return super.exportItem(checkboxItem)
                    .put("checked", checkboxItem.checked);
        }

        private final CheckboxItem defaultValues = new CheckboxItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            CheckboxItem checkboxItem = item != null ? (CheckboxItem) item : new CheckboxItem();
            super.importItem(cherry, checkboxItem);
            checkboxItem.checked = cherry.optBoolean("checked", defaultValues.checked);
            checkboxItem.getStat().setChecked(checkboxItem.checked);
            return checkboxItem;
        }
    }
    // END - Save

    @NonNull
    public static CheckboxItem createEmpty() {
        return new CheckboxItem("", false);
    }

    @SaveKey(key = "checked") @RequireSave private boolean checked;

    protected CheckboxItem() {
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
    public CheckboxItem(CheckboxItem copy) {
        super(copy);
        this.checked = copy.checked;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.CHECKBOX;
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
        // instantly tick in setChecked for moment-filtergroup-reload
        if (isAttached() && ItemUtil.isTypeContainsInParents(this, ItemType.FILTER_GROUP)) {
            App.get().getTickThread().instantlyCheckboxTick(this);
        }
    }
}
