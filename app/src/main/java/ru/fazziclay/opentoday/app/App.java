package ru.fazziclay.opentoday.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.File;
import java.util.Random;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.BuildConfig;
import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.datafixer.DataFixer;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.receiver.ItemsTickReceiver;
import ru.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.debug.TestItemViewGenerator;
import ru.fazziclay.opentoday.ui.activity.CrashReportActivity;
import ru.fazziclay.opentoday.util.DebugUtil;
import ru.fazziclay.opentoday.util.Profiler;

@SuppressWarnings("PointlessBooleanExpression") // for debug variables
public class App extends Application {
    // Application
    public static final int APPLICATION_DATA_VERSION = 5;
    public static final String VERSION_NAME = BuildConfig.VERSION_NAME;
    public static final int VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String APPLICATION_ID = BuildConfig.APPLICATION_ID;

    // Notifications
    public static final String NOTIFICATION_QUCIKNOTE_CHANNEL = QuickNoteReceiver.NOTIFICATION_CHANNEL;
    public static final String NOTIFICATION_ITEMS_CHANNEL = "items_notifications";
    private static final String NOTIFICATION_CRASH_CHANNEL = "crash_report";

    // DEBUG
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final boolean DEBUG_TICK_NOTIFICATION = (DEBUG & false);
    public static final int DEBUG_MAIN_ACTIVITY_START_SLEEP = (DEBUG & false) ? 6000 : 0;
    public static final int DEBUG_APP_START_SLEEP = (DEBUG & false) ? 1000 : 0;
    public static Class<? extends Activity> DEBUG_MAIN_ACTIVITY = (DEBUG & false) ? TestItemViewGenerator.class : null;
    public static final boolean DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY = (DEBUG && false);

    private static Thread.UncaughtExceptionHandler defaultHandler;


    // Instance
    private static volatile App instance = null;
    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
    public static App get() {
        return instance;
    }

    // Application
    private ItemManager itemManager;
    private SettingsManager settingsManager;
    private Telemetry telemetry;
    private JSONObject versionData;
    private boolean appInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            instance = this;
            setupCrashReporter();
            /* debug */ DebugUtil.sleep(DEBUG_APP_START_SLEEP);

            DataFixer dataFixer = new DataFixer(this);
            dataFixer.fixToCurrentVersion();

            try {
                this.versionData = new JSONObject()
                        .put("product", "OpenToday")
                        .put("developer", "FazziCLAY ( https://fazziclay.github.io )")
                        .put("licence", "GNU GPLv3")
                        .put("data_version", APPLICATION_DATA_VERSION)
                        .put("application_version", VERSION_CODE)
                        .put("latest_start", System.currentTimeMillis());
                FileUtil.setText(new File(getExternalFilesDir(""), "version"), versionData.toString(4));
            } catch (Exception e) {
                throw new RuntimeException("Exception!", e);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            itemManager = new ItemManager(new File(getExternalFilesDir(""), "item_data.json"));
            settingsManager = new SettingsManager(new File(getExternalFilesDir(""), "settings.json"));
            telemetry = new Telemetry(this);

            sendBroadcast(new Intent(this, ItemsTickReceiver.class));

            AppCompatDelegate.setDefaultNightMode(settingsManager.getTheme());
            notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_QUCIKNOTE_CHANNEL, getString(R.string.notification_quickNote_title), NotificationManager.IMPORTANCE_HIGH));
            notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_ITEMS_CHANNEL, getString(R.string.notification_items_title), NotificationManager.IMPORTANCE_HIGH));

            //AlarmManager alarmManager = getSystemService(AlarmManager.class);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5 * 60 * 1000, PendingIntent.getBroadcast(this, 0, new Intent(this, ItemsTickReceiver.class), 0));

            telemetry.applicationStart();
        } catch (Exception e) {
            crash(this, CrashReport.create(Thread.currentThread(), new RuntimeException("opentoday.app.App initialization exception", e), System.currentTimeMillis(), System.nanoTime(), Thread.getAllStackTraces()), false);
        }
    }

    private void setupCrashReporter() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> App.crash(App.this, CrashReport.create(thread, throwable, System.currentTimeMillis(), System.nanoTime(), Thread.getAllStackTraces())));
    }

    public static void exception(Context context, Exception exception) {
        App.crash(context, CrashReport.create(Thread.currentThread(), exception, System.currentTimeMillis(), System.nanoTime(), Thread.getAllStackTraces()), false);
    }

    public static void crash(Context context, CrashReport crashReport) {
        crash(context, crashReport, true);
    }

    public static void crash(Context context, CrashReport crashReport, boolean sendToUp) {
        // === File ===
        File file = new File(context.getExternalCacheDir(), "crash_report/" + crashReport.getID().toString());
        FileUtil.setText(file, crashReport.convertToText());

        // === NOTIFICATION ===
        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CRASH_CHANNEL, context.getString(R.string.notification_crash_title), NotificationManager.IMPORTANCE_DEFAULT));

        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
        } else {
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        notificationManager.notify(new Random().nextInt(), new NotificationCompat.Builder(context, App.NOTIFICATION_CRASH_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.crash_notification_title))
                .setContentText(context.getString(R.string.crash_notification_text))
                .setSubText(context.getString(R.string.crash_notification_subtext))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getString(R.string.crash_notification_big_text))
                        .setBigContentTitle(context.getString(R.string.crash_notification_big_title))
                        .setSummaryText(context.getString(R.string.crash_notification_big_summary)))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, CrashReportActivity.class).putExtra("path", file.getAbsolutePath()), flag))
                .setAutoCancel(true)
                .build());

        App app = App.get(context);
        if (app != null) {
            if (app.telemetry != null) {
                app.telemetry.crash(context, crashReport);
                DebugUtil.sleep(1000);
            }
        }

        if (defaultHandler != null && sendToUp) {
            if (DEBUG) DebugUtil.sleep(7000);
            defaultHandler.uncaughtException(crashReport.getThread(), crashReport.getThrowable());
        }
    }

    // getters & setters
    public ItemManager getItemManager() { return itemManager; }
    public SettingsManager getSettingsManager() { return this.settingsManager; }
    public boolean isAppInForeground() { return appInForeground; }
    public void setAppInForeground(boolean appInForeground) { this.appInForeground = appInForeground; }
    public Telemetry getTelemetry() { return telemetry; }
    public JSONObject getVersionData() { return versionData; }
    // not getters & setters :)
}
