package ru.fazziclay.opentoday.app.items.item;

import android.graphics.Color;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.ItemController;
import ru.fazziclay.opentoday.app.items.ItemImportExportTool;
import ru.fazziclay.opentoday.app.items.notifications.ItemNotification;
import ru.fazziclay.opentoday.app.items.notifications.ItemNotificationIETool;
import ru.fazziclay.opentoday.app.items.notifications.ItemNotificationsRegistry;

public class Item implements Cloneable {
    private static final String DEFAULT_BACKGROUND_COLOR = "#99999999";

    // START - Save
    public static class ItemIETool extends ItemImportExportTool {
        @Override
        public JSONObject exportItem(Item item) throws Exception {
            return new JSONObject()
                    .put("viewMinHeight", item.viewMinHeight)
                    .put("viewBackgroundColor", item.viewBackgroundColor)
                    .put("viewCustomBackgroundColor", item.viewCustomBackgroundColor)
                    .put("minimize", item.minimize)
                    .put("notifications", exportNotifications(item.notifications));
        }

        private final Item defaultValues = new Item();
        @Override
        public Item importItem(JSONObject json) throws Exception {
            Item o = new Item();
            o.viewMinHeight = json.optInt("viewMinHeight", defaultValues.viewMinHeight);
            o.viewBackgroundColor = json.optInt("viewBackgroundColor", defaultValues.viewBackgroundColor);
            o.viewCustomBackgroundColor = json.optBoolean("viewCustomBackgroundColor", defaultValues.viewCustomBackgroundColor);
            o.minimize = json.optBoolean("minimize", defaultValues.minimize);
            JSONArray jsonArray = json.optJSONArray("notifications");
            o.notifications = importNotifications(jsonArray != null ? jsonArray : new JSONArray());
            return o;
        }

        private JSONArray exportNotifications(List<ItemNotification> notifications) throws Exception {
            JSONArray array = new JSONArray();

            for (ItemNotification notification : notifications) {
                ItemNotificationsRegistry.ItemNotificationInfo itemNotificationInfo = ItemNotificationsRegistry.REGISTRY.getByClass(notification.getClass());
                JSONObject jsonObject = itemNotificationInfo.getIeTool().exportNotification(notification);
                array.put(jsonObject.put("type", itemNotificationInfo.getStringType()));
            }

            return array;
        }

        private List<ItemNotification> importNotifications(JSONArray notifications) throws Exception {
            List<ItemNotification> list = new ArrayList<>();

            int i = 0;
            while (i < notifications.length()) {
                JSONObject jsonObject = notifications.getJSONObject(i);
                String type = jsonObject.getString("type");
                ItemNotificationIETool ieTool = ItemNotificationsRegistry.REGISTRY.getByStringType(type).getIeTool();
                list.add(ieTool.importNotification(jsonObject));
                i++;
            }

            return list;
        }
    }
    // END - Save

    @JSONName(name = "viewMinHeight") @RequireSave private int viewMinHeight = 0; // минимальная высота
    @JSONName(name = "viewBackgroundColor") @RequireSave private int viewBackgroundColor = Color.parseColor(DEFAULT_BACKGROUND_COLOR); // фоновый цвет
    @JSONName(name = "viewCustomBackgroundColor") @RequireSave private boolean viewCustomBackgroundColor = false; // юзаем ли фоновый цвет
    @JSONName(name = "minimize") @RequireSave private boolean minimize = false;
    @JSONName(name = "notifications") @RequireSave private List<ItemNotification> notifications = new ArrayList<>();
    private ItemController controller = null;

    // Copy
    public Item(Item copy) {
        if (copy != null) {
            this.viewMinHeight = copy.viewMinHeight;
            this.viewBackgroundColor = copy.viewBackgroundColor;
            this.viewCustomBackgroundColor = copy.viewCustomBackgroundColor;
            this.minimize = copy.minimize;
            this.controller = copy.controller;
            this.notifications = new ArrayList<>(copy.notifications);
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

    public void tick(TickSession tickSession) {
        for (ItemNotification notification : notifications) {
            if (notification.tick(tickSession)) {
                updateUi();
            }
        }
    }
    
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
    @Setter public void setController(ItemController controller) { this.controller = controller; }

    public List<ItemNotification> getNotifications() {
        return notifications;
    }
}
