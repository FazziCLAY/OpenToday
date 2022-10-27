package ru.fazziclay.opentoday.app.settings;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.Setter;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.util.L;

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

    private static final String KEY_QUICK_NOTE_NOTIFICATION = "quickNote"; // TODO: 14.10.2022 rename & add to DataFixer
    private static final String KEY_PARSETIMEFROMQUICKNOTE = "parseTimeFromQuickNote"; // TODO: 14.10.2022 add to datafixer
    private static final String KEY_ISMINIMIZEGRAYCOLOR = "isMinimizeGrayColor"; // TODO: 14.10.2022 add to datafixer
    private static final String KEY_TRIMITEMNAMESONEDIT = "trimItemNamesOnEdit";

    // local
    private final File saveFile;

    private int firstDayOfWeek = Calendar.SUNDAY;
    private int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    private boolean quickNoteNotification = true;
    private boolean parseTimeFromQuickNote = true;
    private boolean isMinimizeGrayColor = false;
    private boolean trimItemNamesOnEdit = true;
    private ItemAction itemOnClickAction = ItemAction.OPEN_EDITOR;
    private ItemAction itemOnLeftAction = ItemAction.MINIMIZE_REVERT;



    public SettingsManager(File saveFile) {
        this.saveFile = saveFile;
        load();
    }

    @Getter public int getFirstDayOfWeek() { return firstDayOfWeek; }
    @Setter public void setFirstDayOfWeek(int firstDayOfWeek) { this.firstDayOfWeek = firstDayOfWeek; }
    @Getter public int getTheme() { return theme; }
    @Setter public void setTheme(int theme) { this.theme = theme; }
    @Getter public boolean isQuickNoteNotification() { return quickNoteNotification; }
    @Setter public void setQuickNoteNotification(boolean quickNoteNotification) { this.quickNoteNotification = quickNoteNotification; }
    @Setter public void setMinimizeGrayColor(boolean minimizeGrayColor) {isMinimizeGrayColor = minimizeGrayColor;}
    @Getter public boolean isMinimizeGrayColor() { return isMinimizeGrayColor; }
    @Setter public void setParseTimeFromQuickNote(boolean parseTimeFromQuickNote) {this.parseTimeFromQuickNote = parseTimeFromQuickNote;}
    @Getter public boolean isParseTimeFromQuickNote() { return parseTimeFromQuickNote; }
    @Getter public boolean isTrimItemNamesOnEdit() {return trimItemNamesOnEdit;}
    @Setter public void setTrimItemNamesOnEdit(boolean trimItemNamesOnEdit) {this.trimItemNamesOnEdit = trimItemNamesOnEdit;}
    @Getter public ItemAction getItemOnClickAction() {return itemOnClickAction;}
    @Setter public void setItemOnClickAction(ItemAction itemOnClickAction) {this.itemOnClickAction = itemOnClickAction;}
    @Getter public ItemAction getItemOnLeftAction() {return itemOnLeftAction;}
    @Setter public void setItemOnLeftAction(ItemAction itemOnLeftAction) {this.itemOnLeftAction = itemOnLeftAction;}

    private void load() {
        if (!FileUtil.isExist(saveFile)) {
            return;
        }
        try {
            JSONObject j = new JSONObject(FileUtil.getText(saveFile, "{}"));
            // first day of week
            String dayOfWeek = j.optString(KEY_FIRST_DAY_OF_WEEK, FIRST_DAY_OF_WEEK_SATURDAY);
            if (dayOfWeek.equalsIgnoreCase(FIRST_DAY_OF_WEEK_MONDAY)) {
                this.firstDayOfWeek = Calendar.MONDAY;
            } else {
                this.firstDayOfWeek = Calendar.SATURDAY;
            }

            // theme
            String them = j.optString(KEY_THEME, THEME_SYSTEM);
            if (them.equalsIgnoreCase(THEME_LIGHT)) {
                this.theme = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (them.equalsIgnoreCase(THEME_NIGHT)) {
                this.theme = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                this.theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            // Quick note and etc...
            this.quickNoteNotification = j.optBoolean(KEY_QUICK_NOTE_NOTIFICATION, this.quickNoteNotification);
            this.parseTimeFromQuickNote = j.optBoolean(KEY_PARSETIMEFROMQUICKNOTE, this.parseTimeFromQuickNote);
            this.isMinimizeGrayColor = j.optBoolean(KEY_ISMINIMIZEGRAYCOLOR, this.isMinimizeGrayColor);
            this.trimItemNamesOnEdit = j.optBoolean(KEY_TRIMITEMNAMESONEDIT, this.trimItemNamesOnEdit);
            try {
                this.itemOnClickAction = ItemAction.valueOf(j.optString("itemOnClickAction"));
            } catch (Exception ignored) {}
            try {
                this.itemOnLeftAction = ItemAction.valueOf(j.optString("itemOnLeftAction"));
            } catch (Exception ignored) {}

        } catch (Exception e) {
            L.o("SettingsManager", "load", e);
            App.exception(null, e);
        }
    }

    public void save() {
        try {
            JSONObject j = new JSONObject();

            String temp_firstDayOfWeek = this.firstDayOfWeek == Calendar.MONDAY ? FIRST_DAY_OF_WEEK_MONDAY : FIRST_DAY_OF_WEEK_SATURDAY;
            String temp_theme = THEME_SYSTEM;
            if (this.theme == AppCompatDelegate.MODE_NIGHT_YES) temp_theme = THEME_NIGHT;
            if (this.theme == AppCompatDelegate.MODE_NIGHT_NO) temp_theme = THEME_LIGHT;

            j.put(KEY_THEME, temp_theme);
            j.put(KEY_FIRST_DAY_OF_WEEK, temp_firstDayOfWeek);
            j.put(KEY_QUICK_NOTE_NOTIFICATION, this.quickNoteNotification);
            j.put(KEY_PARSETIMEFROMQUICKNOTE, this.parseTimeFromQuickNote);
            j.put(KEY_ISMINIMIZEGRAYCOLOR, this.isMinimizeGrayColor);
            j.put(KEY_TRIMITEMNAMESONEDIT, this.trimItemNamesOnEdit);
            j.put("itemOnClickAction", itemOnClickAction.name());
            j.put("itemOnLeftAction", itemOnLeftAction.name());

            FileUtil.setText(saveFile, j.toString(2));
        } catch (Exception e) {
            L.o("SettingsManager", "save", e);
            App.exception(null, e);
        }
    }


    public enum ItemAction {
        OPEN_EDITOR(R.string.itemAction_OPEN_EDIT_DIALOG),
        SELECT_REVERT(R.string.itemAction_SELECT_REVERT),
        SELECT_ON(R.string.itemAction_SELECT_ON),
        SELECT_OFF(R.string.itemAction_SELECT_OFF),
        DELETE_REQUEST(R.string.itemAction_DELETE_REQUEST),
        MINIMIZE_REVERT(R.string.itemAction_MINIMIZE_REVERT),
        MINIMIZE_ON(R.string.itemAction_MINIMIZE_ON),
        MINIMIZE_OFF(R.string.itemAction_MINIMIZE_OFF);

        @StringRes
        private final int resId;

        ItemAction(@StringRes int resId) {
            this.resId = resId;
        }

        public int nameResId() {
            return resId;
        }
    }
}
