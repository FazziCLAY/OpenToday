package ru.fazziclay.opentoday.app.items.item;

import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;

public class CheckboxItem extends TextItem {
    // START - Save
    public final static CheckboxItemIETool IE_TOOL = new CheckboxItemIETool();
    public static class CheckboxItemIETool extends TextItem.TextItemIETool {
        @Override
        public JSONObject exportItem(Item item) throws Exception {
            CheckboxItem checkboxItem = (CheckboxItem) item;
            return super.exportItem(checkboxItem)
                    .put("checked", checkboxItem.checked);
        }

        private final CheckboxItem defaultValues = new CheckboxItem("<import_error>", false);
        @Override
        public Item importItem(JSONObject json) throws Exception {
            return new CheckboxItem((TextItem) super.importItem(json), json.optBoolean("checked", defaultValues.checked));
        }
    }
    // END - Save

    public static CheckboxItem createEmpty() {
        return new CheckboxItem("", false);
    }

    @JSONName(name = "checked") @RequireSave private boolean checked;

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
