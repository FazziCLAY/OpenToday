package ru.fazziclay.opentoday.app.items.notification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItemNotificationIEUtil {

    public static List<ItemNotification> importNotifications(JSONArray json) throws Exception {
        final List<ItemNotification> result = new ArrayList<>();

        int i = 0;
        while (i < json.length()) {
            JSONObject jsonNotification = json.getJSONObject(i);
            String type = jsonNotification.getString("type");
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
                    .put("type", itemNotificationInfo.getStringType()));
        }
        return jsonArray;
    }
}
