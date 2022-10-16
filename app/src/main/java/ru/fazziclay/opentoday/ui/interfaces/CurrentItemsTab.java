package ru.fazziclay.opentoday.ui.interfaces;

import java.util.UUID;

import ru.fazziclay.opentoday.app.items.tab.Tab;

public interface CurrentItemsTab {
    UUID getCurrentTabId();
    Tab getCurrentTab();
    void setCurrentTab(UUID id);
}
