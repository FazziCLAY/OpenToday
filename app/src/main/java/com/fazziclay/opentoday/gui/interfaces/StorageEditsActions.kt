package com.fazziclay.opentoday.gui.interfaces

import com.fazziclay.opentoday.app.items.item.CycleListItem
import com.fazziclay.opentoday.app.items.item.FilterGroupItem
import com.fazziclay.opentoday.app.items.item.GroupItem

interface StorageEditsActions {
    fun onGroupEdit(groupItem: GroupItem)
    fun onCycleListEdit(cycleListItem: CycleListItem)
    fun onFilterGroupEdit(filterGroupItem: FilterGroupItem)
}