package com.fazziclay.opentoday.app.items.item;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class TextItem extends Item {
    private static final String DEFAULT_TEXT_COLOR = "#ff0000ff";

    // START - Save
    public final static TextItemIETool IE_TOOL = new TextItemIETool();
    public static class TextItemIETool extends Item.ItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            TextItem textItem = (TextItem) item;
            return super.exportItem(textItem)
                    .put("text", textItem.text)
                    .put("textColor", textItem.textColor)
                    .put("customTextColor", textItem.customTextColor)
                    .put("clickableUrls", textItem.clickableUrls);
        }

        private final TextItem defaultValues = new TextItem();
        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            TextItem textItem = item != null ? (TextItem) item : new TextItem();
            super.importItem(json, textItem);
            textItem.text = json.optString("text", defaultValues.text);
            textItem.textColor = json.optInt("textColor", defaultValues.textColor);
            textItem.customTextColor = json.optBoolean("customTextColor", defaultValues.customTextColor);
            textItem.clickableUrls = json.optBoolean("clickableUrls", defaultValues.clickableUrls);
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

    protected TextItem() {}

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
        this.text = copy.text;
        this.textColor = copy.textColor;
        this.customTextColor = copy.customTextColor;
        this.clickableUrls = copy.clickableUrls;
    }

    @Override @Getter @NonNull public String getText() { return text; }
    @Setter public void setText(@NonNull String v) { this.text = v; }
    @Getter public int getTextColor() { return textColor; }
    @Setter public void setTextColor(int v) { this.textColor = v; }
    @Getter public boolean isCustomTextColor() { return customTextColor; }
    @Setter public void setCustomTextColor(boolean v) { this.customTextColor = v; }
    @Getter public boolean isClickableUrls() { return clickableUrls; }
    @Setter public void setClickableUrls(boolean clickableUrls) { this.clickableUrls = clickableUrls; }
}
