package com.fazziclay.opentoday.gui.item;

import android.graphics.drawable.Drawable;

import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;

public interface ItemViewGeneratorBehavior {

    boolean isConfirmFastChanges();

    void setConfirmFastChanges(boolean b);

    Drawable getForeground(Item item);

    void onGroupEdit(GroupItem groupItem);
    void onCycleListEdit(CycleListItem cycleListItem);
    void onFilterGroupEdit(FilterGroupItem filterGroupItem);

    ItemsStorageDrawerBehavior getItemsStorageDrawerBehavior(Item item);

    boolean isRenderMinimized(Item item);

    boolean isRenderNotificationIndicator(Item item);
}
