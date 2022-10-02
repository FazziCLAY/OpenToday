package ru.fazziclay.opentoday.app.datafixer;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.app.App;

public class DataFixer {
    private final Context context;
    private final File versionFile;
    private final StringBuilder logs = new StringBuilder();
    private boolean isUpdated = false;

    public DataFixer(Context context) {
        this.context = context;
        this.versionFile = new File(context.getExternalFilesDir(""), "version");
    }

    public void fixToCurrentVersion() {
        int dataVersion = 0;

        if (!FileUtil.isExist(versionFile)) {
            // === DETECT 1 DATA VERSION
            File entry_data = new File(context.getExternalFilesDir(""), "entry_data.json");
            if (FileUtil.isExist(entry_data)) {
                dataVersion = 1;
                Log.d("DataFixer", "detect 1 dataVersion");
            } else {
                Log.d("DataFixer", "detect app not initialized!");
                return;
            }
            // === DETECT 1 DATA VERSION
        }
        if (dataVersion == 0) {
            try {
                JSONObject versionData = new JSONObject(FileUtil.getText(versionFile));
                dataVersion = versionData.getInt("data_version");
            } catch (JSONException e) {
                Log.e("DataFixer", "parse from 'version' file", e);
                return;
            }
        }
        if (dataVersion == 0) return;

        if (dataVersion == 1) {
            fix1versionTo2();
            dataVersion = 2;
            isUpdated = true;
        }

        if (dataVersion == 2) {
            try {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.deleteNotificationChannel("test");
                notificationManager.deleteNotificationChannel("mainservice");
            } catch (Exception e) {
                log("[inline v2] error delete old notify channel", e);
            }
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

        Log.d("DataFixer", "latest dataVersion = " + dataVersion);
        if (isUpdated) {
            File logFile = new File(context.getExternalCacheDir(), "data-fixer/logs/" + System.currentTimeMillis() + ".txt");
            FileUtil.setText(logFile, logs.toString());
        }
    }

    private void fix5versionTo6() {
        // DO NOT EDIT!
        final File itemsDataFile = new File(context.getExternalFilesDir(""), "item_data.json");

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
                log("[5to6] ! items key not found");
            }

            JSONArray newJsonTabs = new JSONArray();
            newJsonTabs.put(mainTab);
            newJson.put("tabs", newJsonTabs);

            log("[5to6] write to file");
            FileUtil.setText(itemsDataFile, newJson.toString(2));
            log("[5to6] write to file: DONE");


            log("[5to6] done");
        } catch (Exception e) {
            log("[5to6] exception", e);
            App.exception(context, e);
        }
    }

    private void fix4versionTo5() {
        try {
            File from = new File(context.getExternalFilesDir(""), "item_data.json");
            File to = new File(context.getExternalCacheDir(), "/data-fixer/backups/4to5/item_data.json");
            FileUtil.setText(to, FileUtil.getText(from));
            log("[4to5] backup done ");
        } catch (Exception e) {
            log("[4to5] backup exception ", e);
        }
    }

    private void fix3versionTo4() {
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
                        log("[3to4] patched: " + item);
                    }

                    i++;
                }
            }

            FileUtil.setText(itemsDataFile, j.toString(4));
            log("[3to4] DONE! patched items: " + debugStats_count);
        } catch (Exception e) {
            log("[3to4] exception", e);
        }
    }

    private void fix2versionTo3() {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.deleteNotificationChannel("foreground");
        } catch (Exception e) {
            log("[2to3] error delete old notify channel", e);
        }


        File settings = new File(context.getExternalFilesDir(""), "settings.json");
        try {
            JSONObject jsonObject = new JSONObject(FileUtil.getText(settings, "{}"));
            jsonObject.put("quickNote", true);
            jsonObject.put("version", 7);
            FileUtil.setText(settings, jsonObject.toString(4));
        } catch (Exception e){
            Log.e("DataFixer", "error add quick note", e);
        }
    }

    private void fix1versionTo2() {
        File entry_data = new File(context.getExternalFilesDir(""), "entry_data.json");
        File item_data = new File(context.getExternalFilesDir(""), "item_data.json");
        FileUtil.setText(item_data, FileUtil.getText(entry_data));
        FileUtil.delete(entry_data);
    }


    //
    private void log(String m) {
        log(m, null);
    }

    private void log(String m, Throwable t) {
        if (t != null) {
            Log.e("DataFixer-log", m, t);
        } else {
            Log.d("DataFixer-log", m);
        }

        logs.append(m).append("\n");
        if (t != null) {
            logs.append("Throwable:\n").append(Log.getStackTraceString(t)).append("\n");
        }
    }
}
