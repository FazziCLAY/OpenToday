package ru.fazziclay.opentoday.app.items.item;

import android.graphics.Color;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.annotation.Setter;

public class LongTextItem extends TextItem {
    // START - Save
    public final static LongTextItemIETool IE_TOOL = new LongTextItemIETool();
    public static class LongTextItemIETool extends TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            LongTextItem longTextItem = (LongTextItem) item;
            return super.exportItem(longTextItem)
                    .put("longText", longTextItem.longText)
                    .put("longTextColor", longTextItem.longTextColor)
                    .put("isCustomLongTextColor", longTextItem.isCustomLongTextColor)
                    .put("isLongTextClickableUrls", longTextItem.isLongTextClickableUrls)
                    .put("longTextSize", longTextItem.longTextSize)
                    .put("isCustomLongTextSize", longTextItem.isCustomLongTextSize);
        }

        private final LongTextItem defaultValues = new LongTextItem();
        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            LongTextItem longTextItem = item != null ? (LongTextItem) item : new LongTextItem();
            super.importItem(json, longTextItem);
            longTextItem.longText = json.optString("longText", defaultValues.longText);
            longTextItem.longTextColor = json.optInt("longTextColor", defaultValues.longTextColor);
            longTextItem.isCustomLongTextColor = json.optBoolean("isCustomLongTextColor", defaultValues.isCustomLongTextColor);
            longTextItem.isLongTextClickableUrls = json.optBoolean("isLongTextClickableUrls", defaultValues.isLongTextClickableUrls);
            longTextItem.longTextSize = json.optInt("longTextSize", defaultValues.longTextSize);
            longTextItem.isCustomLongTextSize = json.optBoolean("isCustomLongTextSize", defaultValues.isCustomLongTextSize);
            return longTextItem;
        }
    }
    // END - Save

    @NonNull
    public static LongTextItem createEmpty() {
        return new LongTextItem("", "");
    }

    @SaveKey(key = "longText") @RequireSave private String longText;
    @SaveKey(key = "longTextColor") @RequireSave private int longTextColor;
    @SaveKey(key = "isCustomLongTextColor") @RequireSave private boolean isCustomLongTextColor = false;
    @SaveKey(key = "isLongTextClickableUrls") @RequireSave private boolean isLongTextClickableUrls = false;
    @SaveKey(key = "longTextSize") @RequireSave private int longTextSize = 20;
    @SaveKey(key = "isCustomLongTextSize") @RequireSave private boolean isCustomLongTextSize = false;


    protected LongTextItem() {
        super();
    }

    public LongTextItem(String text, String longText) {
        super(text);
        this.longText = longText;
    }

    // Append
    public LongTextItem(TextItem textItem, String longText) {
        super(textItem);
        this.longText = longText;
    }

    // Copy
    public LongTextItem(LongTextItem copy) {
        super(copy);
        this.longText = copy.longText;
        this.longTextColor = copy.longTextColor;
        this.isCustomLongTextColor = copy.isCustomLongTextColor;
        this.isLongTextClickableUrls = copy.isLongTextClickableUrls;
        this.longTextSize = copy.longTextSize;
        this.isCustomLongTextSize = copy.isCustomLongTextSize;
    }

    @Setter public void setLongTextColor(int longTextColor) {this.longTextColor = longTextColor;}
    @Getter public int getLongTextColor() {return longTextColor;}

    @Getter public boolean isCustomLongTextColor() {return isCustomLongTextColor;}
    @Setter public void setCustomLongTextColor(boolean b) {this.isCustomLongTextColor = b;}

    @Setter public void setLongTextClickableUrls(boolean checked) {this.isLongTextClickableUrls = checked;}
    @Getter public boolean isLongTextClickableUrls() {return isLongTextClickableUrls;}

    @Getter public String getLongText() { return longText; }
    @Setter public void setLongText(String s) { this.longText = s; }

    @Getter public int getLongTextSize() {return longTextSize;}
    @Setter public void setLongTextSize(int longTextSize) {this.longTextSize = longTextSize;}

    @Setter public boolean isCustomLongTextSize() {return isCustomLongTextSize;}
    @Setter public void setCustomLongTextSize(boolean customLongTextSize) {isCustomLongTextSize = customLongTextSize;}
}
