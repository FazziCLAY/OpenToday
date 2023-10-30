package com.fazziclay.opentoday.app.items.item;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;

import org.jetbrains.annotations.NotNull;

public class TextItem extends Item {
    private static final String DEFAULT_TEXT_COLOR = "#ff0000ff";

    // START - Save
    public final static TextItemCodec CODEC = new TextItemCodec();
    public static class TextItemCodec extends ItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            TextItem textItem = (TextItem) item;
            return super.exportItem(textItem)
                    .put("text", textItem.text)
                    .put("textColor", textItem.textColor)
                    .put("customTextColor", textItem.customTextColor)
                    .put("clickableUrls", textItem.clickableUrls)
                    .put("paragraphColorize", textItem.paragraphColorize);
        }

        private final TextItem defaultValues = new TextItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            TextItem textItem = item != null ? (TextItem) item : new TextItem();
            super.importItem(cherry, textItem);
            textItem.text = cherry.optString("text", defaultValues.text);
            textItem.textColor = cherry.optInt("textColor", defaultValues.textColor);
            textItem.customTextColor = cherry.optBoolean("customTextColor", defaultValues.customTextColor);
            textItem.clickableUrls = cherry.optBoolean("clickableUrls", defaultValues.clickableUrls);
            textItem.paragraphColorize = cherry.optBoolean("paragraphColorize", defaultValues.paragraphColorize);
            return textItem;
        }
    }
    // END - Save

    @NonNull
    public static TextItem createEmpty() {
        return new TextItem("");
    }

    @NotNull @SaveKey(key = "text") @RequireSave private String text = "";
    @SaveKey(key = "textColor") @RequireSave private int textColor = Color.parseColor(DEFAULT_TEXT_COLOR);
    @SaveKey(key = "customTextColor") @RequireSave private boolean customTextColor = false;
    @SaveKey(key = "clickableUrls") @RequireSave private boolean clickableUrls = false;
    @SaveKey(key = "paragraphColorize") private boolean paragraphColorize = true;

    protected TextItem() {
        super();
    }

    public TextItem(String text) {
        this(null, text);
    }

    // Append
    public TextItem(Item item, @NonNull String text) {
        super(item);
        this.text = text;
    }

    // Copy
    public TextItem(TextItem copy) {
        super(copy);
        if (copy != null) {
            this.text = copy.text;
            this.textColor = copy.textColor;
            this.customTextColor = copy.customTextColor;
            this.clickableUrls = copy.clickableUrls;
            this.paragraphColorize = copy.paragraphColorize;
        }
    }

    @Override
    public ItemType getItemType() {
        return ItemType.TEXT;
    }

    @Override @Getter @NonNull public String getText() { return text; }
    @Setter public void setText(@NonNull String v) { this.text = v; }
    @Getter public int getTextColor() { return textColor; }
    @Setter public void setTextColor(int v) { this.textColor = v; }
    @Getter public boolean isCustomTextColor() { return customTextColor; }
    @Setter public void setCustomTextColor(boolean v) { this.customTextColor = v; }
    @Getter public boolean isClickableUrls() { return clickableUrls; }
    @Setter public void setClickableUrls(boolean clickableUrls) { this.clickableUrls = clickableUrls; }
    @Getter public boolean isParagraphColorize() {return paragraphColorize;}
    @Setter public void setParagraphColorize(boolean v) {this.paragraphColorize = v;}
}
