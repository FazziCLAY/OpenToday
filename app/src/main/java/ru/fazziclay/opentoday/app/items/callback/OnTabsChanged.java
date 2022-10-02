package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.app.items.ItemsTab;
import ru.fazziclay.opentoday.callback.Callback;
import ru.fazziclay.opentoday.callback.Status;

public interface OnTabsChanged extends Callback {
    Status run(ItemsTab[] tabs);
}
