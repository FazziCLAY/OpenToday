package ru.fazziclay.opentoday.app.items.notifications;

import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.item.Item;

public interface ItemNotification {
    boolean tick(TickSession tickSession, Item item);
}
