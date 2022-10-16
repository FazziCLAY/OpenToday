package ru.fazziclay.opentoday.app.items.notification;

import androidx.annotation.NonNull;

import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.item.Item;

public interface ItemNotification extends Cloneable {
    boolean tick(TickSession tickSession, Item item);
    @NonNull
    ItemNotification clone();
}
