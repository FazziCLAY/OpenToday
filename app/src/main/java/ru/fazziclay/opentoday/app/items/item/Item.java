package ru.fazziclay.opentoday.app.items.item;

import android.graphics.Color;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;
import ru.fazziclay.opentoday.app.items.ItemController;
import ru.fazziclay.opentoday.app.items.ItemImportExportTool;

public class Item implements Cloneable {
    private static final String DEFAULT_BACKGROUND_COLOR = "#99999999";

    // START - Save
    public final static ItemIETool IE_TOOL = new ItemIETool();
    public static class ItemIETool extends ItemImportExportTool {
        @Override
        public JSONObject exportItem(Item item) throws Exception {
            return new JSONObject()
                    .put("viewMinHeight", item.viewMinHeight)
                    .put("viewBackgroundColor", item.viewBackgroundColor)
                    .put("viewCustomBackgroundColor", item.viewCustomBackgroundColor)
                    .put("minimize", item.minimize);
        }

        private final Item defaultValues = new Item();
        @Override
        public Item importItem(JSONObject json) throws Exception {
            Item o = new Item();
            o.viewMinHeight = json.optInt("viewMinHeight", defaultValues.viewMinHeight);
            o.viewBackgroundColor = json.optInt("viewBackgroundColor", defaultValues.viewBackgroundColor);
            o.viewCustomBackgroundColor = json.optBoolean("viewCustomBackgroundColor", defaultValues.viewCustomBackgroundColor);
            o.minimize = json.optBoolean("minimize", defaultValues.minimize);
            return o;
        }
    }
    // END - Save

    @JSONName(name = "viewMinHeight") @RequireSave private int viewMinHeight = 0; // минимальная высота
    @JSONName(name = "viewBackgroundColor") @RequireSave private int viewBackgroundColor = Color.parseColor(DEFAULT_BACKGROUND_COLOR); // фоновый цвет
    @JSONName(name = "viewCustomBackgroundColor") @RequireSave private boolean viewCustomBackgroundColor = false; // юзаем ли фоновый цвет
    @JSONName(name = "minimize") @RequireSave private boolean minimize = false;
    private ItemController controller = null;

    // Copy
    public Item(Item copy) {
        if (copy != null) {
            this.viewMinHeight = copy.viewMinHeight;
            this.viewBackgroundColor = copy.viewBackgroundColor;
            this.viewCustomBackgroundColor = copy.viewCustomBackgroundColor;
            this.minimize = copy.minimize;
            this.controller = copy.controller;
        }
    }

    public Item() {
        this(null);
    }

    public void delete() {
        if (controller != null) controller.delete(this);
    }

    public void save() {
        if (controller != null) controller.save(this);
    }

    public void updateUi() {
        if (controller != null) controller.updateUi(this);
    }

    public void tick() {}
    
    @NonNull
    @Override
    public Item clone() {
        try {
            return (Item) super.clone();
        } catch (CloneNotSupportedException | ClassCastException e) {
            throw new RuntimeException("Clone exception (what???)", e);
        }
    }
    
    @Getter public int getViewMinHeight() { return viewMinHeight; }
    @Setter public void setViewMinHeight(int v) { this.viewMinHeight = v; }
    @Getter public int getViewBackgroundColor() { return viewBackgroundColor; }
    @Setter public void setViewBackgroundColor(int v) { this.viewBackgroundColor = v; }
    @Getter public boolean isViewCustomBackgroundColor() { return viewCustomBackgroundColor; }
    @Setter public void setViewCustomBackgroundColor(boolean v) { this.viewCustomBackgroundColor = v; }
    @Getter public boolean isMinimize() { return minimize; }
    @Setter public void setMinimize(boolean minimize) { this.minimize = minimize; }
    @Getter public ItemController getController() { return controller; }
    @Setter public void setController(ItemController controller) { this.controller = controller; }
}
