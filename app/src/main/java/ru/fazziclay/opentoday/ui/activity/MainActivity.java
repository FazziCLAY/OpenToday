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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.databinding.ActivityMainBinding;
import ru.fazziclay.opentoday.ui.dialog.DialogItem;
import ru.fazziclay.opentoday.ui.dialog.DialogSelectItemType;
import ru.fazziclay.opentoday.ui.other.AppToolbar;
import ru.fazziclay.opentoday.ui.other.item.ItemStorageDrawer;
import ru.fazziclay.opentoday.util.DebugUtil;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding; // binding
    private App app;
    private AppToolbar appToolbar;
    private ItemStorageDrawer itemStorageDrawer;

    private int lastDayOfYear = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugUtil.sleep(App.MAIN_ACTIVITY_START_SLEEP);
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {}
        appToolbar = new AppToolbar(this);

        // logic
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        app = App.get(this);

        itemStorageDrawer = new ItemStorageDrawer(this, app.getItemManager(), app.getItemManager());
        itemStorageDrawer.create();
        binding.mainItems.addView(itemStorageDrawer.getView());

        setupCurrentDate();

        // Notifications
        setupBatteryOptimizationNotify();
        setupUpdateAvailableNotify();

        // toolbar
        setupToolbar();
    }

    private void setupToolbar() {
        binding.toolbar.addView(appToolbar.getToolbarView());
        binding.toolbarMore.addView(appToolbar.getToolbarMoreView());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        itemStorageDrawer.destroy();
    }

    private void setupCurrentDate() {
        setCurrentDate();
        lastDayOfYear = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
        Handler handler = new Handler(getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed()) return;
                final int current = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
                if (current != lastDayOfYear) {
                    lastDayOfYear = current;
                    setCurrentDate();
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
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
        App.get(this).getUpdateChecker().check((available, url) -> runOnUiThread(() -> {
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

    @Override
    protected void onResume() {
        super.onResume();
        setupBatteryOptimizationNotify();
    }

    public void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE", Locale.getDefault());
        binding.currentDate.setText(dateFormat.format(new GregorianCalendar().getTime()));
    }
}