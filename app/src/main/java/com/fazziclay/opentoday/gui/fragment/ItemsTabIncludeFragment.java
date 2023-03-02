package com.fazziclay.opentoday.gui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.items.ID;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;
import com.fazziclay.opentoday.databinding.FragmentItemsTabIncludeBinding;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.interfaces.CurrentItemsTab;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.gui.toolbar.AppToolbar;
import com.fazziclay.opentoday.util.L;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fazziclay.javaneoutil.FileUtil;

public class ItemsTabIncludeFragment extends Fragment implements CurrentItemsTab, NavigationHost {
    private static final String TAG = "ItemsTabIncludeFragment";

    public static final QuickNoteInterface QUICK_NOTE_NOTIFICATIONS_PARSE = s -> {
        List<ItemNotification> notifys = new ArrayList<>();
        boolean parseTime = true;
        if (parseTime) {
            char[] chars = s.toCharArray();
            int i = 0;
            for (char aChar : chars) {
                if (aChar == ':') {
                    try {
                        if (i >= 2 && chars.length >= 5) {
                            int hours = Integer.parseInt(String.valueOf(chars[i - 2]) + chars[i - 1]);
                            int minutes = Integer.parseInt(String.valueOf(chars[i + 1]) + chars[i + 2]);

                            DayItemNotification noti = new DayItemNotification();
                            noti.setTime((hours * 60 * 60) + (minutes * 60));
                            noti.setNotifyTextFromItemText(true);
                            notifys.add(noti);
                        }
                    } catch (Exception ignored) {
                    }
                }
                i++;
            }
        }
        return notifys;
    };

    public static ItemsTabIncludeFragment create() {
        return new ItemsTabIncludeFragment();
    }


    private FragmentItemsTabIncludeBinding binding;
    private App app;
    private ItemManager itemManager;
    private SettingsManager settingsManager;
    private AppToolbar toolbar;
    private ItemsStorage currentItemsStorage;

    // Tabs
    private UUID currentTab = null;
    private FragmentStateAdapter tabsViewPagerAdapter;
    private final OnTabsChanged onTabsChanged = new LocalOnTabChanged();
    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new LocalOnTabSelectedListener();


    private File latestTabCacheFile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.o(TAG, "onCreate", L.nn(savedInstanceState));
        this.app = App.get(requireContext());
        this.itemManager = app.getItemManager();
        this.settingsManager = app.getSettingsManager();
        binding = FragmentItemsTabIncludeBinding.inflate(getLayoutInflater());

        // Tabs
        latestTabCacheFile = new File(getExternalCacheDir(), "latest-tab");
        Tab extraTab = null;
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
        tabsViewPagerAdapter = new LocalViewPagerAdapter();
        binding.viewPager.setAdapter(tabsViewPagerAdapter);
        binding.viewPager.setUserInputEnabled(false);

        setCurrentTab(extraTab.getId());
        reloadTabs(extraTab.getId());
        selectViewPager(extraTab.getId(), false);
        // Tabs end

        this.toolbar = new AppToolbar(requireActivity(), itemManager, settingsManager, getCurrentTab(), (NavigationHost) UI.findFragmentInParents(this, MainRootFragment.class), this);
        /*Tabs*/ this.setItemStorageInContext(itemManager.getTab(extraTab.getId()));
        /*Tabs*/ toolbar.setTab(extraTab);



