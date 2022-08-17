package ru.fazziclay.opentoday.app;

import ru.fazziclay.opentoday.app.items.ItemsRegistry;
import ru.fazziclay.opentoday.app.items.notifications.ItemNotificationsRegistry;

public class Registry {
    public static final ItemsRegistry ITEMS = ItemsRegistry.REGISTRY;
    public static final ItemNotificationsRegistry ITEM_NOTIFICATIONS = ItemNotificationsRegistry.REGISTRY;
}
