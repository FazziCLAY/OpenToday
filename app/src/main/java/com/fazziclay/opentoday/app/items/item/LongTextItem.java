package com.fazziclay.opentoday.app.items.item;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;

public class LongTextItem extends TextItem {
    // START - Save
    public final static LongTextItemCodec CODEC = new LongTextItemCodec();
    public static class LongTextItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
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
        public Item importItem(@NonNull Cherry cherry, Item item) {
            LongTextItem longTextItem = item != null ? (LongTextItem) item : new LongTextItem();
            super.importItem(cherry, longTextItem);
            longTextItem.longText = cherry.optString("longText", defaultValues.longText);
            longTextItem.longTextColor = cherry.optInt("longTextColor", defaultValues.longTextColor);
            longTextItem.isCustomLongTextColor = cherry.optBoolean("isCustomLongTextColor", defaultValues.isCustomLongTextColor);
            longTextItem.isLongTextClickableUrls = cherry.optBoolean("isLongTextClickableUrls", defaultValues.isLongTextClickableUrls);
            longTextItem.longTextSize = cherry.optInt("longTextSize", defaultValues.longTextSize);
            longTextItem.isCustomLongTextSize = cherry.optBoolean("isCustomLongTextSize", defaultValues.isCustomLongTextSize);
            return longTextItem;
        }
    }
    // END - Save

    private static final String DEFAULT_LONG_TEXT_COLOR = "#00dd00";

    @NonNull
    public static LongTextItem createEmpty() {
        return new LongTextItem("", "");
    }

    @SaveKey(key = "longText") @RequireSave
    private String longText;
    @SaveKey(key = "longTextColor") @RequireSave private int longTextColor = Color.parseColor(DEFAULT_LONG_TEXT_COLOR);
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

    @Override
    public ItemType getItemType() {
        return ItemType.LONG_TEXT;
    }

    @Setter
    public void setLongTextColor(int longTextColor) {this.longTextColor = longTextColor;}
    @Getter
    public int getLongTextColor() {return longTextColor;}

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
