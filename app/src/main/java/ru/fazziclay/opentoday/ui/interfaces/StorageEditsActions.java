package ru.fazziclay.opentoday.ui.interfaces;

import ru.fazziclay.opentoday.app.items.item.CycleListItem;
import ru.fazziclay.opentoday.app.items.item.FilterGroupItem;
import ru.fazziclay.opentoday.app.items.item.GroupItem;

public interface StorageEditsActions {
    void onGroupEdit(GroupItem groupItem);
    void onCycleListEdit(CycleListItem cycleListItem);
    void onFilterGroupEdit(FilterGroupItem filterGroupItem);
}
