package ru.fazziclay.opentoday.ui.activity;

import static android.view.View.GONE;
import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.Telemetry;
import ru.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import ru.fazziclay.opentoday.app.updatechecker.UpdateChecker;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.databinding.ActivityMainBinding;
import ru.fazziclay.opentoday.ui.UITickService;
import ru.fazziclay.opentoday.ui.fragment.MainRootFragment;
import ru.fazziclay.opentoday.ui.interfaces.ContainBackStack;
import ru.fazziclay.opentoday.util.L;
import ru.fazziclay.opentoday.util.NetworkUtil;
import ru.fazziclay.opentoday.util.OnDebugLog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CONTAINER_ID = R.id.content_root;

    private ActivityMainBinding binding;
    private App app;
    private UITickService uiTickService;
    private long lastExitClick = 0;
    private final OnDebugLog onDebugLog = new LocalOnDebugLog();

    // Current Date
    private Handler currentDateHandler;
    private Runnable currentDateRunnable;
    private GregorianCalendar currentDateCalendar;


    // Activity overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {}

        this.app = App.get(this);
        this.app.setAppInForeground(true);
        this.app.getTelemetry().send(new Telemetry.UiOpenLPacket());
        this.uiTickService = new UITickService(this);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());

        if (!App.DEBUG) binding.debugs.setVisibility(GONE);
        L.getCallbackStorage().addCallback(CallbackImportance.DEFAULT, onDebugLog);

        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(CONTAINER_ID, MainRootFragment.create(), "MainRootFragment")
                    .commit();
        }

        binding.debugApp.setVisibility(App.DEBUG ? View.VISIBLE : View.GONE);
        setupUpdateAvailableNotify();
        setupCurrentDate();

        if (app.getSettingsManager().isQuickNoteNotification()) {
            QuickNoteReceiver.sendQuickNoteNotification(this);
        }
        uiTickService.create();
        uiTickService.tick();
    }

    @Override
    public void onBackPressed() {
        Runnable exit = super::onBackPressed;
        Runnable def = () -> {
            if (System.currentTimeMillis() - lastExitClick > 1000) {
                Toast.makeText(this, R.string.exit_tab_2_count, Toast.LENGTH_SHORT).show();
                lastExitClick = System.currentTimeMillis();
            } else {
                exit.run();
            }
        };

        Fragment fragment = getMainRootFragment();
        if (fragment instanceof ContainBackStack) {
            ContainBackStack d = (ContainBackStack) fragment;
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
        if (app != null) {
            app.setAppInForeground(false);
            app.getTelemetry().send(new Telemetry.UiClosedLPacket());
        }
        if (uiTickService != null) {
            uiTickService.destroy();
        }
        currentDateHandler.removeCallbacks(currentDateRunnable);
        L.getCallbackStorage().deleteCallback(onDebugLog);
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
            binding.updateAvailable.setVisibility(available ? View.VISIBLE : GONE);
            if (url != null) {
                binding.updateAvailable.setOnClickListener(v -> NetworkUtil.openBrowser(MainActivity.this, url));
            }
        }));
    }

    public void toggleDebugOverLogs() {
        binding.debugs.setVisibility(binding.debugs.getVisibility() == View.VISIBLE ? GONE : View.VISIBLE);
    }

    private class LocalOnDebugLog implements OnDebugLog {
        @Override
        public void run(String text) {
            if (isDestroyed() || binding == null || !App.DEBUG) {
                return;
            }
            runOnUiThread(() -> binding.debugs.setText(text));
        }
    }
}