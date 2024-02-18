package com.betterbrainmemory.opentoday.app.items.notification;

import com.betterbrainmemory.opentoday.app.data.Cherry;

public abstract class AbstractItemNotificationCodec {
    public abstract Cherry exportNotification(ItemNotification itemNotification);
    public abstract ItemNotification importNotification(Cherry cherry, ItemNotification notification);
}
