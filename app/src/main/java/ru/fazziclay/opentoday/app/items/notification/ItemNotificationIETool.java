package ru.fazziclay.opentoday.app.items.notification;

import org.json.JSONObject;

public abstract class ItemNotificationIETool {
    public abstract JSONObject exportNotification(ItemNotification itemNotification) throws Exception;
    public abstract ItemNotification importNotification(JSONObject json) throws Exception;
}
