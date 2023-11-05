package com.fazziclay.opentoday.app.datafixer;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public class DataFixer {
    private static final String TAG = "DataFixer";

    private final Context context;
    private final File versionFile;
    private final StringBuilder logs = new StringBuilder();
    private boolean isUpdated = false;

    public DataFixer(Context context) {
        this.context = context;
        this.versionFile = new File(context.getExternalFilesDir(""), "version");
    }

    @NonNull
    public FixResult fixToCurrentVersion() {
        int dataVersion;
        boolean isVersionFileExist = FileUtil.isExist(versionFile);
        boolean isVersionFileOutdated = false;

        // If version file NOT EXIST
        if (isVersionFileExist) {
            try {
                JSONObject versionData = new JSONObject(FileUtil.getText(versionFile));
                dataVersion = versionData.optInt("data_version", 0);
                int applicationVersion = versionData.optInt("application_version", 0);
                isVersionFileOutdated = applicationVersion != App.VERSION_CODE;
            } catch (JSONException e) {
                Logger.e(TAG, "state: parse from 'version' file", e);
                return FixResult.NO_FIX.versionFileExist(false);
            }
        } else {
            // === DETECT 1 DATA VERSION
            File entry_data = new File(context.getExternalFilesDir(""), "entry_data.json");
            if (FileUtil.isExist(entry_data)) {
                dataVersion = 1;
                Logger.d(TAG, "detected first(1) dataVersion (entry_data.json)");
            } else {
                Logger.d(TAG, "detected not initialized app (first run?)");
                return FixResult.NO_FIX.versionFileExist(false);
            }
            // === DETECT 1 DATA VERSION
        }
        Logger.i(TAG, "parsed dataVersion = " + dataVersion);
        if (dataVersion == 0) return FixResult.NO_FIX.versionFileExist(isVersionFileExist).versionFileOutdated(isVersionFileOutdated);

        dataVersion = tryFix(dataVersion);

        Logger.i(TAG, "Fix done! Current dataVersion = " + dataVersion);
        if (isUpdated) {
            Logger.i(TAG, "isUpdated = TRUE!\nlogs: " + logs);
            File logFile = new File(context.getExternalCacheDir(), "data-fixer/logs/" + System.currentTimeMillis() + ".txt");
            FileUtil.setText(logFile, logs.toString());
            return new FixResult(dataVersion, logFile, logs.toString()).versionFileExist(isVersionFileExist).versionFileOutdated(isVersionFileOutdated);
        }
        return FixResult.NO_FIX.versionFileExist(isVersionFileExist).versionFileOutdated(isVersionFileOutdated);
    }

    // Logs
    private void log(String tag, String m) {
        log(tag, m, null);
    }

    private void log(String tag, String m, Throwable t) {
        if (t != null) {
            Logger.e("DataFixer-log", String.format("[%s] %s", tag, m), t);
        } else {
            Logger.d("DataFixer-log", String.format("[%s] %s", tag, m));
        }

        logs.append(String.format("[%s] %s", tag, m)).append("\n");
        if (t != null) {
            logs.append(String.format("[%s] Throwable:\n", tag)).append(Log.getStackTraceString(t)).append("\n");
        }
    }

    private int tryFix(int dataVersion) {
        if (dataVersion == 1) {
            fix1versionTo2();
            dataVersion = 2;
            isUpdated = true;
        }

        if (dataVersion == 2) {
            fix2versionTo3();
            dataVersion = 3;
            isUpdated = true;
        }

        if (dataVersion == 3) {
            fix3versionTo4();
            dataVersion = 4;
            isUpdated = true;
        }

        if (dataVersion == 4) {
            fix4versionTo5();
            dataVersion = 5;
            isUpdated = true;
        }

        if (dataVersion == 5) {
            fix5versionTo6();
            dataVersion = 6;
            isUpdated = true;
        }

        if (dataVersion == 6) {
            fix6versionTo7();
            dataVersion = 7;
            isUpdated = true;
        }

        if (dataVersion == 7) {
            fix7versionTo8();
            dataVersion = 8;
            isUpdated = true;
        }

        if (dataVersion == 8) {
            fix8versionTo9();
            dataVersion = 9;
            isUpdated = true;
        }

        if (dataVersion == 9) {
            // nothing to fix in to10Version
            log("v9 -> v10", "Nothing to fix while 9to10 upgrade...");
            dataVersion = 10;
            isUpdated = true;
        }

        if (dataVersion == 10) {
            // nothing to fix in to11Version: added SleepTimeItem
            log("v10 -> v11", "Nothing to fix while 10to11 upgrade...");
            dataVersion = 11;
            isUpdated = true;
        }

        if (dataVersion == 11) {
            log("v11 -> v12", "Fixing settings");
            fix11versionTo12();
            dataVersion = 12;
            isUpdated = true;
        }

        return dataVersion;
    }

    private void fix11versionTo12() {
        final String TAG = "v11 -> v12";
        // DO NOT EDIT
        try {
            File file = new File(context.getExternalFilesDir(""), "settings.json");
            JSONObject old = new JSONObject(FileUtil.getText(file));
            JSONObject nev = new JSONObject();

            if (old.has("theme")) {
                String val = old.getString("theme");
                if (val.equals("night")) {
                    nev.put("theme", "NIGHT");
                } else if (val.equals("light")) {
                    nev.put("theme", "LIGHT");
                } else {
                    nev.put("theme", "AUTO");
                }
            }

            if (old.has("firstDayOfWeek")) {
                String val = old.getString("firstDayOfWeek");
                if (val.equals("saturday")) {
                    nev.put("first_day_of_week", "SATURDAY");
                } else if (val.equals("monday")) {
                    nev.put("first_day_of_week", "MONDAY");
                }
            }

            if (old.has("quickNote")) {
                nev.put("quick_note.notification.enable", old.getBoolean("quickNote"));
            }

            if (old.has("parseTimeFromQuickNote")) {
                nev.put("quick_note.parse_time_from_item", old.getBoolean("parseTimeFromQuickNote"));
            }

            if (old.has("isMinimizeGrayColor")) {
                nev.put("item.minimize.gray_color", old.getBoolean("isMinimizeGrayColor"));
            }

            if (old.has("trimItemNamesOnEdit")) {
                nev.put("item.editor.trim_names", old.getBoolean("trimItemNamesOnEdit"));
            }

            if (old.has("itemOnClickAction")) {
                nev.put("item.action.click", old.getString("itemOnClickAction"));
            }

            if (old.has("itemOnLeftAction")) {
                nev.put("item.action.left_swipe", old.getString("itemOnLeftAction"));
            }

            if (old.has("isTelemetry")) {
                nev.put("telemetry.enable", old.getBoolean("isTelemetry"));
            }

            if (old.has("defaultQuickNoteType")) {
                String defaultQuickNoteType = old.getString("defaultQuickNoteType");
                String c = switch (defaultQuickNoteType) {
                    case "CheckboxItem" -> "CHECKBOX";
                    case "GroupItem" -> "GROUP";
                    case "FilterGroupItem" -> "FILTER_GROUP";
                    case "LongTextItem" -> "LONG_TEXT";
                    case "DayRepeatableCheckboxItem" -> "CHECKBOX_DAY_REPEATABLE";
                    default -> "TEXT";
                };
                nev.put("quick_note.item_type", c);
            }

            if (old.has("firstTab")) {
                nev.put("first_tab", old.getString("firstTab"));
            }

            if (old.has("itemAddPosition")) {
                nev.put("item.add_position", old.getString("itemAddPosition"));
            }

            if (old.has("confirmFastChanges")) {
                nev.put("fast_changes.confirm", old.getBoolean("confirmFastChanges"));
            }

            if (old.has("isAutoCloseToolbar")) {
                nev.put("toolbar.auto_close", old.getBoolean("isAutoCloseToolbar"));
            }

            if (old.has("isScrollToAddedItem")) {
                nev.put("item.is_scroll_to_added", old.getBoolean("isScrollToAddedItem"));
            }

            if (old.has("isItemEditorBackgroundFromItem")) {
                nev.put("item.use_container_editor_background_from_item", old.getBoolean("isItemEditorBackgroundFromItem"));
            }


            FileUtil.setText(file, nev.toString());
        } catch (Exception e) {
            log(TAG, "exception: " + e);
        }
    }

    private void fix8versionTo9() {
        final String TAG = "v8 -> v9";
        log(TAG, "FIX START");
        log(TAG, "(start external fixer)");
        Scheme8Fix9.fix8versionTo9(context);
        log(TAG, "FIX DONE");
    }

    // Moved instanceId from settings to external file
    private void fix7versionTo8() {
        final String TAG = "v7 -> v8";
        log(TAG, "FIX START");

        // DO NOT EDIT!
        File instanceIdFile = new File(context.getExternalFilesDir(""), "instanceId");
        File settingsFile = new File(context.getExternalFilesDir(""), "settings.json");
        final String KEY = "instanceId";
        if (!FileUtil.isExist(settingsFile)) return;
        try {
            JSONObject j = new JSONObject(FileUtil.getText(settingsFile, "{}"));
            if (j.has(KEY)) {
                UUID uuid = UUID.fromString(j.getString(KEY));
                FileUtil.setText(instanceIdFile, uuid.toString());
            } else {
                log(TAG, "instanceId key not found!");
            }

            log(TAG, "FIX DONE");
        } catch (Exception e) {
            log(TAG, "FIX Exception", e);
        }
    }

    // Add tabType to tabs
    private void fix6versionTo7() {
        final String TAG = "v6 -> v7";
        log(TAG, "FIX START");

        // DO NOT EDIT!
        final File itemsDataFile = new File(context.getExternalFilesDir(""), "item_data.json");

        try {
            JSONObject json = new JSONObject(FileUtil.getText(itemsDataFile));
            JSONArray jsonTabs = json.getJSONArray("tabs");

            int i = 0;
            while (i < jsonTabs.length()) {
                JSONObject jsonTab = jsonTabs.getJSONObject(i);
                jsonTab.put("tabType", "LocalItemsTab");
                log(TAG, String.format("tab (%s) patched", i));
                i++;
            }

            log(TAG, "Write to file");
            FileUtil.setText(itemsDataFile, json.toString(2));
            log(TAG, "Write to file: DONE");

            log(TAG, "FIX DONE");

        } catch (Exception e) {
            log(TAG, "FIX Exception", e);
        }
    }

    // Added tabs support
    private void fix5versionTo6() {
        final String TAG = "v5 -> v6";
        log(TAG, "FIX START");

        // DO NOT EDIT!
        final File itemsDataFile = new File(context.getExternalFilesDir(""), "item_data.json");
        if (!FileUtil.isExist(itemsDataFile)) return;
        try {
            JSONObject oldJson = new JSONObject(FileUtil.getText(itemsDataFile));
            JSONObject newJson = new JSONObject();

            JSONObject mainTab = new JSONObject();
            mainTab.put("id", UUID.randomUUID());
            mainTab.put("name", "My Items");

            if (oldJson.has("items")) {
                JSONArray items = oldJson.getJSONArray("items");
                mainTab.put("items", items);

            } else {
                mainTab.put("items", new JSONArray());
                log(TAG, "items key not found");
            }

            JSONArray newJsonTabs = new JSONArray();
            newJsonTabs.put(mainTab);
            newJson.put("tabs", newJsonTabs);

            log(TAG, "Write to file");
            FileUtil.setText(itemsDataFile, newJson.toString(2));
            log(TAG, "Write to file: DONE");


            log(TAG, "FIX DONE");
        } catch (Exception e) {
            log(TAG, "FIX Exception", e);
        }
    }

    private void fix4versionTo5() {
        final String TAG = "v4 -> v5 (only backup)";
        log(TAG, "FIX START");

        try {
            File from = new File(context.getExternalFilesDir(""), "item_data.json");
            File to = new File(context.getExternalCacheDir(), "/data-fixer/backups/4to5/item_data.json");
            FileUtil.setText(to, FileUtil.getText(from));
            log(TAG, "Backup done");

        } catch (Exception e) {
            log(TAG, "Backup exception ", e);
        }
    }

    // CycleListItem itemsCycleBackgroundWork -> tickBehavior
    private void fix3versionTo4() {
        final String TAG = "v3 -> v4";
        log(TAG, "FIX START");

        // DO NOT EDIT!
        final File itemsDataFile = new File(context.getExternalFilesDir(""), "item_data.json");
        if (!FileUtil.isExist(itemsDataFile)) return;
        final String ITEM_TYPE = "CycleListItem";
        final String OLD_KEY = "itemsCycleBackgroundWork";
        final String NEW_KEY = "tickBehavior";
        int debugStats_count = 0;
        try {
            JSONObject j = new JSONObject(FileUtil.getText(itemsDataFile));

            if (j.has("items")) {
                JSONArray items = j.getJSONArray("items");
                int i = 0;
                while (i < items.length()) {
                    JSONObject item = items.getJSONObject(i);
                    if (ITEM_TYPE.equals(item.optString("itemType"))) {
                        item.put(NEW_KEY, item.getString(OLD_KEY));
                        item.remove(OLD_KEY);
                        debugStats_count++; // debug stats
                        log(TAG, "Patch " + i);
                    }

                    i++;
                }
            }

            FileUtil.setText(itemsDataFile, j.toString(4));
            log(TAG, "FIX DONE! patched items: " + debugStats_count);

        } catch (Exception e) {
            log(TAG, "FIX Exception", e);
        }
    }

    private void fix2versionTo3() {
        final String TAG = "v2 -> v3";
        log(TAG, "FIX START");

        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.deleteNotificationChannel("test");
            notificationManager.deleteNotificationChannel("mainservice");
            notificationManager.deleteNotificationChannel("foreground");
        } catch (Exception e) {
            log(TAG, "Exception while delete old notify channels", e);
        }

        File settings = new File(context.getExternalFilesDir(""), "settings.json");

        try {
            JSONObject jsonObject = new JSONObject(FileUtil.getText(settings, "{}"));
            jsonObject.put("quickNote", true);
            jsonObject.put("version", 7);
            FileUtil.setText(settings, jsonObject.toString(4));
        } catch (Exception e) {
            log(TAG, "Exception while add quick note to settings", e);
        }
        log(TAG, "FIX DONE");
    }

    private void fix1versionTo2() {
        final String TAG = "v1 -> v2";
        log(TAG, "FIX START");

        File entry_data = new File(context.getExternalFilesDir(""), "entry_data.json");
        if (!FileUtil.isExist(entry_data)) return;
        File item_data = new File(context.getExternalFilesDir(""), "item_data.json");
        FileUtil.setText(item_data, FileUtil.getText(entry_data));
        log(TAG, "Copy file (entry_data.json -> item_data.json)");
        FileUtil.delete(entry_data);
        log(TAG, "Delete entry_data.json");

        log(TAG, "FIX DONE");
    }

    public JSONArray fixItems(int from, JSONArray items) throws Exception {
        if (from < 8) throw new RuntimeException("Oldest dataVersion for this functional");

        JSONArray result = items;
        if (from == 8) {
            Scheme8Fix9.itemsListFix(context, items);
            result = items;
            from = 9;
        }

        if (from == 9) {
            // nothing to fix
            from = 10;
        }


        if (from == 10) {
            // nothing to fix
            from = 11;
        }

        if (from == 11) {
            // nothing to fix
            from = 12;
        }

        return result;
    }

    public JSONArray fixTabs(int from, JSONArray tabs) throws Exception {
        if (from < 8) throw new RuntimeException("Oldest dataVersion for this functional");

        JSONArray result = tabs;
        if (from == 8) {
            Scheme8Fix9.tabsListFix(context, tabs);
            result = tabs;
            from = 9;
        }

        if (from == 9) {
            // nothing to fix
            from = 10;
        }

        if (from == 10) {
            // nothing to fix
            from = 11;
        }

        if (from == 11) {
            // nothing to fix
            from = 12;
        }
        return result;
    }
}
