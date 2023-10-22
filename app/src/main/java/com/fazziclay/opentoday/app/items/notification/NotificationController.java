package com.fazziclay.opentoday.app.items.notification;

import com.fazziclay.opentoday.app.items.item.Item;

import java.util.UUID;

public interface NotificationController {
    UUID generateId(ItemNotification notification);

    Item getParentItem(ItemNotification itemNotification);
}
