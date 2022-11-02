package ru.fazziclay.opentoday.app;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.fazziclay.neosocket.NeoSocket;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.javaneoutil.JavaNeoUtil;
import ru.fazziclay.javaneoutil.NonNull;
import ru.fazziclay.opentoday.BuildConfig;
import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.annotation.AppInitIfNeed;
import ru.fazziclay.opentoday.app.datafixer.DataFixer;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.debug.TestItemViewGenerator;
import ru.fazziclay.opentoday.telemetry.OpenTodayTelemetry;
import ru.fazziclay.opentoday.ui.activity.CrashReportActivity;
import ru.fazziclay.opentoday.util.DebugUtil;

@SuppressWarnings("PointlessBooleanExpression") // for debug variables
public class App extends Application {
    // Application
    public static final int APPLICATION_DATA_VERSION = 8;
    public static final String VERSION_NAME = BuildConfig.VERSION_NAME;
    public static final int VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String APPLICATION_ID = BuildConfig.APPLICATION_ID;

    // Notifications
    public static final String NOTIFICATION_QUCIKNOTE_CHANNEL = QuickNoteReceiver.NOTIFICATION_CHANNEL;
    public static final String NOTIFICATION_ITEMS_CHANNEL = "items_notifications";
    private static final String NOTIFICATION_CRASH_CHANNEL = "crash_report";

    // DEBUG
    public static final boolean SHADOW_RELEASE = false;
    public static final boolean DEBUG = !SHADOW_RELEASE && BuildConfig.DEBUG;
    public static final boolean DEBUG_TICK_NOTIFICATION = (DEBUG & false);
    public static final int DEBUG_MAIN_ACTIVITY_START_SLEEP = (DEBUG & false) ? 6000 : 0;
    public static final int DEBUG_APP_START_SLEEP = (DEBUG & false) ? 8000 : 0;
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
    @AppInitIfNeed private UUID instanceId;
    private JSONObject versionData;
    private boolean appInForeground = false;
    @AppInitIfNeed private ItemManager itemManager = null;
    @AppInitIfNeed private SettingsManager settingsManager = null;
    @AppInitIfNeed private ColorHistoryManager colorHistoryManager = null;
    @AppInitIfNeed private Telemetry telemetry = null;
    @AppInitIfNeed private License[] openSourceLicenses = null;
    private final List<FeatureFlag> featureFlags = new ArrayList<>(App.DEBUG ? Arrays.asList(
            FeatureFlag.ITEM_DEBUG_TICK_COUNTER,
            FeatureFlag.ITEM_EDITOR_SHOW_COPY_ID_BUTTON,
            FeatureFlag.AVAILABLE_LOGS_OVERLAY,
            FeatureFlag.NONE,
            FeatureFlag.SHOW_APP_STARTUP_TIME_IN_PREMAIN_ACTIVITY,
            FeatureFlag.ALWAYS_SHOW_SAVE_STATUS,
            FeatureFlag.SHOW_MAINACTIVITY_STARTUP_TIME,
            FeatureFlag.AVAILABLE_UI_PERSONAL_TICK
    ) : Collections.emptyList());
    private long appStartupTime = 0;

    @Override
    public void onCreate() {
        long start = System.currentTimeMillis();
        try {
            super.onCreate();
            instance = this;
            setupCrashReporter();
            DebugUtil.sleep(DEBUG_APP_START_SLEEP);

            final DataFixer dataFixer = new DataFixer(this);
            final DataFixer.FixResult fixResult = dataFixer.fixToCurrentVersion();

            registryNotificationsChannels();

            if (!fixResult.isVersionFileExist()) updateVersionFile();
            if (fixResult.isFixed()) {
                getTelemetry().send(new Telemetry.DataFixerLogsLPacket(fixResult.getDataVersion(), fixResult.getLogs()));
            }
        } catch (Exception e) {
            crash(this, CrashReport.create(new RuntimeException(getClass().getName() + " onCreate exception: " + e, e)), false);
        }
        this.appStartupTime = System.currentTimeMillis() - start;
    }

