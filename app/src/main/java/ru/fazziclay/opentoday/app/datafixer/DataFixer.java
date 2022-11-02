package ru.fazziclay.opentoday.app.datafixer;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

import ru.fazziclay.javaneoutil.FileUtil;

public class DataFixer {
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

        // If version file NOT EXIST
        if (isVersionFileExist) {
            try {
                JSONObject versionData = new JSONObject(FileUtil.getText(versionFile));
                dataVersion = versionData.getInt("data_version");
            } catch (JSONException e) {
                Log.e("DataFixer", "parse from 'version' file", e);
                return FixResult.NO_FIX;
            }
        } else {
            // === DETECT 1 DATA VERSION
            File entry_data = new File(context.getExternalFilesDir(""), "entry_data.json");
            if (FileUtil.isExist(entry_data)) {
                dataVersion = 1;
                Log.d("DataFixer", "detect 1 dataVersion");
            } else {
                Log.d("DataFixer", "detect app not initialized!");
                return FixResult.NO_FIX;
            }
            // === DETECT 1 DATA VERSION
        }
        if (dataVersion == 0) return FixResult.NO_FIX.versionFileExist(isVersionFileExist);

        dataVersion = tryFix(dataVersion);

        Log.d("DataFixer", "latest dataVersion = " + dataVersion);
        if (isUpdated) {
            File logFile = new File(context.getExternalCacheDir(), "data-fixer/logs/" + System.currentTimeMillis() + ".txt");
            FileUtil.setText(logFile, logs.toString());
            return new FixResult(dataVersion, logFile, logs.toString()).versionFileExist(isVersionFileExist);
        }
        return FixResult.NO_FIX.versionFileExist(isVersionFileExist);
    }

    // Logs
    private void log(String tag, String m) {
        log(tag, m, null);
    }

    private void log(String tag, String m, Throwable t) {
        if (t != null) {
            Log.e("DataFixer-log", String.format("[%s] %s", tag, m), t);
        } else {
            Log.d("DataFixer-log", String.format("[%s] %s", tag, m));
        }

        logs.append(String.format("[%s] %s", tag, m)).append("\n");
        if (t != null) {
            logs.append(String.format("[%s] Throwable:\n", tag)).append(Log.getStackTraceString(t)).append("\n");
        }
    }

    public static class FixResult {
        public static FixResult NO_FIX = new FixResult();

        private final boolean fixed;
        private boolean versionFileExist = false;
        private int dataVersion;
        private File logFile;
        private String logs;

        public FixResult(int dataVersion, File logFile, String logs) {
            this.dataVersion = dataVersion;
            this.logFile = logFile;
            this.logs = logs;
            this.fixed = true;
        }

        private FixResult() {
            this.fixed = false;
        }

        public FixResult versionFileExist(boolean b) {
            this.versionFileExist = b;
            return this;
        }

        public boolean isFixed() {
            return fixed;
        }

        public boolean isVersionFileExist() {
            return versionFileExist;
        }

        public int getDataVersion() {
            return dataVersion;
        }

        public File getLogFile() {
            return logFile;
        }

        public String getLogs() {
            return logs;
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

        return dataVersion;
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
}
