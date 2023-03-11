package com.fazziclay.opentoday.gui.interfaces;

import com.fazziclay.opentoday.app.items.tab.Tab;

import java.util.UUID;

public interface CurrentItemsTab {
    UUID getCurrentTabId();
    Tab getCurrentTab();
    void setCurrentTab(UUID id);
}
