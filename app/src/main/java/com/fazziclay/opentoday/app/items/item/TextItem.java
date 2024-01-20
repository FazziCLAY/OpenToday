package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.Checks;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.Setter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextItem extends Item {
    public final static TextItemCodec CODEC = new TextItemCodec();
    public final static ItemFactory<TextItem> FACTORY = new TextItemFactory();

    private static final String DEFAULT_TEXT_COLOR = "#ff0000ff";
    private static final int DEFAULT_TEXT_COLOR_CACHED = ColorUtil.hexToColor(DEFAULT_TEXT_COLOR);


    @NotNull private String text = "";
    private int textColor = DEFAULT_TEXT_COLOR_CACHED;
    private boolean customTextColor = false;
    private boolean clickableUrls = false;
    private boolean formatting = true;

    public TextItem() {
        super();
    }

    public TextItem(@NotNull String text) {
        this(null, text);
    }

    // Append
    public TextItem(@Nullable Item item, @NonNull String text) {
        super(item);
        Checks.throwIsNull(text, "text");
        this.text = text;
    }

    // Copy
    public TextItem(@Nullable TextItem copy) {
        super(copy);
        if (copy != null) {
            this.text = copy.text;
            this.textColor = copy.textColor;
            this.customTextColor = copy.customTextColor;
            this.clickableUrls = copy.clickableUrls;
            this.formatting = copy.formatting;
        }
    }

    @Override @Getter @NonNull public String getText() { return text; }
    @Setter public void setText(@NonNull String v) { this.text = v; }
    @Getter public int getTextColor() { return textColor; }
    @Setter public void setTextColor(int v) { this.textColor = v; }
    @Getter public boolean isCustomTextColor() { return customTextColor; }
    @Setter public void setCustomTextColor(boolean v) { this.customTextColor = v; }
    @Getter public boolean isClickableUrls() { return clickableUrls; }
    @Setter public void setClickableUrls(boolean clickableUrls) { this.clickableUrls = clickableUrls; }
    @Getter public boolean isFormatting() {return formatting;}
    @Setter public void setFormatting(boolean v) {this.formatting = v;}




    // Import - Export - Factory
    public static class TextItemCodec extends ItemCodec {
        private static final String KEY_TEXT = "text";
        private static final String KEY_TEXT_COLOR = "text_color";
        private static final String KEY_TEXT_IS_CUSTOM = "text_color_is_custom";
        private static final String KEY_TEXT_CLICKABLE_URLS = "text_clickable_urls";
        private static final String KEY_TEXT_FORMATTING = "text_formatting";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            TextItem textItem = (TextItem) item;
            return super.exportItem(textItem)
                    .put(KEY_TEXT, textItem.text)
                    .put(KEY_TEXT_COLOR, ColorUtil.colorToHex(textItem.textColor))
                    .put(KEY_TEXT_IS_CUSTOM, textItem.customTextColor)
                    .put(KEY_TEXT_CLICKABLE_URLS, textItem.clickableUrls)
                    .put(KEY_TEXT_FORMATTING, textItem.formatting);
        }

        private final TextItem DEFAULT_VALUES = new TextItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, @Nullable Item item) {
            final TextItem textItem = fallback(item, TextItem::new);
            super.importItem(cherry, textItem);

            textItem.text = cherry.optString(KEY_TEXT, DEFAULT_VALUES.text);
            textItem.textColor = ColorUtil.hexToColor(cherry.optString(KEY_TEXT_COLOR, DEFAULT_TEXT_COLOR));
            textItem.customTextColor = cherry.optBoolean(KEY_TEXT_IS_CUSTOM, DEFAULT_VALUES.customTextColor);
            textItem.clickableUrls = cherry.optBoolean(KEY_TEXT_CLICKABLE_URLS, DEFAULT_VALUES.clickableUrls);
            textItem.formatting = cherry.optBoolean(KEY_TEXT_FORMATTING, DEFAULT_VALUES.formatting);
            return textItem;
        }
    }

    private static class TextItemFactory implements ItemFactory<TextItem> {
        @Override
        public TextItem create() {
            return new TextItem("");
        }

        @Override
        public TextItem copy(Item item) {
            return new TextItem((TextItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new TextItem(textItem));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
