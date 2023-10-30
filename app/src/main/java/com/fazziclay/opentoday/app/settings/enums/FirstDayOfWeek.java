package com.fazziclay.opentoday.app.settings.enums;

import java.util.Calendar;

public enum FirstDayOfWeek {
    MONDAY,
    SATURDAY;

    public static FirstDayOfWeek of(int i) {
        if (i == Calendar.SATURDAY) return SATURDAY;
        return MONDAY;
    }

    public int id() {
        if (this == SATURDAY) return Calendar.SATURDAY;
        return Calendar.MONDAY;
    }
}
