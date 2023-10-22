package com.fazziclay.opentoday.app.items.notification;

import com.fazziclay.opentoday.app.data.Cherry;

public abstract class AbstractItemNotificationCodec {
    public abstract Cherry exportNotification(ItemNotification itemNotification);
    public abstract ItemNotification importNotification(Cherry cherry, ItemNotification notification);
}
