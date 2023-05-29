package com.fazziclay.opentoday.app.items.notification;

import com.fazziclay.opentoday.app.data.CherryOrchard;

import java.util.ArrayList;
import java.util.List;

public class ItemNotificationCodecUtil {
    private static final String KEY_TYPE = "type";

    public static List<ItemNotification> importNotificationList(CherryOrchard orchard) {
        final List<ItemNotification> result = new ArrayList<>();

        orchard.forEachCherry((_ignore, cherry) -> {
            final String type = cherry.getString(KEY_TYPE);
            final ItemNotificationCodec codec = ItemNotificationsRegistry.REGISTRY.getByStringType(type).getCodec();
            result.add(codec.importNotification(cherry));
        });

        return result;
    }

    public static CherryOrchard exportNotificationList(List<ItemNotification> notifications) {
        final CherryOrchard orchard = new CherryOrchard();
        for (ItemNotification notification : notifications) {
            ItemNotificationsRegistry.ItemNotificationInfo itemNotificationInfo = ItemNotificationsRegistry.REGISTRY.getByClass(notification.getClass());
            orchard.put(itemNotificationInfo.getCodec().exportNotification(notification)
                    .put(KEY_TYPE, itemNotificationInfo.getStringType()));
        }
        return orchard;
    }
}
