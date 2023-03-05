package com.fazziclay.opentoday.app.settings;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.util.L;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.Setter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

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
    private UUID quickNoteNotificationItemsStorageId = null;
    private boolean isTelemetry = true;
    private ItemsRegistry.ItemInfo defaultQuickNoteType = ItemsRegistry.REGISTRY.get(TextItem.class);
    private FirstTab firstTab = FirstTab.TAB_ON_CLOSING;


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
    @Getter public boolean isMinimizeGrayColor() { return isMinimizeGrayColor; }
    @Setter public void setMinimizeGrayColor(boolean minimizeGrayColor) {isMinimizeGrayColor = minimizeGrayColor;}
    @Getter public boolean isParseTimeFromQuickNote() { return parseTimeFromQuickNote; }
    @Setter public void setParseTimeFromQuickNote(boolean parseTimeFromQuickNote) {this.parseTimeFromQuickNote = parseTimeFromQuickNote;}
    @Getter public boolean isTrimItemNamesOnEdit() {return trimItemNamesOnEdit;}
    @Setter public void setTrimItemNamesOnEdit(boolean trimItemNamesOnEdit) {this.trimItemNamesOnEdit = trimItemNamesOnEdit;}
    @Getter public ItemAction getItemOnClickAction() {return itemOnClickAction;}
    @Setter public void setItemOnClickAction(ItemAction itemOnClickAction) {this.itemOnClickAction = itemOnClickAction;}
    @Getter public ItemAction getItemOnLeftAction() {return itemOnLeftAction;}
    @Setter public void setItemOnLeftAction(ItemAction itemOnLeftAction) {this.itemOnLeftAction = itemOnLeftAction;}
    @Getter public UUID getQuickNoteNotificationItemsStorageId() {return quickNoteNotificationItemsStorageId;}
    @Setter public void setQuickNoteNotificationItemsStorageId(UUID quickNoteNotificationItemsStorageId) {this.quickNoteNotificationItemsStorageId = quickNoteNotificationItemsStorageId;}
    @Getter public boolean isTelemetry() {return isTelemetry;}
    @Setter public void setTelemetry(boolean b) {this.isTelemetry = b;}
    @Getter public ItemsRegistry.ItemInfo getDefaultQuickNoteType() {return defaultQuickNoteType;}
    @Setter public void setDefaultQuickNoteType(ItemsRegistry.ItemInfo defaultQuickNoteType) {this.defaultQuickNoteType = defaultQuickNoteType;}
    @Getter public FirstTab getFirstTab() {return firstTab;}
    @Setter public void setFirstTab(FirstTab firstTab) {this.firstTab = firstTab;}

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
            try {
                this.quickNoteNotificationItemsStorageId = UUID.fromString(j.optString("quickNoteNotificationItemsStorageId"));
            } catch (Exception ignored) {}
            this.isTelemetry = j.optBoolean("isTelemetry", true);
            try {
                this.defaultQuickNoteType = ItemsRegistry.REGISTRY.get(j.getString("defaultQuickNoteType"));
            } catch (Exception ignored) {}

            try {
                this.firstTab = FirstTab.valueOf(j.getString("firstTab"));
            } catch (Exception ignored) {}

        } catch (Exception e) {
            L.o("SettingsManager", "load", e);
            App.exception(null, e);
        }
    }

    public void save() {
        try {
            JSONObject j = exportJSONSettings();
            FileUtil.setText(saveFile, j.toString());
        } catch (Exception e) {
            L.o("SettingsManager", "save", e);
            App.exception(null, e);
        }
    }

    public JSONObject exportJSONSettings() throws JSONException {
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
        j.put("quickNoteNotificationItemsStorageId", quickNoteNotificationItemsStorageId != null ? quickNoteNotificationItemsStorageId.toString() : null);
        j.put("isTelemetry", this.isTelemetry);
        j.put("defaultQuickNoteType", this.defaultQuickNoteType.getStringType());
        j.put("firstTab", this.firstTab.name());
        return j;
    }

    public void importData(JSONObject settings) {
        FileUtil.setText(saveFile, settings.toString());
        load();
    }


    public enum ItemAction {
        OPEN_EDITOR(R.string.itemAction_OPEN_EDITOR),
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

    public enum FirstTab {
        FIRST,
        TAB_ON_CLOSING
    }
}
