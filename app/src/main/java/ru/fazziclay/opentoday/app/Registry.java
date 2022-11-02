package ru.fazziclay.opentoday.app;

import com.fazziclay.neosocket.packet.PacketsRegistry;

import ru.fazziclay.opentoday.app.items.item.ItemsRegistry;
import ru.fazziclay.opentoday.app.items.tab.TabsRegistry;
import ru.fazziclay.opentoday.app.items.notification.ItemNotificationsRegistry;

public class Registry {
    public static final TabsRegistry TABS = TabsRegistry.REGISTRY;
    public static final ItemsRegistry ITEMS = ItemsRegistry.REGISTRY;
    public static final ItemNotificationsRegistry ITEM_NOTIFICATIONS = ItemNotificationsRegistry.REGISTRY;
    public static final PacketsRegistry TELEMETRY_PACKETS = Telemetry.REGISTRY;
}
