package com.fazziclay.opentoday.gui;

import com.fazziclay.opentoday.gui.activity.MainActivity;

public interface UIRoot {
    void pushActivitySettings(ActivitySettings settings);
    void pushActivitySettings(MainActivity.ActivitySettingsPush settings);
    void popActivitySettings();

    void addNotification(UINotification notification);
}
