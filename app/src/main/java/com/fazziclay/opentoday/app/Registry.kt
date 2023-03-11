package com.fazziclay.opentoday.app

import com.fazziclay.neosocket.packet.PacketsRegistry
import com.fazziclay.opentoday.app.items.item.ItemsRegistry
import com.fazziclay.opentoday.app.items.notification.ItemNotificationsRegistry
import com.fazziclay.opentoday.app.items.tab.TabsRegistry

val ITEMS: ItemsRegistry = ItemsRegistry.REGISTRY
val TABS: TabsRegistry = TabsRegistry.REGISTRY
val ITEM_NOTIFICATIONS: ItemNotificationsRegistry = ItemNotificationsRegistry.REGISTRY
val TELEMETRY_PACKETS: PacketsRegistry = Telemetry.REGISTRY