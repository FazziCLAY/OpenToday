package com.fazziclay.opentoday.app.items.notification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItemNotificationIEUtil {
    private static final String KEY_TYPE = "type";

    public static List<ItemNotification> importNotifications(JSONArray json) throws Exception {
        final List<ItemNotification> result = new ArrayList<>();

        int i = 0;
        while (i < json.length()) {
            JSONObject jsonNotification = json.getJSONObject(i);
            String type = jsonNotification.getString(KEY_TYPE);
            ItemNotificationIETool ieTool = ItemNotificationsRegistry.REGISTRY.getByStringType(type).getIeTool();
            result.add(ieTool.importNotification(jsonNotification));
            i++;
        }

        return result;
    }

    public static JSONArray exportNotifications(List<ItemNotification> notifications) throws Exception {
        final JSONArray jsonArray = new JSONArray();
        for (ItemNotification notification : notifications) {
            ItemNotificationsRegistry.ItemNotificationInfo itemNotificationInfo = ItemNotificationsRegistry.REGISTRY.getByClass(notification.getClass());
            jsonArray.put(itemNotificationInfo.getIeTool().exportNotification(notification)
                    .put(KEY_TYPE, itemNotificationInfo.getStringType()));
        }
        return jsonArray;
    }
}
