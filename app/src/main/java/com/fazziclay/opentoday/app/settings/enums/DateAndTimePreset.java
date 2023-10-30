package com.fazziclay.opentoday.app.settings.enums;

public enum DateAndTimePreset {
    DEFAULT("HH:mm:ss", "yyyy.MM.dd EE"),
    DEFAULT_FULLY_WEEKDAY("HH:mm:ss", "yyyy.MM.dd EEEE"),
    NO_SECONDS("HH:mm", "yyyy.MM.dd EE"),
    NO_SECONDS_FULLY_WEEKDAY("HH:mm", "yyyy.MM.dd EEEE"),
    NO_TIME("", "yyyy.MM.dd EE"),
    NO_TIME_INVERT("", "dd.MM.yyyy EE"),
    NO_TIME_FULLY_WEEKDAY("", "yyyy.MM.dd EEEE"),
    NO_TIME_FULLY_WEEKDAY_INVERT("", "dd.MM.yyyy EEEE"),
    INVERT_DATE("HH:mm:ss", "dd.MM.yyyy EE"),
    INVERT_DATE_FULLY_WEEKDAY("HH:mm:ss", "dd.MM.yyyy EEEE"),
    ;

    private final String time;
    private final String date;

    DateAndTimePreset(String time, String date) {
        this.time = time;
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }
}
