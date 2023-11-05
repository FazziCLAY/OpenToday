package com.fazziclay.opentoday.app

import com.fazziclay.neosocket.packet.PacketsRegistry
import com.fazziclay.opentoday.app.icons.IconsRegistry
import com.fazziclay.opentoday.app.items.item.ItemsRegistry
import com.fazziclay.opentoday.app.items.item.filter.FiltersRegistry
import com.fazziclay.opentoday.app.items.notification.ItemNotificationsRegistry
import com.fazziclay.opentoday.app.items.tab.TabsRegistry
import com.fazziclay.opentoday.gui.EnumsRegistry

val ITEMS: ItemsRegistry = ItemsRegistry.REGISTRY
val TABS: TabsRegistry = TabsRegistry.REGISTRY
val ITEM_NOTIFICATIONS: ItemNotificationsRegistry = ItemNotificationsRegistry.REGISTRY
val TELEMETRY_PACKETS: PacketsRegistry = Telemetry.REGISTRY
val FILTERS: FiltersRegistry = FiltersRegistry.REGISTRY
val ENUMS: EnumsRegistry = EnumsRegistry
val ICONS: IconsRegistry = IconsRegistry.REGISTRY