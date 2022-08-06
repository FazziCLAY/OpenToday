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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.databinding.ActivityMainBinding;
import ru.fazziclay.opentoday.ui.dialog.DialogAboutApp;
import ru.fazziclay.opentoday.ui.dialog.DialogItem;
import ru.fazziclay.opentoday.ui.dialog.DialogSelectItemType;
import ru.fazziclay.opentoday.ui.other.item.ItemUIDrawer;
import ru.fazziclay.opentoday.util.DebugUtil;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding; // binding
    private App app;
    private int lastDayOfYear = 0;

    private ItemUIDrawer itemUIDrawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugUtil.sleep(App.MAIN_ACTIVITY_START_SLEEP);

        // logic
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        app = App.get(this);

        itemUIDrawer = new ItemUIDrawer(this, app.getItemManager(), app.getItemManager());
        itemUIDrawer.create();
        binding.items.addView(itemUIDrawer.getView());

        // Current date
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

        // Add new
        fcu_viewOnClick(binding.addNew, () -> showAddNewDialog(App.get(this).getItemManager()));

        // Battery optimization
        setupBatteryOptimizationDialog();
        fcu_viewOnClick(binding.disableBatteryOptimizationWarning, this::showBatteryOptimizationDialog);

        // Update available
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

    private void setupBatteryOptimizationDialog() {
        PowerManager powerManager = getSystemService(PowerManager.class);
        boolean show = !powerManager.isIgnoringBatteryOptimizations(getPackageName());
        binding.disableBatteryOptimizationWarning.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @SuppressLint("BatteryLife")
    private void showBatteryOptimizationDialog() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBatteryOptimizationDialog();
    }

    // ======== START - MENU ========
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.about) {
            new DialogAboutApp(this).show();
        } else if (item.getItemId() == R.id.settings) {
            startActivity(SettingsActivity.createLaunchIntent(this));
        }
        return super.onOptionsItemSelected(item);
    }
    // ======== END - MENU ========


    @Override
    protected void onDestroy() {
        super.onDestroy();
        itemUIDrawer.destroy();
    }

    private void showAddNewDialog(ItemStorage itemStorage) {
        new DialogSelectItemType(this, R.string.selectItemTypeDialog_create)
                .setOnSelected((itemType) -> {
                    DialogItem dialogItem = new DialogItem(this, app.getItemManager());
                    dialogItem.create(itemType, itemStorage::addItem);
                })
                .show();
    }

    public void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE", Locale.getDefault());
        binding.currentDate.setText(dateFormat.format(new GregorianCalendar().getTime()));
    }
}