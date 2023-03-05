package com.fazziclay.opentoday.gui.activity;

import static com.fazziclay.opentoday.util.InlineUtil.nullStat;
import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.FeatureFlag;
import com.fazziclay.opentoday.app.Telemetry;
import com.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.app.updatechecker.UpdateChecker;
import com.fazziclay.opentoday.databinding.ActivityMainBinding;
import com.fazziclay.opentoday.databinding.NotificationDebugappBinding;
import com.fazziclay.opentoday.databinding.NotificationUpdateAvailableBinding;
import com.fazziclay.opentoday.gui.UITickService;
import com.fazziclay.opentoday.gui.fragment.MainRootFragment;
import com.fazziclay.opentoday.gui.interfaces.BackStackMember;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.NetworkUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CONTAINER_ID = R.id.mainActivity_rootFragmentContainer;

    private ActivityMainBinding binding;
    private App app;
    private SettingsManager settingsManager;
    private UITickService uiTickService;
    private long lastExitClick = 0;

    // Current Date
    private Handler currentDateHandler;
    private Runnable currentDateRunnable;
    private GregorianCalendar currentDateCalendar;


    // Activity overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate", nullStat(savedInstanceState));
        long ping_start = System.currentTimeMillis();
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {}

        this.app = App.get(this);
        this.settingsManager = app.getSettingsManager();
        AppCompatDelegate.setDefaultNightMode(settingsManager.getTheme());
        this.app.setAppInForeground(true);
        this.app.getTelemetry().send(new Telemetry.UiOpenLPacket());
        this.uiTickService = new UITickService(this);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());

        long ping_startStat_apptelemetrythemebinging = System.currentTimeMillis();
        setContentView(binding.getRoot());
        long ping_startStat_setviewanddebug = System.currentTimeMillis();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(CONTAINER_ID, MainRootFragment.create(), "MainRootFragment")
                    .commit();
        }
        long ping_startStat_setfragment = System.currentTimeMillis();


        setupAppDebugNotify();
        setupUpdateAvailableNotify();
        setupCurrentDate();
        long ping_startStat_setups = System.currentTimeMillis();


        if (settingsManager.isQuickNoteNotification()) {
            QuickNoteReceiver.sendQuickNoteNotification(this);
        }
        if (!app.isFeatureFlag(FeatureFlag.DISABLE_AUTOMATIC_TICK)) {
            uiTickService.create();
            uiTickService.tick();
        }
        long startStat_preStop = System.currentTimeMillis();

        if (app.isFeatureFlag(FeatureFlag.SHOW_MAINACTIVITY_STARTUP_TIME)) {
            long c = System.currentTimeMillis();

            long startupTime = c - ping_start;
            StringBuilder text = new StringBuilder("MainActivity startup time:\n").append(startupTime).append("ms");
            text.append("\n");
            text.append("App;telemetry;theme;binging: ").append(ping_startStat_apptelemetrythemebinging - ping_start).append("ms\n");
            text.append("setContentView&debugs: ").append(ping_startStat_setviewanddebug - ping_startStat_apptelemetrythemebinging).append("ms\n");
            text.append("Set fragment: ").append(ping_startStat_setfragment - ping_startStat_setviewanddebug).append("ms\n");
            text.append("setups: ").append(ping_startStat_setups - ping_startStat_setfragment).append("ms\n");
            text.append("preStop: ").append(startStat_preStop - ping_startStat_setups).append("ms\n");
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Runnable exit = super::onBackPressed;
        Runnable def = () -> {
            if (System.currentTimeMillis() - lastExitClick > 2000) {
                Toast.makeText(this, R.string.exit_tab_2_count, Toast.LENGTH_SHORT).show();
                lastExitClick = System.currentTimeMillis();
            } else {
                exit.run();
            }
        };

        Fragment fragment = getMainRootFragment();
        if (fragment instanceof BackStackMember) {
            BackStackMember d = (BackStackMember) fragment;
            if (!d.popBackStack()) {
                def.run();
            }
        } else {
            def.run();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy");
        if (app != null) {
            app.setAppInForeground(false);
            app.getTelemetry().send(new Telemetry.UiClosedLPacket());
        }
        if (uiTickService != null) {
            uiTickService.destroy();
        }
        currentDateHandler.removeCallbacks(currentDateRunnable);
    }

    private Fragment getMainRootFragment() {
        return getSupportFragmentManager().findFragmentById(CONTAINER_ID);
    }

    // Current Date
    private void setupCurrentDate() {
        currentDateCalendar = new GregorianCalendar();
        setCurrentDate();
        currentDateHandler = new Handler(getMainLooper());
        currentDateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed()) return;
                setCurrentDate();
                long millis = System.currentTimeMillis() % 1000;
                currentDateHandler.postDelayed(this, 1000 - millis);
            }
        };
        currentDateHandler.post(currentDateRunnable);
        viewClick(binding.currentDate, () -> {
            DatePickerDialog dialog = new DatePickerDialog(this);
            dialog.getDatePicker().setFirstDayOfWeek(settingsManager.getFirstDayOfWeek());
            dialog.show();
        });
    }

    private void setCurrentDate() {
        currentDateCalendar.setTimeInMillis(System.currentTimeMillis());
        Date time = currentDateCalendar.getTime();

        // TODO: 11.10.2022 IDEA: Pattern to settings
        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE", Locale.getDefault());
        binding.currentDateDate.setText(dateFormat.format(time));

        // Time
        dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        binding.currentDateTime.setText(dateFormat.format(time));
    }

    // Update checker
    private void setupUpdateAvailableNotify() {
        UpdateChecker.check(app, (available, url) -> runOnUiThread(() -> {
            if (available) {
                NotificationUpdateAvailableBinding updateAvailable = NotificationUpdateAvailableBinding.inflate(getLayoutInflater());
                binding.notifications.addView(updateAvailable.getRoot());

                viewVisible(updateAvailable.getRoot(), available, View.GONE);
                if (url != null) {
                    viewClick(updateAvailable.getRoot(), () -> NetworkUtil.openBrowser(MainActivity.this, url));
                }
            }
        }));
    }

    // App is DEBUG warning notify
    private void setupAppDebugNotify() {
        if (App.DEBUG) {
            NotificationDebugappBinding b = NotificationDebugappBinding.inflate(getLayoutInflater());
            binding.notifications.addView(b.getRoot());
        }
    }

    public void toggleLogsOverlay() {
        // TODO: 3/2/23 delete
    }
}