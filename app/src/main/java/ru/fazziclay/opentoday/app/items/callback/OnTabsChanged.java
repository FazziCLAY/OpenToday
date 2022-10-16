package ru.fazziclay.opentoday.app.items.callback;

import ru.fazziclay.opentoday.app.items.tab.Tab;
import ru.fazziclay.opentoday.callback.Callback;
import ru.fazziclay.opentoday.callback.Status;

public interface OnTabsChanged extends Callback {
    Status run(Tab[] tabs);
}
