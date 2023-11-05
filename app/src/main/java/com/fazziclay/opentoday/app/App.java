package com.fazziclay.opentoday.app;

import static com.fazziclay.opentoday.util.InlineUtil.IPROF;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.javaneoutil.JavaNeoUtil;
import com.fazziclay.javaneoutil.NonNull;
import com.fazziclay.neosocket.NeoSocket;
import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.api.PluginManager;
import com.fazziclay.opentoday.app.datafixer.DataFixer;
import com.fazziclay.opentoday.app.datafixer.FixResult;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.QuickNoteReceiver;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.app.items.tick.TickThread;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.gui.activity.CrashReportActivity;
import com.fazziclay.opentoday.plugins.PluginsRegistry;
import com.fazziclay.opentoday.util.DebugUtil;
import com.fazziclay.opentoday.util.License;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.profiler.Profiler;
import com.fazziclay.opentoday.util.profiler.ProfilerImpl;
import com.fazziclay.opentoday.util.time.TimeUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import ru.fazziclay.opentoday.telemetry.OpenTodayTelemetry;

public class App extends Application {
    // Application
    public static final int APPLICATION_DATA_VERSION = 12;
    public static final String VERSION_NAME = CustomBuildConfig.VERSION_NAME;
    public static final int VERSION_CODE = CustomBuildConfig.VERSION_CODE;
    public static final long VERSION_RELEASE_TIME = CustomBuildConfig.VERSION_RELEASE_TIME;
    public static final String VERSION_BRANCH = CustomBuildConfig.VERSION_BRANCH;
    public static final String APPLICATION_ID = CustomBuildConfig.APPLICATION_ID;

    // Notifications
    public static final String NOTIFICATION_QUCIKNOTE_CHANNEL = QuickNoteReceiver.NOTIFICATION_CHANNEL;
    public static final String NOTIFICATION_ITEMS_CHANNEL = "items_notifications";
    private static final String NOTIFICATION_CRASH_CHANNEL = "crash_report";

    // Shared preference
    public static final String SHARED_NAME = "main";
    public static final String SHARED_KEY_PINCODE = "app_pinCode";
    public static final String SHARED_KEY_LAST_TAB = "app_tabInclude_lastTabId";

    // DEBUG
    public static final boolean DEBUG = com.fazziclay.opentoday.Build.isDebug();
    public static final boolean LOG = com.fazziclay.opentoday.Build.isLogs();
    public static final boolean LOGS_SAVE = com.fazziclay.opentoday.Build.isLogsSave();
    public static final boolean SECRET_SETTINGS_AVAILABLE = com.fazziclay.opentoday.Build.isSecretSettingAvailable();

    public static boolean debug(boolean b) {
        return (DEBUG && b);
    }

    // Crash-report exception handler
    private static Thread.UncaughtExceptionHandler androidUncaughtHandler;
    private static Thread.UncaughtExceptionHandler appUncaughtHandler;

    // Instance
    private static volatile App instance = null;

    public static App get(@NotNull Context context) {
        return (App) context.getApplicationContext();
    }

    public static App get() {
        return instance;
    }

