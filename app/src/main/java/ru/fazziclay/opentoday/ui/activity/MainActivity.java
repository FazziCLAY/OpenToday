package ru.fazziclay.opentoday.ui.activity;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import ru.fazziclay.opentoday.app.receiver.service.UITickService;
import ru.fazziclay.opentoday.app.updatechecker.UpdateChecker;
import ru.fazziclay.opentoday.databinding.ActivityMainBinding;
import ru.fazziclay.opentoday.ui.other.ItemsEditor;
import ru.fazziclay.opentoday.util.DebugUtil;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding; // binding
    private App app;
    private Handler currentDateHandler;
    private Runnable currentDateRunnable;
    private ItemsEditor itemsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugUtil.sleep(App.MAIN_ACTIVITY_START_SLEEP);
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {}

        // logic
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        app = App.get(this);
        app.setAppInForeground(true);

        ItemManager itemManager = app.getItemManager();
        itemsEditor = new ItemsEditor(this, binding.itemsEditor, itemManager, itemManager);
        itemsEditor.create();
        binding.itemsEditor.addView(itemsEditor.getView());

        setupCurrentDate();

        // Notifications
        setupBatteryOptimizationNotify();
        setupUpdateAvailableNotify();

        startService(new Intent(this, UITickService.class));

        sendQuickNoteNotify();
    }

    private void sendQuickNoteNotify() {
        QuickNoteReceiver.sendQuickNoteNotification(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentDateHandler.removeCallbacks(currentDateRunnable);
        itemsEditor.destroy();
        app.setAppInForeground(false);
        stopService(new Intent(this, UITickService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentDateHandler.removeCallbacks(currentDateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentDateHandler.post(currentDateRunnable);
        setupBatteryOptimizationNotify();
    }

    private void setupCurrentDate() {
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

    private void setupBatteryOptimizationNotify() {
        PowerManager powerManager = getSystemService(PowerManager.class);
        boolean show = !powerManager.isIgnoringBatteryOptimizations(getPackageName());
        binding.disableBatteryOptimizationWarning.setVisibility(show ? View.VISIBLE : View.GONE);
        fcu_viewOnClick(binding.disableBatteryOptimizationWarning, this::showBatteryOptimizationDialog);
    }

    @SuppressLint("BatteryLife")
    private void showBatteryOptimizationDialog() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void setupUpdateAvailableNotify() {
        UpdateChecker.check(app, (available, url) -> runOnUiThread(() -> {
            binding.updateAvailable.setVisibility(available ? View.VISIBLE : View.GONE);
            if (url != null) {
                binding.updateAvailable.setOnClickListener(v -> {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, R.string.update_available_error_browserNotFound, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }));
    }

    private void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE HH:mm:ss", Locale.getDefault());
        binding.currentDate.setText(dateFormat.format(new GregorianCalendar().getTime()));
    }
}