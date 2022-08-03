package ru.fazziclay.opentoday.app.settings;

import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

import ru.fazziclay.javaneoutil.FileUtil;

public class SettingsManager {
    // Theme
    private static final String KEY_THEME = "theme";
    private static final String THEME_SYSTEM = "system";
    private static final String THEME_NIGHT = "night";
    private static final String THEME_LIGHT = "light";

    // First day of week
    private static final String KEY_FIRST_DAY_OF_WEEK = "firstDayOfWeek";
    private static final String FIRST_DAY_OF_WEEK_SATURDAY = "saturday";
    private static final String FIRST_DAY_OF_WEEK_MONDAY = "monday";

    //
    private final File saveFile;
    private int firstDayOfWeek = Calendar.SUNDAY;
    private int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    public SettingsManager(File saveFile) {
        this.saveFile = saveFile;
        load();
    }

    private void load() {
        if (!FileUtil.isExist(saveFile)) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(FileUtil.getText(saveFile, "{}"));

            // first day of week
            String dayOfWeek = jsonObject.optString(KEY_FIRST_DAY_OF_WEEK, FIRST_DAY_OF_WEEK_SATURDAY);
            if (dayOfWeek.equalsIgnoreCase(FIRST_DAY_OF_WEEK_MONDAY)) {
                this.firstDayOfWeek = Calendar.MONDAY;
            } else {
                this.firstDayOfWeek = Calendar.SATURDAY;
            }

            // theme
            String them = jsonObject.optString(KEY_THEME, THEME_SYSTEM);
            if (them.equalsIgnoreCase(THEME_LIGHT)) {
                this.theme = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (them.equalsIgnoreCase(THEME_NIGHT)) {
                this.theme = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                this.theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

        } catch (Exception e) {
            Log.e("SettingsManager", "load", e);
        }
    }

    // Первый день недели
    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    // тема приложения
    public int getTheme() {
        return theme;
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public void save() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version", 6);

            String temp_firstDayOfWeek = this.firstDayOfWeek == Calendar.MONDAY ? FIRST_DAY_OF_WEEK_MONDAY : FIRST_DAY_OF_WEEK_SATURDAY;
            String temp_theme = THEME_SYSTEM;
            if (this.theme == AppCompatDelegate.MODE_NIGHT_YES) temp_theme = THEME_NIGHT;
            if (this.theme == AppCompatDelegate.MODE_NIGHT_NO) temp_theme = THEME_LIGHT;

            jsonObject.put(KEY_THEME, temp_theme);
            jsonObject.put(KEY_FIRST_DAY_OF_WEEK, temp_firstDayOfWeek);

            FileUtil.setText(saveFile, jsonObject.toString(2));
        } catch (JSONException e) {
            Log.e("SettingsManager", "save", e);
        }
    }
}
