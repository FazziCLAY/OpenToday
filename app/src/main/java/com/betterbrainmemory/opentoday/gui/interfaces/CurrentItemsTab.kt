package com.betterbrainmemory.opentoday.gui.interfaces

import java.util.UUID

interface CurrentItemsTab {
    fun getCurrentTabId(): UUID
    fun getCurrentTab(): com.betterbrainmemory.opentoday.app.items.tab.Tab
    fun setCurrentTab(id: UUID?)
}