        this.binding.toolbar.addView(this.toolbar.getToolbarView());
        this.binding.toolbarMore.addView(this.toolbar.getToolbarMoreView());
        this.toolbar.setOnMoreVisibleChangedListener(visible -> binding.quickNote.setVisibility(!visible ? View.VISIBLE : View.INVISIBLE));
        this.toolbar.create();
        setupQuickNote();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                ItemsEditorRootFragment r = getCurrentViewPagerFragment();
                if (r != null) r.triggerUpdateISToCurrent();
            }
        });
    }

    private File getExternalCacheDir() {
        return requireActivity().getExternalCacheDir();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        L.o(TAG, "onCreateView", L.nn(savedInstanceState));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        L.o(TAG, "onViewCreated", L.nn(savedInstanceState));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        L.o(TAG, "onDestroyView");
        binding.viewPager.setAdapter(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        itemManager.getOnTabsChanged().deleteCallback(onTabsChanged);
        if (toolbar != null) toolbar.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        L.o(TAG, "onResume");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        L.o(TAG, "onViewStateRestored", L.nn(savedInstanceState));

        tabsViewPagerAdapter = new LocalViewPagerAdapter();
        binding.viewPager.setAdapter(tabsViewPagerAdapter);
        selectViewPager(getCurrentTabId(), false);
        reloadTabs(getCurrentTabId());
    }

    private void setupQuickNote() {
        binding.quickNoteAdd.setOnClickListener(v -> {
            String s = binding.quickNoteText.getText().toString();
            if (s.isEmpty()) return;
            if (ImportWrapper.isImportText(s)) {
                binding.quickNoteText.setText("");
                UUID id = null;
                if (currentItemsStorage instanceof ID) {
                    ID i = (ID) currentItemsStorage;
                    id = i.getId();
                }

                if (id == null) {
                    Toast.makeText(requireContext(), R.string.toolbar_more_file_import_unsupported, Toast.LENGTH_SHORT).show();
                    return;
                }
                ((NavigationHost) UI.findFragmentInParents(this, MainRootFragment.class)).navigate(ImportFragment.create(id, s.trim(), true), true);
                return;
            }
            binding.quickNoteText.setText("");
            if (currentItemsStorage != null) {
                Item item = settingsManager.getDefaultQuickNoteType().create();

                if (item instanceof TextItem) {
                    TextItem textItem = (TextItem) item;
                    textItem.setText(s);
                }

                if (app.getSettingsManager().isParseTimeFromQuickNote()) item.getNotifications().addAll(QUICK_NOTE_NOTIFICATIONS_PARSE.run(s));
                currentItemsStorage.addItem(item);
            }
        });

        binding.quickNoteAdd.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), binding.quickNoteAdd);

            for (ItemsRegistry.ItemInfo registryItem : ItemsRegistry.REGISTRY.getAllItems()) {
                if (!registryItem.isCompatibility(app.getFeatureFlags())) {
                    continue;
                }
                MenuItem menuItem = popupMenu.getMenu().add(registryItem.getNameResId());
                menuItem.setOnMenuItemClickListener(clicked -> {
                    String s = binding.quickNoteText.getText().toString();
                    if (currentItemsStorage != null) {
                        Item item = registryItem.create();
                        if (item instanceof TextItem) {
                            TextItem textItem = (TextItem) item;
                            textItem.setText(s);
                        }
                        if (settingsManager.isParseTimeFromQuickNote()) item.getNotifications().addAll(QUICK_NOTE_NOTIFICATIONS_PARSE.run(s));
                        currentItemsStorage.addItem(item);
                    }

                    return true;
                });
            }

            popupMenu.show();
            return true;
        });
    }

    // CurrentItemsTab implements
    public void setItemStorageInContext(ItemsStorage itemsStorage) {
        d("changed itemStorage =", itemsStorage);
        this.currentItemsStorage = itemsStorage;
        this.toolbar.setItemStorage(itemsStorage);
    }

    private void selectViewPager(UUID tabId) {
        selectViewPager(tabId, true);
    }

    private void selectViewPager(UUID tabId, boolean smoothScroll) {
        int i = 0;
        for (Tab tab : itemManager.getTabs()) {
            if (tab.getId().equals(tabId)) break;
            i++;
        }
        binding.viewPager.setCurrentItem(i, smoothScroll);
    }

    private void setupTabs() {
        binding.tabs.removeAllTabs();

        for (Tab localItemsTab : itemManager.getTabs()) {
            TabLayout.Tab tabView = binding.tabs.newTab();
            tabView.setTag(localItemsTab.getId().toString());
            tabView.setText(localItemsTab.getName());
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
    public Tab getCurrentTab() {
        return itemManager.getTab(getCurrentTabId());
    }

    @Override
    public void setCurrentTab(UUID id) {
        currentTab = id;
        if (toolbar != null) toolbar.setTab(itemManager.getTab(id));
        if (currentTab != null) FileUtil.setText(latestTabCacheFile, currentTab.toString());
    }

    @Override
    public boolean popBackStack() {
        L.o(TAG, "popBackStack");
        ItemsEditorRootFragment fragment = getCurrentViewPagerFragment();
        if (fragment == null) {
            L.o(TAG, "popBackStack: current fragment null: -> false");
            return false;
        }
        boolean isPop = fragment.popBackStack();
        if (isPop == false) {
            if (toolbar.isMoreViewVisible()) {
                toolbar.closeMoreView();
                isPop = true;
            }
        }
        return isPop;
    }

    @Override
    public void navigate(Fragment navigateTo, boolean addToBackStack) {
        L.o(TAG, "navigate", "to=", navigateTo, "back=", addToBackStack);
        ItemsEditorRootFragment fragment = getCurrentViewPagerFragment();
        if (navigateTo != null) {
            fragment.navigate(navigateTo, addToBackStack);
        } else {
            L.o(TAG, "navigate: current fragment null!");
        }
    }

    private ItemsEditorRootFragment getCurrentViewPagerFragment() {
        return (ItemsEditorRootFragment) getChildFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());
    }

    public interface QuickNoteInterface {
        List<ItemNotification> run(String text);
    }

    // On UI tab selected
    private class LocalOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            d("onTab selected");
            if (tab == null || tab.getTag() == null) return;
            UUID id = UUID.fromString(String.valueOf(tab.getTag()));
            setCurrentTab(id);
            ItemsTabIncludeFragment.this.selectViewPager(id);
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            d("onTab reselected");
            if (tab == null || tab.getTag() == null) return;
            UUID id = UUID.fromString(String.valueOf(tab.getTag()));
            setCurrentTab(id);
            ItemsTabIncludeFragment.this.selectViewPager(id);
            ItemsEditorRootFragment r = getCurrentViewPagerFragment();
            if (r != null) r.toRoot();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}
    }

    // On itemManager tabs changed
    private class LocalOnTabChanged implements OnTabsChanged {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public Status onTabsChanged(@NonNull final Tab[] tabs) {
            d("on tabs changed");
            UUID id = null;
            for (Tab tab : tabs) {
                if (tab.getId().equals(currentTab)) {
                    id = currentTab;
                    break;
                }
            }
            if (id == null) {
                id = itemManager.getMainTab().getId();
            }
            ItemsTabIncludeFragment.this.setCurrentTab(id);
            ItemsTabIncludeFragment.this.reloadTabs(id);
            d("-- DATa set changed notify");
            tabsViewPagerAdapter = new LocalViewPagerAdapter();
            binding.viewPager.setAdapter(tabsViewPagerAdapter);

            ItemsTabIncludeFragment.this.selectViewPager(id, false);
            ItemsTabIncludeFragment.this.setItemStorageInContext(itemManager.getTab(id));
            return Status.NONE;
        }
    }

    // For viewPager
    private class LocalViewPagerAdapter extends FragmentStateAdapter {
        public LocalViewPagerAdapter() {
            super(ItemsTabIncludeFragment.this);
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
        if (TOAST) Toast.makeText(requireContext(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();
        if (ALOG) Log.e("------------D", stringBuilder.toString());
    }
}
