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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemStorage;
import ru.fazziclay.opentoday.app.items.ItemsTab;
import ru.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.app.items.notifications.ItemNotification;
import ru.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import ru.fazziclay.opentoday.ui.UITickService;
import ru.fazziclay.opentoday.app.updatechecker.UpdateChecker;
import ru.fazziclay.opentoday.callback.CallbackImportance;
import ru.fazziclay.opentoday.callback.Status;
import ru.fazziclay.opentoday.databinding.ActivityMainBinding;
import ru.fazziclay.opentoday.ui.fragment.ItemsEditorRootFragment;
import ru.fazziclay.opentoday.ui.interfaces.ContainBackStack;
import ru.fazziclay.opentoday.ui.interfaces.CurrentItemsTab;
import ru.fazziclay.opentoday.ui.interfaces.NavigationHost;
import ru.fazziclay.opentoday.ui.other.AppToolbar;

public class MainActivity extends AppCompatActivity implements NavigationHost, CurrentItemsTab {
    private ActivityMainBinding binding;
    private App app;
    private ItemManager itemManager;
    private AppToolbar toolbar;
    private ItemStorage currentItemStorage;
    private UITickService uiTickService;

    // Tabs
    private UUID currentTab = null;
    private FragmentStateAdapter tabsViewPagerAdapter = new LocalViewPagerAdapter();
    private final OnTabsChanged onTabsChanged = new LocalOnTabChanged();
    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new LocalOnTabSelectedListener();

    // Current Date
    private Handler currentDateHandler;
    private Runnable currentDateRunnable;
    private GregorianCalendar currentDateCalendar;
    private File latestTabCacheFile;


    // Activity overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {}