    // Application
    private long startTime;
    private JSONObject versionData;
    private File logsFile;
    private final StringBuilder logs = new StringBuilder();
    private final OptionalField<UUID> instanceId = new OptionalField<>(this::parseInstanceId);
    private final OptionalField<License[]> openSourceLicenses = new OptionalField<>(this::createOpenSourceLicensesArray);
    private final OptionalField<DataFixer> dataFixer = new OptionalField<>(() -> new DataFixer(this));
    private final OptionalField<TabsManager> tabsManager = new OptionalField<>(this::preCheckTabsManager, TabsManager::destroy);
    private final OptionalField<SettingsManager> settingsManager = new OptionalField<>(() -> new SettingsManager(new File(getExternalFilesDir(""), "settings.json")));
    private final OptionalField<ColorHistoryManager> colorHistoryManager = new OptionalField<>(() -> new ColorHistoryManager(new File(getExternalFilesDir(""), "color_history.json"), 10));
    private final OptionalField<PinCodeManager> pinCodeManager = new OptionalField<>(() -> new PinCodeManager(this));
    private final OptionalField<SelectionManager> selectionManager = new OptionalField<>(SelectionManager::new);
    private final OptionalField<Telemetry> telemetry = new OptionalField<>(() -> new Telemetry(this, SettingsManager.IS_TELEMETRY.get(getSettingsManager())));
    private final OptionalField<TickThread> tickThread = new OptionalField<>(this::preCheckTickThread, TickThread::requestTerminate, this::validateTickThread);
    private final OptionalField<Translation> translation = new OptionalField<>(() -> new TranslationImpl(this::getString));
    private final OptionalField<CallbackStorage<ImportantDebugCallback>> importantDebugCallbacks = new OptionalField<>(CallbackStorage::new);
    private final OptionalField<BeautifyColorManager> beautifyColorManager = new OptionalField<>(() -> new BeautifyColorManager(this));
    private final OptionalField<ItemNotificationHandler> itemNotificationHandler = new OptionalField<>(() -> new ItemNotificationHandler(this, this));
    private final List<FeatureFlag> featureFlags = new ArrayList<>(Arrays.asList(com.fazziclay.opentoday.Build.INITIAL_FEATURE_FLAGS));
    private long appStartupTime = 0;
    private boolean pluginsInitialized = false;

    /**
     * OPENTODAY APPLICATION INITIALIZE
     * <p>1. Setup {@link #instance} variable to this object</p>
     * <p>2. Setup UncaughtExceptionHandler to CrashReport</p>
     * <p>2.5 init logFile variable for logs</p>
     * <p>3. {@link DataFixer} run</p>
     * <p>4. (Android) Registry notification channels</p>
     * <p>5. {@link #updateVersionFile()} if {@link FixResult} return true and send Telemetry signal about datafixer work</p>
     */
    @Override
    public void onCreate() {
        startTime = System.currentTimeMillis();
        try {
            super.onCreate();
            instance = this;
            setupCrashReporter();
            DebugUtil.sleep(Debug.DEBUG_APP_START_SLEEP);
            CrashReportContext.BACK.push("App onCreate");
            if (DEBUG) {
                StrictMode.enableDefaults();
            }

            logsFile = new File(getExternalCacheDir(), "latest.log");
            final FixResult fixResult = Logger.dur("App", "[DataFixer] fixToCurrentVersion", () -> getDataFixer().fixToCurrentVersion());

            registryNotificationsChannels();

            if (fixResult.isVersionFileUpdateRequired()) updateVersionFile();
            if (fixResult.isFixed()) {
                getTelemetry().send(new Telemetry.DataFixerLogsLPacket(fixResult.getDataVersion(), fixResult.getLogs()));
            }
            Logger.i("App", "== onCreate successfully ==");
        } catch (Exception e) {
            crash(this, CrashReport.create(new RuntimeException(getClass().getName() + " onCreate exception: " + e, e)), false);
        }
        this.appStartupTime = Debug.appStartupTime = System.currentTimeMillis() - startTime;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Logger.i("App", "onLowMemory.");
        openSourceLicenses.free();
        dataFixer.free();
        pinCodeManager.free();

        Debug.free();
        TimeUtil.free();
        RandomUtil.free();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.i("App", "onTerminate.");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Logger.i("App", "onTrimMemory. level="+level);

        if (level >= TRIM_MEMORY_RUNNING_LOW) {
            openSourceLicenses.free();
            dataFixer.free();
            pinCodeManager.free();
            Debug.free();
        }
    }

    public static Profiler createProfiler(@NotNull String name) {
        return com.fazziclay.opentoday.Build.isProfilersEnabled() ? addProfiler(new ProfilerImpl(name)) : Profiler.EMPTY;
    }

    public static ProfilerImpl addProfiler(ProfilerImpl profiler) {
        if (ProfilerImpl.PROFILERS == null) {
            ProfilerImpl.PROFILERS = new ArrayList<>();
        }
        ProfilerImpl.PROFILERS.add(profiler);
        return profiler;
    }

