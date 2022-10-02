package ru.fazziclay.opentoday.ui.interfaces;

import java.util.UUID;

import ru.fazziclay.opentoday.app.items.ItemsTab;

public interface CurrentItemsTab {
    UUID getCurrentTabId();
    ItemsTab getCurrentTab();
    void setCurrentTab(UUID id);
}