        this.app = App.get(this);
        this.app.setAppInForeground(true);
        this.app.getTelemetry().mainActivityStart();
        this.itemManager = app.getItemManager();
        this.uiTickService = new UITickService(this);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());

        // Tabs
        latestTabCacheFile = new File(getExternalCacheDir(), "latest-tab");
        ItemsTab extraTab = null;
        if (FileUtil.isExist(latestTabCacheFile)) {
            UUID l = null;
            try {
                l = UUID.fromString(FileUtil.getText(latestTabCacheFile));
            } catch (Exception ignored) {}
            if (l != null) {
                extraTab = itemManager.getTab(l);
            }
        }
        if (extraTab == null) {
            extraTab = itemManager.getMainTab();
        }

        itemManager.getOnTabsChanged().addCallback(CallbackImportance.DEFAULT, onTabsChanged);
        binding.viewPager.setAdapter(tabsViewPagerAdapter);
        binding.viewPager.setUserInputEnabled(false);

        setCurrentTab(extraTab.getId());
        reloadTabs(extraTab.getId());
        selectViewPager(extraTab.getId(), false);
        // Tabs end

        this.toolbar = new AppToolbar(this, itemManager, getCurrentTab());
        /*Tabs*/MainActivity.this.setItemStorageInContext(itemManager.getTab(extraTab.getId()));

        super.onCreate(savedInstanceState);

        this.binding.toolbar.addView(this.toolbar.getToolbarView());
        this.binding.toolbarMore.addView(this.toolbar.getToolbarMoreView());
        this.toolbar.setOnMoreVisibleChangedListener(visible -> binding.quickNote.setVisibility(visible ? View.GONE : View.VISIBLE));
        this.toolbar.create();
        setupQuickNote();

        setContentView(binding.getRoot());

        setupBatteryOptimizationNotify();
        setupUpdateAvailableNotify();
        setupCurrentDate();

        if (app.getSettingsManager().isQuickNote()) {
            QuickNoteReceiver.sendQuickNoteNotification(this);
        }
        uiTickService.create();
    }

    private void setupQuickNote() {
        binding.quickNoteAdd.setOnClickListener(v -> {
            String s = binding.quickNoteText.getText().toString();
            if (s.isEmpty()) return;
            binding.quickNoteText.setText("");
            if (currentItemStorage != null) {
                TextItem item = new TextItem(s);
                if (app.getSettingsManager().isParseTimeFromQuickNote()) item.getNotifications().addAll(App.QUICK_NOTE.run(s));
                currentItemStorage.addItem(item);
            }
        });

        binding.quickNoteAdd.setOnLongClickListener(v -> {
            String s = binding.quickNoteText.getText().toString();
            if (currentItemStorage != null) {
                TextItem item = new TextItem(s);
                if (app.getSettingsManager().isParseTimeFromQuickNote()) item.getNotifications().addAll(App.QUICK_NOTE.run(s));
                currentItemStorage.addItem(item);
            }
            return true;
        });
    }

    private long lastExitClick = 0;
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

        Fragment fragment = getCurrentViewPagerFragment();
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
        if (app != null) app.setAppInForeground(false);
        if (itemManager != null) {
            itemManager.getOnTabsChanged().deleteCallback(onTabsChanged);
        }
        if (toolbar != null) {
            toolbar.destroy();
        }
        if (uiTickService != null) {
            uiTickService.destroy();
        }
        currentDateHandler.removeCallbacks(currentDateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBatteryOptimizationNotify();
    }

    // Navigation host
    @Override
    public void navigate(Fragment fragment, boolean addToBackStack) {
        Fragment current = getCurrentViewPagerFragment();
        if (current instanceof NavigationHost) {
            NavigationHost navigationHost = (NavigationHost) current;
            navigationHost.navigate(fragment, addToBackStack);
        }
    }

    @Override
    public boolean popBackStack() {
        Fragment current = getCurrentViewPagerFragment();
        if (current instanceof NavigationHost) {
            NavigationHost navigationHost = (NavigationHost) current;
            return navigationHost.popBackStack();
        }
        return false;
    }

    private Fragment getCurrentViewPagerFragment() {
        return getSupportFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());
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

        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd EEEE", Locale.getDefault());
        binding.currentDate.setText(dateFormat.format(time));

        // Time
        dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        binding.currentTime.setText(dateFormat.format(time));
    }

    // Battery
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

    // Update checker
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

    // CurrentItemsTab implements
    public void setItemStorageInContext(ItemStorage itemStorage) {
        d("changed itemStorage =", itemStorage);
        this.currentItemStorage = itemStorage;
        this.toolbar.setItemStorage(itemStorage);
    }

    private void selectViewPager(UUID tabId) {
        selectViewPager(tabId, true);
    }

    private void selectViewPager(UUID tabId, boolean smoothScroll) {
        int i = 0;
        for (ItemsTab tab : itemManager.getTabs()) {
            if (tab.getId().equals(tabId)) break;
            i++;
        }
        binding.viewPager.setCurrentItem(i, smoothScroll);
    }

    private void setupTabs() {
        binding.tabs.removeAllTabs();

        for (ItemsTab itemsTab : itemManager.getTabs()) {
            TabLayout.Tab tabView = binding.tabs.newTab();
            tabView.setTag(itemsTab.getId().toString());
            tabView.setText(itemsTab.getName());
            binding.tabs.addTab(tabView);
        }
    }

    private void reloadTabs(UUID selected) {
        binding.tabs.removeOnTabSelectedListener(onTabSelectedListener);
        setupTabs();

        int i = 0;
        while (i < binding.tabs.getTabCount()) {
            TabLayout.Tab tabView = binding.tabs.getTabAt(i);
            UUID tabUUID = UUID.fromString(tabView.getTag().toString());
            if (selected.equals(tabUUID)) binding.tabs.selectTab(tabView);
            i++;
        }

        binding.tabs.setVisibility(binding.tabs.getTabCount() == 1 ? View.GONE : View.VISIBLE);
        binding.tabs.addOnTabSelectedListener(onTabSelectedListener);
    }

    @Override
    public UUID getCurrentTabId() {
        return currentTab;
    }

    @Override
    public ItemsTab getCurrentTab() {
        return itemManager.getTab(getCurrentTabId());
    }

    @Override
    public void setCurrentTab(UUID id) {
        currentTab = id;
        if (currentTab != null) FileUtil.setText(latestTabCacheFile, currentTab.toString());
    }

    // On UI tab selected
    private class LocalOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            d("onTab selected");
            if (tab == null || tab.getTag() == null) return;
            UUID id = UUID.fromString(String.valueOf(tab.getTag()));
            setCurrentTab(id);
            MainActivity.this.selectViewPager(id);
            MainActivity.this.setItemStorageInContext(itemManager.getTab(id));
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            d("onTab reselected");
            if (tab == null || tab.getTag() == null) return;
            UUID id = UUID.fromString(String.valueOf(tab.getTag()));
            setCurrentTab(id);
            MainActivity.this.selectViewPager(id);
            MainActivity.this.setItemStorageInContext(itemManager.getTab(id));
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}
    }

    // On itemManager tabs changed
    private class LocalOnTabChanged implements OnTabsChanged {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public Status run(ItemsTab[] tabs) {
            d("on tabs changed");
            UUID id = null;
            for (ItemsTab tab : tabs) {
                if (tab.getId().equals(currentTab)) {
                    id = currentTab;
                    break;
                }
            }
            if (id == null) {
                id = itemManager.getMainTab().getId();
            }
            MainActivity.this.setCurrentTab(id);
            MainActivity.this.reloadTabs(id);
            d("-- DATa set changed notify");
            tabsViewPagerAdapter = new LocalViewPagerAdapter();
            binding.viewPager.setAdapter(tabsViewPagerAdapter);

            MainActivity.this.selectViewPager(id);
            MainActivity.this.setItemStorageInContext(itemManager.getTab(id));
            return Status.NONE;
        }
    }

    // For viewPager
    private class LocalViewPagerAdapter extends FragmentStateAdapter {
        public LocalViewPagerAdapter() {
            super(MainActivity.this);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            d("viewPager adapter createFragment pos=", position, "tab=", itemManager.getTabs().get(position));
            return ItemsEditorRootFragment.create(itemManager.getTabs().get(position).getId());
        }

        @Override
        public int getItemCount() {
            d("viewPager adapter getCount=", itemManager.getTabs().size());
            return itemManager.getTabs().size();
        }
    }

    private void d(Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            stringBuilder.append(object.toString()).append(" ");
        }
        boolean TOAST = false;
        boolean ALOG = true;
        if (TOAST)Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
        if (ALOG) Log.e("------------D", stringBuilder.toString());
    }

    public interface QuickNoteInterface {
        List<ItemNotification> run(String text);
    }
}