    private void registryNotificationsChannels() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_QUCIKNOTE_CHANNEL, getString(R.string.notification_quickNote_title), NotificationManager.IMPORTANCE_HIGH));
        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_ITEMS_CHANNEL, getString(R.string.notification_items_title), NotificationManager.IMPORTANCE_HIGH));
    }

    /**
     * Parse instanceId from file or generate random
     * @return uuid
     */
    @NonNull
    private UUID parseInstanceId() {
        final File instanceIdFile = new File(getExternalFilesDir(""), "instanceId");
        UUID instanceId;
        if (FileUtil.isExist(instanceIdFile)) {
            try {
                instanceId = UUID.fromString(FileUtil.getText(instanceIdFile));
            } catch (Exception e) {
                throw new RuntimeException("Cannot get App instanceId!", e);
            }

        } else {
            instanceId = UUID.randomUUID();
            FileUtil.setText(instanceIdFile, instanceId.toString());
        }
        return instanceId;
    }

    private void updateVersionFile() {
        try {
            this.versionData = new JSONObject()
                    .put("product", "OpenToday")
                    .put("developer", "FazziCLAY ( https://fazziclay.github.io )")
                    .put("licence", "GNU GPLv3")
                    .put("data_version", APPLICATION_DATA_VERSION)
                    .put("application_version", VERSION_CODE)
                    .put("latest_start", System.currentTimeMillis());
            FileUtil.setText(new File(getExternalFilesDir(""), "version"), versionData.toString());
        } catch (Exception e) {
            throw new RuntimeException("Exception!", e);
        }
    }

    private License[] getOpenSourceLicences() {
        // TODO: 19.10.2022 add v prefix to version to telemetry
        return new License[]{
                new License("LICENSE_OpenToday", "OpenToday (this app)", "fazziclay@gmail.com\nhttps://fazziclay.github.io/opentoday"),
                new License("LICENSE_hsv-alpha-color-picker-android", "hsv-alpha-color-picker-android", "https://github.com/martin-stone/hsv-alpha-color-picker-android"),
                new License("LICENSE_JavaNeoUtil", "JavaNeoUtil v" + JavaNeoUtil.VERSION_NAME, "https://github.com/fazziclay/javaneoutil"),
                new License("LICENSE_NeoSocket", "NeoSocket v" + NeoSocket.VERSION_NAME, "https://github.com/fazziclay/neosocket"),
                new License("LICENSE_OpenTodayTelemetry", "OpenTodayTelemetry " + OpenTodayTelemetry.VERSION_NAME, getString(R.string.openSourceLicenses_telemetry_warn) + "\nhttps://github.com/fazziclay/opentodaytelemetry"),
        };
    }

    public boolean isFeatureFlag(FeatureFlag flag) {
        if (flag == null) return true;
        for (FeatureFlag f : this.getFeatureFlags()) {
            if (f == flag) {
                return true;
            }
        }
        return false;
    }

    private void setupCrashReporter() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> App.crash(App.this, CrashReport.create(throwable), true));
    }

    public static void exception(Context context, Exception exception) {
        App.crash(context, CrashReport.create(exception), false);
    }

    private static void crash(Context context, final CrashReport crashReport, boolean fatal) {
        crashReport.setFatal(CrashReport.FatalEnum.fromBoolean(fatal));
        if (context == null) context = App.get();

        // === File ===
        final File crashReportFile = new File(context.getExternalCacheDir(), "crash_report/" + crashReport.getID().toString());
        FileUtil.setText(crashReportFile, crashReport.convertToText());

        try {
            Log.e("OpenToday-Crash", "Crash saved to: " + crashReportFile.getAbsolutePath());
            Log.e("OpenToday-Crash", crashReport.convertToText(), crashReport.getThrowable());
        } catch (Exception ignored) {}

        if (fatal) {
            // === NOTIFICATION ===
            final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CRASH_CHANNEL, context.getString(R.string.notification_crash_title), NotificationManager.IMPORTANCE_DEFAULT));

            final int flag;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
            } else {
                flag = PendingIntent.FLAG_UPDATE_CURRENT;
            }
            notificationManager.notify(new Random().nextInt(), new NotificationCompat.Builder(context, App.NOTIFICATION_CRASH_CHANNEL)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setContentTitle(context.getString(R.string.crash_notification_title))
                    .setContentText(context.getString(R.string.crash_notification_text))
                    .setSubText(context.getString(R.string.crash_notification_subtext))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(context.getString(R.string.crash_notification_big_text))
                            .setBigContentTitle(context.getString(R.string.crash_notification_big_title))
                            .setSummaryText(context.getString(R.string.crash_notification_big_summary)))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(PendingIntent.getActivity(context, 0, CrashReportActivity.createLaunchIntent(context, crashReportFile.getAbsolutePath()), flag))
                    .setAutoCancel(true)
                    .build());

        }

        // Telemetry
        final App app = App.get(context);
        if (app != null && app.getTelemetry() != null) {
            app.getTelemetry().send(new Telemetry.CrashReportLPacket(crashReport));
        }

        try {
            Thread.sleep(250);
        } catch (Exception ignored) {}

        if (defaultHandler != null && fatal) {
            defaultHandler.uncaughtException(crashReport.getThread(), crashReport.getThrowable());
        }
    }

    private void preCheckOpenSourceLicenses() {
        if (openSourceLicenses == null) {
            openSourceLicenses = getOpenSourceLicences();
        }
    }

    private void preCheckTelemetry() {
        if (telemetry == null) {
            telemetry = new Telemetry(this);
        }
    }

    private void preCheckItemManager() {
        if (itemManager == null) {
            final File externalFiles = getExternalFilesDir("");
            itemManager = new ItemManager(new File(externalFiles, "item_data.json"), new File(externalFiles, "item_data.gz"));
            itemManager.setDebugPrintSaveStatusAlways(isFeatureFlag(FeatureFlag.ALWAYS_SHOW_SAVE_STATUS));
        }
    }

    private void preCheckSettingsManager() {
        if (settingsManager == null) {
            final File externalFiles = getExternalFilesDir("");
            settingsManager = new SettingsManager(new File(externalFiles, "settings.json"));
        }
    }

    private void preCheckColorHistoryManager() {
        if (colorHistoryManager == null) {
            final File externalFiles = getExternalFilesDir("");
            colorHistoryManager = new ColorHistoryManager(new File(externalFiles, "color_history.json"), 10);
        }
    }

    private void preCheckInstanceId() {
        if (instanceId == null) {
            instanceId = parseInstanceId();
        }
    }

    // getters & setters
    public JSONObject getVersionData() { return versionData; }
    public long getAppStartupTime() {return appStartupTime;}
    public UUID getInstanceId() {
        preCheckInstanceId();
        return instanceId;
    }

    public Telemetry getTelemetry() {
        preCheckTelemetry();
        return telemetry;
    }
    public ItemManager getItemManager() {
        preCheckItemManager();
        return itemManager;
    }
    public SettingsManager getSettingsManager() {
        preCheckSettingsManager();
        return this.settingsManager;
    }
    public ColorHistoryManager getColorHistoryManager() {
        preCheckColorHistoryManager();
        return colorHistoryManager;
    }
    public License[] getOpenSourcesLicenses() {
        preCheckOpenSourceLicenses();
        return this.openSourceLicenses;
    }
    public boolean isAppInForeground() { return appInForeground; }
    public void setAppInForeground(boolean appInForeground) { this.appInForeground = appInForeground; }
    public List<FeatureFlag> getFeatureFlags() {return featureFlags;}
    // not getters & setters :)
}
