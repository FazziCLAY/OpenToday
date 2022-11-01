package ru.fazziclay.opentoday.app.items.notification;

import java.util.ArrayList;
import java.util.List;

import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.item.Item;

public class ItemNotificationUtil {
    /**
     * Copy notifications
     * @param notifications to copy
     * @return coped notifications
     */
    public static List<ItemNotification> copy(List<ItemNotification> notifications) {
        List<ItemNotification> result = new ArrayList<>();
        for (ItemNotification copyNotify : notifications) {
            ItemNotification newNotify = copyNotify.clone();
            result.add(newNotify);
        }
        return result;
    }

    public static void tick(TickSession tickSession, List<ItemNotification> notifications, Item item) {
        for (ItemNotification notification : notifications) {
            notification.tick(tickSession, item);
        }
    }
}