    public List<Profiler> getProfilers() {
        return ProfilerImpl.PROFILERS;
    }

    public boolean isPinCodeNeed() {
        return this.getPinCodeManager().isPinCodeSet();
    }

    public boolean isPinCodeTooLong() {
        return getPinCodeManager().getPinCode().length() > PinCodeManager.MAX_LENGTH;
    }

    public boolean isPinCodeAllow(String p) {
        if (getPinCodeManager().getPinCode().length() > PinCodeManager.MAX_LENGTH) {
            if (p.length() >= PinCodeManager.MAX_LENGTH) {
                return this.getPinCodeManager().getPinCode().startsWith(p);
            }
        }
        return p.equals(this.getPinCodeManager().getPinCode());
    }

    public int getPinCodeLength() {
        return Math.min(getPinCodeManager().getPinCode().length(), PinCodeManager.MAX_LENGTH);
    }

    private void registryNotificationsChannels() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_QUCIKNOTE_CHANNEL, getString(R.string.notificationChannel_quickNote_title), NotificationManager.IMPORTANCE_HIGH));
        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_ITEMS_CHANNEL, getString(R.string.notificationChannel_items_title), NotificationManager.IMPORTANCE_MAX));
    }

    public void tryInitPlugins() {
        IPROF.push("reinitPlugins");
        CrashReportContext.BACK.push("App::reinitPlugins");
        if (!pluginsInitialized) {
            CrashReportContext.BACK.push("disabling all...");
            PluginManager.disableAllPlugins();
            for (final String shortName : getSettingsManager().getPlugins().split(",")) {
                CrashReportContext.BACK.swap("plugin: " + shortName.trim());

                PluginsRegistry.PluginInfo pluginInfo = PluginsRegistry.REGISTRY.getByShortName(shortName);
                try {
                    initPlugin(pluginInfo, 0);

                } catch (Exception e) {
                    Logger.e("App", "failed init plugin: " + shortName, e);
                }
            }
            pluginsInitialized = true;
            CrashReportContext.BACK.pop();
        }
        CrashReportContext.BACK.pop();
        IPROF.pop();
    }

    public void initPlugin(PluginsRegistry.PluginInfo pluginInfo, int i) throws Exception {
        if (pluginInfo == null) {
            throw new NullPointerException("pluginInfo is null");
        }

        if (PluginManager.isPluginActive(pluginInfo.getPackageId())) {
            Logger.w("App:initPlugins", "plugin " + pluginInfo.getShortName() + " already active...");
            return;
        }

        if (i > 10) {
            Logger.w("App:initPlugins", "i > 10...");
            return;
        }

        for (final String depend : pluginInfo.getDepends()) {
            initPlugin(PluginsRegistry.REGISTRY.getByShortName(depend), i + 1);
        }

        PluginManager.loadPlugin(
                pluginInfo.getPackageId(),
                pluginInfo.getClazz().getConstructor().newInstance());
    }

    public void initPlugins() {
        pluginsInitialized = false;
        tryInitPlugins();
    }

    /**
     * Parse instanceId from file or generate random
     * @return uuid
     */
    @NonNull
    private UUID parseInstanceId() {
        final File instanceIdFile = new File(getExternalFilesDir(""), "instanceId");
        final UUID instanceId;
        if (FileUtil.isExist(instanceIdFile)) {
            try {
                instanceId = UUID.fromString(FileUtil.getText(instanceIdFile));
            } catch (Exception e) {
                FileUtil.setText(instanceIdFile, UUID.randomUUID().toString());
                throw new RuntimeException("Cannot get App instanceId! (rewritten before crash.)", e);
            }

        } else {
            instanceId = UUID.randomUUID();
            FileUtil.setText(instanceIdFile, instanceId.toString());
        }
        return instanceId;
    }

    public JSONObject generateVersionDataMinimal() {
        try {
            return new JSONObject()
                    .put("product", "OpenToday")
                    .put("developer", "FazziCLAY ( https://fazziclay.github.io )")
                    .put("licence", "GNU GPLv3")
                    .put("data_version", APPLICATION_DATA_VERSION)
                    .put("application_version", VERSION_CODE)
                    .put("application_versionName", VERSION_NAME)
                    .put("application_releaseTime", VERSION_RELEASE_TIME);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject versionDataPutLatestStart(JSONObject j) {
        try {
            j.put("latest_start", startTime);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return j;
    }

    private void updateVersionFile() {
        try {
            this.versionData = versionDataPutLatestStart(generateVersionDataMinimal());
            FileUtil.setText(new File(getExternalFilesDir(""), "version"), versionData.toString());
        } catch (Exception e) {
            throw new RuntimeException("Exception!", e);
        }
    }

    private License[] createOpenSourceLicensesArray() {
        // TODO: 19.10.2022 add v prefix to version to telemetry
        return new License[]{
                new License("licenses/LICENSE_OpenToday", "OpenToday (this app)", "fazziclay@gmail.com\nhttps://fazziclay.github.io/opentoday"),
                new License("licenses/LICENSE_hsv-alpha-color-picker-android", "hsv-alpha-color-picker-android", "https://github.com/martin-stone/hsv-alpha-color-picker-android"),
                new License("licenses/LICENSE_JavaNeoUtil", "JavaNeoUtil v" + JavaNeoUtil.VERSION_NAME, "https://github.com/fazziclay/javaneoutil"),
                new License("licenses/LICENSE_NeoSocket", "NeoSocket v" + NeoSocket.VERSION_NAME, "https://github.com/fazziclay/neosocket"),
                new License("licenses/LICENSE_OpenTodayTelemetry", "OpenTodayTelemetry " + OpenTodayTelemetry.VERSION_NAME, getString(R.string.openSourceLicenses_telemetry_warn) + "\nhttps://github.com/fazziclay/opentodaytelemetry"),
                new License("licenses/LICENSE_simple-analog-clock", "simple-analog-clock", "Analog clock\nhttps://github.com/leondzn/simple-analog-clock")
        };
    }

    public boolean isFeatureFlag(final FeatureFlag flag) {
        return this.getFeatureFlags().contains(flag);
    }

    private void setupCrashReporter() {
        androidUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler();
        appUncaughtHandler = (thread, throwable) -> App.crash(this, CrashReport.create(thread, throwable), true);
        Thread.setDefaultUncaughtExceptionHandler(appUncaughtHandler);
    }

    public static void exception(Context context, Exception exception) {
        App.crash(context, CrashReport.create(exception), false);
    }

    public static void crash(@Nullable Context context, @NotNull final CrashReport crashReport, boolean fatal) {
        if (context == null) context = App.get();
        crashReport.setFatal(CrashReport.FatalEnum.fromBoolean(fatal));

        // === File ===
        final File crashReportFile = new File(context.getExternalCacheDir(), "crash_report/" + crashReport.getID().toString());
        FileUtil.setText(crashReportFile, crashReport.convertToText());

        // === Android.Log ===
        try {
            final String CRASH_TAG = "OpenToday-Crash";
            Log.e(CRASH_TAG, "=== Crash " + crashReport.getID() + " === (Start)");
            Log.e(CRASH_TAG, "Crash saved to: " + crashReportFile.getAbsolutePath());
            Log.e(CRASH_TAG, crashReport.convertToText(), crashReport.getThrowable());
            Log.e(CRASH_TAG, "=== Crash " + crashReport.getID() + " === (End)");

            Logger.e("=CRASH=", crashReport.convertToText(), crashReport.getThrowable());
        } catch (Exception ignored) {}

        // === If fatal: notify user ===
        if (fatal || App.DEBUG) {
            sendCrashNotification(context, crashReportFile, crashReport);
        }

        // Telemetry
        final App app = App.get(context);
        final Telemetry telemetry = app.getTelemetry();
        if (app != null && telemetry != null) {
            telemetry.send(new Telemetry.CrashReportLPacket(crashReport));
        }

        // === If fatal: crash app. ===
        if (fatal && androidUncaughtHandler != null) {
            try {
                Thread.sleep(250);
            } catch (Exception ignore) {}
            androidUncaughtHandler.uncaughtException(crashReport.getThread(), crashReport.getThrowable());
        }

        if (!fatal) ImportantDebugCallback.pushStatic("App.crash() no fatal.\nException: " + crashReport.getThrowable() + "\n\ntext:\n"+crashReport.convertToText());
    }

    private static void sendCrashNotification(final Context context, File fileToCrash, CrashReport crashReport) {
        // === NOTIFICATION ===
        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CRASH_CHANNEL, context.getString(R.string.notificationChannel_crash_title), NotificationManager.IMPORTANCE_DEFAULT));

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
                .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(), CrashReportActivity.createLaunchIntent(context, fileToCrash.getAbsolutePath(), crashReport.getID(), crashReport.getThrowable().toString()), flag))
                .setAutoCancel(true)
                .build());

    }



    // getters & setters
    public JSONObject getVersionData() {
        if (versionData == null) {
            versionData = generateVersionDataMinimal();
        }
        return versionData;
    }
    public long getAppStartupTime() {
        return appStartupTime;
    }

    @NotNull
    public License[] getOpenSourcesLicenses() {
        return openSourceLicenses.get();
    }

    @NotNull
    public UUID getInstanceId() {
        return instanceId.get();
    }

    @NotNull
    public Telemetry getTelemetry() {
        return telemetry.get();
    }

    private TabsManager preCheckTabsManager() {
        CrashReportContext.BACK.push("App.preCheckTabsManager");
        final File externalFiles = getExternalFilesDir("");
        final File internalFiles = getFilesDir();
        TabsManager tabsManager = new TabsManager(
                new File(externalFiles, "item_data.json"),
                new File(externalFiles, "item_data.gz"),
                new File(internalFiles, "item_data.gz.bak"),
                getTranslation());
        tabsManager.setDebugPrintSaveStatusAlways(isFeatureFlag(FeatureFlag.ALWAYS_SHOW_SAVE_STATUS));
        CrashReportContext.BACK.pop();
        return tabsManager;
    }

    @NotNull
    public TabsManager getTabsManager() {
        return tabsManager.get();
    }

    @NotNull
    public Translation getTranslation() {
        return translation.get();
    }

    @NotNull
    public SettingsManager getSettingsManager() {
        return settingsManager.get();
    }

    @NotNull
    public ColorHistoryManager getColorHistoryManager() {
        return colorHistoryManager.get();
    }

    @NotNull
    public PinCodeManager getPinCodeManager() {
        return pinCodeManager.get();
    }

    @NotNull
    public SelectionManager getSelectionManager() {
        return selectionManager.get();
    }

    private TickThread preCheckTickThread() {
        TickThread tickThread = new TickThread(getApplicationContext(), getTabsManager());
        tickThread.start();
        return tickThread;
    }

    private TickThread validateTickThread(TickThread t) {
        if (t.isEnabled()) {
            return t;
        }
        return preCheckTickThread();
    }

    @NotNull
    public TickThread getTickThread() {
        return tickThread.get();
    }

    @NotNull
    public DataFixer getDataFixer() {
        return dataFixer.get();
    }

    @NotNull
    public List<FeatureFlag> getFeatureFlags() {
        return featureFlags;
    }

    @NotNull
    public File getLogsFile() {
        return logsFile;
    }

    public CallbackStorage<ImportantDebugCallback> getImportantDebugCallbacks() {
        return importantDebugCallbacks.get();
    }
    public ItemsRoot getItemsRoot() {
        return getTabsManager();
    }

    public BeautifyColorManager getBeautifyColorManager() {
        return beautifyColorManager.get();
    }

    public StringBuilder getLogs() {
        return logs;
    }

    public ItemNotificationHandler getItemNotificationHandler() {
        return itemNotificationHandler.get();
    }
}
