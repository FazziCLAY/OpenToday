package com.fazziclay.opentoday.app.items.notification;

import com.fazziclay.opentoday.app.data.CherryOrchard;

import java.util.ArrayList;
import java.util.List;

public class ItemNotificationCodecUtil {
    private static final String KEY_TYPE = "type"; // TODO: 22.10.2023 maybe rename to "notificationType"? (it need write fixes in DataFaxer....) 

    // must return mutable list (arraylist!)
    public static List<ItemNotification> importNotificationList(CherryOrchard orchard) {
        final List<ItemNotification> result = new ArrayList<>();

        orchard.forEachCherry((_ignore, cherry) -> {
            final String type = cherry.getString(KEY_TYPE);
            final AbstractItemNotificationCodec codec = ItemNotificationsRegistry.REGISTRY.getByStringType(type).getCodec();
            result.add(codec.importNotification(cherry, null));
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
