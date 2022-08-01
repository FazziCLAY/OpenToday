package ru.fazziclay.opentoday.app.items;

import android.graphics.Color;

import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.GGGetter;
import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;

public class TextItem extends Item {
    private static final String DEFAULT_TEXT_COLOR = "#ff0000ff";
    // START - Save
    protected final static TextItemIETool IE_TOOL = new TextItemIETool();
    protected static class TextItemIETool extends Item.ItemIETool {
        @Override
        protected JSONObject exportItem(Item item) throws Exception {
            TextItem textItem = (TextItem) item;
            return super.exportItem(textItem)
                    .put("text", textItem.text)
                    .put("textColor", textItem.textColor)
                    .put("customTextColor", textItem.customTextColor);
        }

        private final TextItem defaultValues = new TextItem("<import_error>");
        @Override
        protected Item importItem(JSONObject json) throws Exception {
            TextItem o = new TextItem(super.importItem(json), json.optString("text", defaultValues.text));
            o.textColor = json.optInt("textColor", defaultValues.textColor);
            o.customTextColor = json.optBoolean("customTextColor", defaultValues.customTextColor);
            return o;
        }
    }
    // END - Save

    @JSONName(name = "text") @RequireSave protected String text;
    @JSONName(name = "textColor") @RequireSave protected int textColor = Color.parseColor(DEFAULT_TEXT_COLOR);
    @JSONName(name = "customTextColor") @RequireSave protected boolean customTextColor = false;

    public TextItem(String text) {
        this(null, text);
    }

    // Append
    public TextItem(Item item, String text) {
        super(item);
        this.text = text;
    }

    // Copy
    public TextItem(TextItem copy) {
        super(copy);
        this.text = copy.text;
        this.textColor = copy.textColor;
        this.customTextColor = copy.customTextColor;
    }

    @GGGetter public String getText() { return text; }
    @Setter public void setText(String v) { this.text = v; }
    @GGGetter public int getTextColor() { return textColor; }
    @Setter public void setTextColor(int v) { this.textColor = v; }
    @GGGetter public boolean isCustomTextColor() { return customTextColor; }
    @Setter public void setCustomTextColor(boolean v) { this.customTextColor = v; }

    @Override
    public void tick() {
        super.tick();
    }
}
