package com.fazziclay.opentoday.app;

public interface Translation {
    int KEY_TABS_DEFAULT_MAIN_NAME = 314;
    int KEY_MATHGAME_PRIMITIVE_OPERATION = 315;
    int KEY_SLEEP_TIME_ITEM_PATTERN = 316;

    String get(Object key, Object... args);
}
