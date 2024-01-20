package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.Checks;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.Setter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtendedTextItem extends TextItem {
    public static final LongTextItemCodec CODEC = new LongTextItemCodec();
    public static final ItemFactory<ExtendedTextItem> FACTORY = new LongTextItemFactory();

    private static final String DEFAULT_EXTENDED_TEXT_COLOR = "#00dd00";
    private static final int DEFAULT_EXTENDED_TEXT_COLOR_CACHED = ColorUtil.hexToColor(DEFAULT_EXTENDED_TEXT_COLOR);


    private String addictionText = "";
    private int addictionTextColor = DEFAULT_EXTENDED_TEXT_COLOR_CACHED;
    private boolean isCustomAddictionTextColor = false;
    private boolean isAddictionTextClickableUrls = false;
    private int addictionTextSize = 20;
    private boolean isCustomAddictionTextSize = false;


    public ExtendedTextItem() {
        super();
    }

    public ExtendedTextItem(@NotNull String text, @NotNull String addictionText) {
        super(text);
        Checks.throwIsNull(addictionText, "addictionText");
        this.addictionText = addictionText;
    }

    // Append
    public ExtendedTextItem(@Nullable TextItem textItem, @NotNull String addictionText) {
        super(textItem);
        Checks.throwIsNull(addictionText, "addictionText");
        this.addictionText = addictionText;
    }

    // Copy
    public ExtendedTextItem(@Nullable ExtendedTextItem copy) {
        super(copy);
        if (copy != null) {
            this.addictionText = copy.addictionText;
            this.addictionTextColor = copy.addictionTextColor;
            this.isCustomAddictionTextColor = copy.isCustomAddictionTextColor;
            this.isAddictionTextClickableUrls = copy.isAddictionTextClickableUrls;
            this.addictionTextSize = copy.addictionTextSize;
            this.isCustomAddictionTextSize = copy.isCustomAddictionTextSize;
        }
    }

    @Setter
    public void setAddictionTextColor(int addictionTextColor) {this.addictionTextColor = addictionTextColor;}
    @Getter
    public int getAddictionTextColor() {return addictionTextColor;}

    @Getter public boolean isCustomAddictionTextColor() {return isCustomAddictionTextColor;}
    @Setter public void setCustomAddictionTextColor(boolean b) {this.isCustomAddictionTextColor = b;}

    @Setter public void setAddictionTextClickableUrls(boolean checked) {this.isAddictionTextClickableUrls = checked;}
    @Getter public boolean isAddictionTextClickableUrls() {return isAddictionTextClickableUrls;}

    @Getter public String getAddictionText() { return addictionText; }
    @Setter public void setAddictionText(String s) { this.addictionText = s; }

    @Getter public int getAddictionTextSize() {return addictionTextSize;}
    @Setter public void setAddictionTextSize(int addictionTextSize) {this.addictionTextSize = addictionTextSize;}

    @Setter public boolean isCustomAddictionTextSize() {return isCustomAddictionTextSize;}
    @Setter public void setCustomAddictionTextSize(boolean customAddictionTextSize) {isCustomAddictionTextSize = customAddictionTextSize;}



    // Import - Export - Factory
    public static class LongTextItemCodec extends TextItemCodec {
        private static final String KEY_TEXT = "addiction_text";
        private static final String KEY_TEXT_SIZE = "addiction_text_size";
        private static final String KEY_TEXT_SIZE_IS_CUSTOM = "addiction_text_size_is_custom";
        private static final String KEY_TEXT_COLOR = "addiction_text_color";
        private static final String KEY_TEXT_COLOR_IS_CUSTOM = "addiction_text_color_is_custom";
        private static final String KEY_TEXT_CLICKABLE_URLS = "addiction_text_clickable_urls";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            ExtendedTextItem extendedTextItem = (ExtendedTextItem) item;
            return super.exportItem(extendedTextItem)
                    .put(KEY_TEXT, extendedTextItem.addictionText)
                    .put(KEY_TEXT_SIZE, extendedTextItem.addictionTextSize)
                    .put(KEY_TEXT_SIZE_IS_CUSTOM, extendedTextItem.isCustomAddictionTextSize)
                    .put(KEY_TEXT_COLOR, ColorUtil.colorToHex(extendedTextItem.addictionTextColor))
                    .put(KEY_TEXT_COLOR_IS_CUSTOM, extendedTextItem.isCustomAddictionTextColor)
                    .put(KEY_TEXT_CLICKABLE_URLS, extendedTextItem.isAddictionTextClickableUrls);
        }

        private final ExtendedTextItem DEFAULT_VALUES = new ExtendedTextItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            var extendedTextItem = fallback(item, ExtendedTextItem::new);
            super.importItem(cherry, extendedTextItem);

            extendedTextItem.addictionText = cherry.optString(KEY_TEXT, DEFAULT_VALUES.addictionText);
            extendedTextItem.addictionTextSize = cherry.optInt(KEY_TEXT_SIZE, DEFAULT_VALUES.addictionTextSize);
            extendedTextItem.isCustomAddictionTextSize = cherry.optBoolean(KEY_TEXT_SIZE_IS_CUSTOM, DEFAULT_VALUES.isCustomAddictionTextSize);
            extendedTextItem.addictionTextColor = ColorUtil.hexToColor(cherry.optString(KEY_TEXT_COLOR, DEFAULT_EXTENDED_TEXT_COLOR));
            extendedTextItem.isCustomAddictionTextColor = cherry.optBoolean(KEY_TEXT_COLOR_IS_CUSTOM, DEFAULT_VALUES.isCustomAddictionTextColor);
            extendedTextItem.isAddictionTextClickableUrls = cherry.optBoolean(KEY_TEXT_CLICKABLE_URLS, DEFAULT_VALUES.isAddictionTextClickableUrls);
            return extendedTextItem;
        }
    }

    private static class LongTextItemFactory implements ItemFactory<ExtendedTextItem> {
        @Override
        public ExtendedTextItem create() {
            return new ExtendedTextItem("", "");
        }

        @Override
        public ExtendedTextItem copy(Item item) {
            return new ExtendedTextItem((ExtendedTextItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof ContainerItem containerItem) {
                return Transform.Result.allow(() -> {
                    String text = "";
                    for (Item intItem : containerItem.getAllItems()) {
                        String t = ((intItem instanceof CheckboxItem checkboxItem) ? (checkboxItem.isChecked() ? "[*] " : "[ ] ") : "") + intItem.getText();
                        text += t.trim() + "\n\n";
                    }
                    return new ExtendedTextItem((TextItem) from, text);
                });

            } else if (from instanceof TextItem) {
                return Transform.Result.allow(() -> new ExtendedTextItem((TextItem) from, from.getText()));
            }

            return Transform.Result.NOT_ALLOW;
        }
    }
}
