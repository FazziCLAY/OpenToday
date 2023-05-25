package com.fazziclay.opentoday.app.items.notification;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.tick.TickSession;
import com.fazziclay.opentoday.app.items.item.Item;

public interface ItemNotification extends Cloneable {
    boolean tick(TickSession tickSession, Item item);
    @NonNull
    ItemNotification clone();
}
