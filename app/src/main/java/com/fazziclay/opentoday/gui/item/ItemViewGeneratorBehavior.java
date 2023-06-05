package com.fazziclay.opentoday.gui.item;

import android.graphics.drawable.Drawable;

import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.app.items.item.Item;

public interface ItemViewGeneratorBehavior {

    boolean isMinimizeGrayColor();

    SettingsManager.ItemAction getItemOnClickAction();

    boolean isScrollToAddedItem();

    SettingsManager.ItemAction getItemOnLeftAction();

    boolean isConfirmFastChanges();

    void setConfirmFastChanges(boolean b);

    Drawable getForeground(Item item);
}
