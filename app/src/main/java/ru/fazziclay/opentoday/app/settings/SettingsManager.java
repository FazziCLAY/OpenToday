package ru.fazziclay.opentoday.app.settings;

import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.javaneoutil.NonNull;
import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.Setter;

public class SettingsManager {
    private static final int VERSION = 7;

    // Theme
    private static final String KEY_THEME = "theme";
    private static final String THEME_SYSTEM = "system";
    private static final String THEME_NIGHT = "night";
    private static final String THEME_LIGHT = "light";

    // First day of week
    private static final String KEY_FIRST_DAY_OF_WEEK = "firstDayOfWeek";
    private static final String FIRST_DAY_OF_WEEK_SATURDAY = "saturday";
    private static final String FIRST_DAY_OF_WEEK_MONDAY = "monday";

    private static final String KEY_QUICK_NOTE = "quickNote";

    //
    private final File saveFile;
    private int firstDayOfWeek = Calendar.SUNDAY;
    private int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    private boolean quickNote = true;
    private UUID instanceId = null;
    private final boolean parseTimeFromQuickNote = true; // TODO: 02.10.2022 owo
    private final boolean isMinimizeGrayColor = false; // TODO: 02.10.2022 owo

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
            int version = jsonObject.optInt("version", -1);
            if (version != VERSION) {
                Log.e("SettingsManager", "version unspecified; version=" + version);
            }

            if (jsonObject.has("instanceId")) {
                instanceId = UUID.fromString(jsonObject.getString("instanceId"));
            }

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
            this.quickNote = jsonObject.optBoolean(KEY_QUICK_NOTE, this.quickNote);

        } catch (Exception e) {
            Log.e("SettingsManager", "load", e);
        }
    }

    @Getter public int getFirstDayOfWeek() { return firstDayOfWeek; }
    @Setter public void setFirstDayOfWeek(int firstDayOfWeek) { this.firstDayOfWeek = firstDayOfWeek; }
    @Getter public int getTheme() { return theme; }
    @Setter public void setTheme(int theme) { this.theme = theme; }
    @Getter public boolean isQuickNote() { return quickNote; }
    @Setter public void setQuickNote(boolean quickNote) { this.quickNote = quickNote; }

    public void save() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("instanceId", instanceId.toString());
            jsonObject.put("version", VERSION);

            String temp_firstDayOfWeek = this.firstDayOfWeek == Calendar.MONDAY ? FIRST_DAY_OF_WEEK_MONDAY : FIRST_DAY_OF_WEEK_SATURDAY;
            String temp_theme = THEME_SYSTEM;
            if (this.theme == AppCompatDelegate.MODE_NIGHT_YES) temp_theme = THEME_NIGHT;
            if (this.theme == AppCompatDelegate.MODE_NIGHT_NO) temp_theme = THEME_LIGHT;

            jsonObject.put(KEY_THEME, temp_theme);
            jsonObject.put(KEY_FIRST_DAY_OF_WEEK, temp_firstDayOfWeek);
            jsonObject.put(KEY_QUICK_NOTE, quickNote);

            FileUtil.setText(saveFile, jsonObject.toString(2));
        } catch (JSONException e) {
            Log.e("SettingsManager", "save", e);
        }
    }

    // TODO: 14.09.2022 make variable
    public boolean isMinimizeGrayColor() {
        return isMinimizeGrayColor;
    }

    @NonNull
    public UUID getInstanceId() {
        if (instanceId == null) {
            instanceId = UUID.randomUUID();
            save();
        }
        return instanceId;
    }

    public boolean isParseTimeFromQuickNote() {
        return parseTimeFromQuickNote;
    }
}
