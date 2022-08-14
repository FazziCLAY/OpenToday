package ru.fazziclay.opentoday.app.datafixer;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import ru.fazziclay.javaneoutil.FileUtil;

public class DataFixer {
    private final Context context;
    private final File versionFile;

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
        }

        if (dataVersion == 2) {
            try {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.deleteNotificationChannel("test");
                notificationManager.deleteNotificationChannel("mainservice");
            } catch (Exception e) {
                Log.e("DataFixer", "error delete old notify channel", e);
            }
        }

        if (dataVersion == 2) {
            fix2versionTo3();
            dataVersion = 3;
        }

        Log.d("DataFixer", "latest dataVersion = " + dataVersion);
    }

    private void fix2versionTo3() {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.deleteNotificationChannel("foreground");
        } catch (Exception e) {
            Log.e("DataFixer", "error delete old notify channel", e);
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
}
