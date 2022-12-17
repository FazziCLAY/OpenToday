package com.fazziclay.opentoday.gui.interfaces;

import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;

public interface StorageEditsActions {
    void onGroupEdit(GroupItem groupItem);
    void onCycleListEdit(CycleListItem cycleListItem);
    void onFilterGroupEdit(FilterGroupItem filterGroupItem);
}
