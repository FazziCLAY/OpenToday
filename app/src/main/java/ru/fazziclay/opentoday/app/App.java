package ru.fazziclay.opentoday.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONObject;

import java.io.File;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.BuildConfig;
import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.datafixer.DataFixer;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.app.updatechecker.UpdateChecker;
import ru.fazziclay.opentoday.util.DebugUtil;

@SuppressWarnings("PointlessBooleanExpression") // for debug variables
public class App extends Application {
    // Application
    public final static int APPLICATION_DATA_VERSION = 2;
    public static final String VERSION_NAME = BuildConfig.VERSION_NAME;
    public static final int VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String APPLICATION_ID = BuildConfig.APPLICATION_ID;

    // Notifications
    public static final String NOTIFICATION_FOREGROUND_CHANNEL = "foreground";
    public static final int NOTIFICATION_FOREGROUND_ID = 1;

    // DEBUG
    public final static boolean DEBUG = BuildConfig.DEBUG;
    public final static int MAIN_ACTIVITY_START_SLEEP = (DEBUG & false) ? 6000 : 0;
    public final static int APP_START_SLEEP = (DEBUG & false) ? 1000 : 0;

    // Instance
    private volatile static App instance = null;
    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
    public static App get() {
        return instance;
    }

    // Application
    private ItemManager itemManager;
    private SettingsManager settingsManager;
    private NotificationManager notificationManager;
    private UpdateChecker updateChecker;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        /* debug */ DebugUtil.sleep(APP_START_SLEEP);

        DataFixer dataFixer = new DataFixer(this);
        dataFixer.fixToCurrentVersion();
        try {
            FileUtil.setText(new File(getExternalFilesDir(""),"version"), new JSONObject()
                    .put("product", "OpenToday")
                    .put("developer", "FazziCLAY ( https://fazziclay.github.io )")
                    .put("licence", "GNU GPLv3")
                    .put("data_version", APPLICATION_DATA_VERSION)
                    .put("latest_start", System.currentTimeMillis())
                    .toString(4));
        } catch (Exception e) {
            throw new RuntimeException("Exception!", e);
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        itemManager = new ItemManager(new File(getExternalFilesDir(""), "item_data.json"));
        settingsManager = new SettingsManager(new File(getExternalFilesDir(""), "settings.json"));
        updateChecker = new UpdateChecker();

        AppCompatDelegate.setDefaultNightMode(settingsManager.getTheme());
        notificationManager.createNotificationChannel(new NotificationChannel("foreground", getString(R.string.notification_foreground_title), NotificationManager.IMPORTANCE_HIGH));
        startService(new Intent(this, MainService.class));
    }

    // getters
    public ItemManager getItemManager() { return itemManager; }
    public SettingsManager getSettingsManager() { return this.settingsManager; }
    public NotificationManager getNotificationManager() { return notificationManager; }
    public UpdateChecker getUpdateChecker() { return updateChecker; }
    // not getters :)
}
