package com.fazziclay.opentoday.app.settings.enums;

import android.graphics.Color;

import androidx.appcompat.app.AppCompatDelegate;

public enum ThemeEnum {
    AUTO,
    NIGHT,
    LIGHT;

    public static ThemeEnum ofId(int theme) {
        return switch (theme) {
            case AppCompatDelegate.MODE_NIGHT_NO -> LIGHT;
            case AppCompatDelegate.MODE_NIGHT_YES -> NIGHT;
            default -> AUTO;
        };
    }

    public int id() {
        return switch (this) {
            case AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            case NIGHT -> AppCompatDelegate.MODE_NIGHT_YES;
            case LIGHT -> AppCompatDelegate.MODE_NIGHT_NO;
        };
    }

    public int getRawForegroundColor() {
        return switch (this) {
            case AUTO -> Color.GREEN;
            case NIGHT -> Color.WHITE;
            case LIGHT -> Color.BLACK;
        };
    }
}
