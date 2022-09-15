package ru.fazziclay.opentoday.ui.activity;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private ItemsEditor itemsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugUtil.sleep(App.DEBUG_MAIN_ACTIVITY_START_SLEEP);
        if (!(App.DEBUG_MAIN_ACTIVITY == this.getClass() || App.DEBUG_MAIN_ACTIVITY == null)) {
            startActivity(new Intent(this, App.DEBUG_MAIN_ACTIVITY));
            finish();
        }
        if (App.DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY) {
            throw new RuntimeException("This is cute Runtime Exception :)", new RuntimeException("DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY is enabled :)"));
        }

        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {}

        // logic
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        app = App.get(this);
        app.setAppInForeground(true);

        ItemManager itemManager = app.getItemManager();
        itemsEditor = new ItemsEditor(this, binding.itemsEditor, itemManager, itemManager, "/");
        itemsEditor.create();
        binding.itemsEditor.addView(itemsEditor.getView());

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
        itemsEditor.destroy();
        app.setAppInForeground(false);
        stopService(new Intent(this, UITickService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBatteryOptimizationNotify();
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
}