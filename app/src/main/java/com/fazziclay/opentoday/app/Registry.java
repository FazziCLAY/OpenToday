package com.fazziclay.opentoday.app;

import com.fazziclay.neosocket.packet.PacketsRegistry;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.notification.ItemNotificationsRegistry;
import com.fazziclay.opentoday.app.items.tab.TabsRegistry;

/**
 * All registry's
 */
public class Registry {
    public static final TabsRegistry TABS = TabsRegistry.REGISTRY;
    public static final ItemsRegistry ITEMS = ItemsRegistry.REGISTRY;
    public static final ItemNotificationsRegistry ITEM_NOTIFICATIONS = ItemNotificationsRegistry.REGISTRY;
    public static final PacketsRegistry TELEMETRY_PACKETS = Telemetry.REGISTRY;
}
