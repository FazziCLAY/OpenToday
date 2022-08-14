package ru.fazziclay.opentoday.app.items.notifications;

import ru.fazziclay.opentoday.app.TickSession;

public interface ItemNotification {
    boolean tick(TickSession tickSession);
}
