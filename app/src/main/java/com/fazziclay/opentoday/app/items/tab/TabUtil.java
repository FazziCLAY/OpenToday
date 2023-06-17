package com.fazziclay.opentoday.app.items.tab;

public class TabUtil {
    public static void throwIsAttached(Tab tab) {
        if (tab.isAttached()) {
            throw new RuntimeException("Tab already attached! tab="+tab);
        }
    }
}
