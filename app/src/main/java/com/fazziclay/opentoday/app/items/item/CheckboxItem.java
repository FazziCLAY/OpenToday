package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.annotation.Getter;
import com.fazziclay.opentoday.annotation.RequireSave;
import com.fazziclay.opentoday.annotation.SaveKey;
import com.fazziclay.opentoday.annotation.Setter;

import org.json.JSONObject;

public class CheckboxItem extends TextItem {
    // START - Save
    public final static CheckboxItemIETool IE_TOOL = new CheckboxItemIETool();
    public static class CheckboxItemIETool extends TextItem.TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            CheckboxItem checkboxItem = (CheckboxItem) item;
            return super.exportItem(checkboxItem)
                    .put("checked", checkboxItem.checked);
        }

        private final CheckboxItem defaultValues = new CheckboxItem();
        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            CheckboxItem checkboxItem = item != null ? (CheckboxItem) item : new CheckboxItem();
            super.importItem(json, checkboxItem);
            checkboxItem.checked = json.optBoolean("checked", defaultValues.checked);
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

    @Getter public boolean isChecked() { return checked; }
    @Setter public void setChecked(boolean s) { this.checked = s; }
}
