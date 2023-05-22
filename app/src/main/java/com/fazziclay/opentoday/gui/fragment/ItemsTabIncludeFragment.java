package com.fazziclay.opentoday.gui.fragment;

import static com.fazziclay.opentoday.util.InlineUtil.nullStat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.databinding.FragmentItemsTabIncludeBinding;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.interfaces.CurrentItemsTab;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.gui.toolbar.AppToolbar;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.QuickNote;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.UUID;

public class ItemsTabIncludeFragment extends Fragment implements CurrentItemsTab, NavigationHost {
    private static final String TAG = "ItemsTabIncludeFragment";

    public static final ItemsTabIncludeFragment.QuickNoteInterface QUICK_NOTE_NOTIFICATIONS_PARSE = QuickNote.QUICK_NOTE_NOTIFICATIONS_PARSE;
    public static ItemsTabIncludeFragment create() {
        return new ItemsTabIncludeFragment();
    }



    private FragmentItemsTabIncludeBinding binding;
    private App app;
    private ItemManager itemManager;
    private SettingsManager settingsManager;
    private SelectionManager selectionManager;
    private AppToolbar toolbar;
    private NavigationHost rootNavigationHost;
    private ItemsStorage currentItemsStorage; // <-------------------------- this generic current item storage

    // Tabs
    private UUID currentTab = null; // <----------------------------- this is current active tab
    private FragmentStateAdapter tabsViewPagerAdapter;
    private final OnTabsChanged localOnTabChanged = new LocalOnTabChanged();
    private final TabLayout.OnTabSelectedListener uiOnTabSelectedListener = new UIOnTabSelectedListener();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate", nullStat(savedInstanceState));
        this.app = App.get(requireContext());
        this.itemManager = app.getItemManager();
        this.settingsManager = app.getSettingsManager();
        this.selectionManager = app.getSelectionManager();
        this.rootNavigationHost = UI.findFragmentInParents(this, MainRootFragment.class);
        binding = FragmentItemsTabIncludeBinding.inflate(getLayoutInflater());

        if (settingsManager.getFirstTab() == SettingsManager.FirstTab.TAB_ON_CLOSING) {
            currentTab = getOldTabId();
            if (itemManager.getTab(currentTab) == null) {
                currentTab = itemManager.getMainTab().getId();
                currentItemsStorage = itemManager.getMainTab();
            } else {
                currentItemsStorage = itemManager.getTab(currentTab);
            }
        } else if (settingsManager.getFirstTab() == SettingsManager.FirstTab.FIRST) {
            currentTab = itemManager.getMainTab().getId();
            currentItemsStorage = itemManager.getMainTab();
        } else {
            throw new RuntimeException("Unknown firstTab settings!");
        }

        this.toolbar = new AppToolbar(requireActivity(), itemManager, settingsManager, selectionManager, currentItemsStorage, rootNavigationHost, binding.toolbar, binding.toolbarMore);

        // Tabs
        itemManager.getOnTabsChanged().addCallback(CallbackImportance.DEFAULT, localOnTabChanged);
        binding.viewPager.setAdapter(tabsViewPagerAdapter = new LocalViewPagerAdapter(this));
        binding.viewPager.setUserInputEnabled(false);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                ItemsEditorRootFragment r = getCurrentViewPagerFragment();
                Logger.d(TAG, "viewPager", "onPageSelected", "position=", position, "getCurrentItem()=", binding.viewPager.getCurrentItem(), "r="+r);
                if (r == null) {
                    ItemsTabIncludeFragment.this.setItemStorageInContext(getCurrentTab());
                } else {
                    ItemsTabIncludeFragment.this.setItemStorageInContext(r.getCurrentItemsStorage());
                }
            }
        });

        reloadTabs();
        updateViewPager(false);


        this.toolbar.setOnMoreVisibleChangedListener(visible -> binding.quickNote.setVisibility(!visible ? View.VISIBLE : View.INVISIBLE));
        this.toolbar.create();

        setupQuickNote();
    }

    private UUID getOldTabId() {
        String s = requireContext().getSharedPreferences(App.SHARED_NAME, Context.MODE_PRIVATE).getString(App.SHARED_KEY_LAST_TAB, "");
        try {
            return UUID.fromString(s);
        } catch (Exception e) {
            return null;
        }
    }

    private void setOldTabId(UUID id) {
        requireContext().getSharedPreferences(App.SHARED_NAME, Context.MODE_PRIVATE).edit().putString(App.SHARED_KEY_LAST_TAB, id.toString()).apply();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView", nullStat(savedInstanceState));
        if (Debug.CUSTOM_ITEMSTABINCLUDE_BACKGROUND) binding.getRoot().setBackgroundColor(Color.parseColor("#ffff00"));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated", nullStat(savedInstanceState));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logger.d(TAG, "onDestroyView", "viewPagerAdapter set to null!");
        binding.viewPager.setAdapter(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy");
        itemManager.getOnTabsChanged().deleteCallback(localOnTabChanged);
        if (toolbar != null) toolbar.destroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Logger.d(TAG, "onViewStateRestored", nullStat(savedInstanceState), "todo: здесь что-то должно выполняться?");
        binding.viewPager.setAdapter(tabsViewPagerAdapter = new LocalViewPagerAdapter(this));
        updateViewPager(false);
    }


    private void setupQuickNote() {
        binding.quickNoteAdd.setOnClickListener(v -> {
            String text = binding.quickNoteText.getText().toString();
            if (text.isEmpty()) return;
            binding.quickNoteText.setText("");

            if (ImportWrapper.isImportText(text)) {
                if (currentItemsStorage instanceof Unique) {
                    UUID id = ((Unique) currentItemsStorage).getId();
                    rootNavigationHost.navigate(ImportFragment.create(id, text.trim(), true), true);
                } else {
                    Toast.makeText(requireContext(), R.string.toolbar_more_file_import_unsupported, Toast.LENGTH_SHORT).show();
                }
            } else {
                Item item = settingsManager.getDefaultQuickNoteType().create();
                if (item instanceof TextItem) ((TextItem) item).setText(text);
                if (app.getSettingsManager().isParseTimeFromQuickNote()) item.getNotifications().addAll(QUICK_NOTE_NOTIFICATIONS_PARSE.run(text));
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
                    String text = binding.quickNoteText.getText().toString();
                    Item item = registryItem.create();
                    if (item instanceof TextItem) ((TextItem) item).setText(text);
                    if (settingsManager.isParseTimeFromQuickNote()) item.getNotifications().addAll(QUICK_NOTE_NOTIFICATIONS_PARSE.run(text));
                    currentItemsStorage.addItem(item);
                    return true;
                });
            }

            popupMenu.show();
            return true;
        });
    }

    public void setItemStorageInContext(ItemsStorage itemsStorage) {
        Logger.d(TAG, "setItemStorageInContext", itemsStorage);
        this.currentItemsStorage = itemsStorage;
        this.toolbar.setItemStorage(itemsStorage);
    }

    private void updateViewPager(boolean smoothScroll) {
        binding.viewPager.setCurrentItem(itemManager.getTabPosition(currentTab), smoothScroll);
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

    private void reloadTabs() {
        binding.tabs.removeOnTabSelectedListener(uiOnTabSelectedListener);
        setupTabs();

        int i = 0;
        while (i < binding.tabs.getTabCount()) {
            TabLayout.Tab tabView = binding.tabs.getTabAt(i);
            UUID tabUUID = UUID.fromString(tabView.getTag().toString());
            if (currentTab.equals(tabUUID)) binding.tabs.selectTab(tabView);
            i++;
        }

        binding.tabs.setVisibility(binding.tabs.getTabCount() == 1 ? View.GONE : View.VISIBLE);
        binding.tabs.addOnTabSelectedListener(uiOnTabSelectedListener);
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
        setOldTabId(id);
    }

    @Override
    public boolean popBackStack() {
        Logger.d(TAG, "popBackStack");
        ItemsEditorRootFragment fragment = getCurrentViewPagerFragment();
        if (fragment == null) {
            Logger.d(TAG, "popBackStack: current fragment null: -> false");
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
        Logger.d(TAG, "navigate", "to=", navigateTo, "addToBack=", addToBackStack);
        ItemsEditorRootFragment fragment = getCurrentViewPagerFragment();
        if (navigateTo != null) {
            fragment.navigate(navigateTo, addToBackStack);
        } else {
            Logger.d(TAG, "navigate: current fragment null!");
        }
    }

    private ItemsEditorRootFragment getCurrentViewPagerFragment() {
        return (ItemsEditorRootFragment) getChildFragmentManager().findFragmentByTag("f" + binding.viewPager.getCurrentItem());
    }

    public interface QuickNoteInterface {
        List<ItemNotification> run(String text);
    }

    // On UI tab selected
    private class UIOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            Logger.d(TAG, UIOnTabSelectedListener.class.getSimpleName(), "onTabSelected", tab);

            if (tab == null || tab.getTag() == null) return;
            UUID id = UUID.fromString(String.valueOf(tab.getTag()));
            setCurrentTab(id);
            setItemStorageInContext(getCurrentTab());
            ItemsTabIncludeFragment.this.updateViewPager(true);
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            Logger.d(TAG, UIOnTabSelectedListener.class.getSimpleName(), "onTabReselected", tab);
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1, true);

            if (tab == null || tab.getTag() == null) return;
            UUID id = UUID.fromString(String.valueOf(tab.getTag()));
            setCurrentTab(id);
            setItemStorageInContext(getCurrentTab());
            ItemsTabIncludeFragment.this.updateViewPager(false);
            ItemsEditorRootFragment r = getCurrentViewPagerFragment();
            if (r != null) r.toRoot();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            Logger.d(TAG, UIOnTabSelectedListener.class.getSimpleName(), "onTabUnselected", tab);
        }
    }

    // On itemManager tabs changed
    private class LocalOnTabChanged implements OnTabsChanged {
        @Override
        public Status onTabsChanged(@NonNull final Tab[] tabs) {
            Logger.d(TAG, LocalOnTabChanged.class.getSimpleName(), "onTabsChanged");

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
            ItemsTabIncludeFragment.this.setItemStorageInContext(getCurrentTab());
            ItemsTabIncludeFragment.this.reloadTabs();
            tabsViewPagerAdapter = new LocalViewPagerAdapter(ItemsTabIncludeFragment.this);
            binding.viewPager.setAdapter(tabsViewPagerAdapter);

            ItemsTabIncludeFragment.this.updateViewPager(true);
            return Status.NONE;
        }
    }

    // For viewPager
    private class LocalViewPagerAdapter extends FragmentStateAdapter {
        public LocalViewPagerAdapter(Fragment f) {
            super(f);
            Logger.d(TAG, LocalViewPagerAdapter.class.getSimpleName(), "<init>");
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Tab t = itemManager.getTabs().get(position);
            Logger.d(TAG, LocalViewPagerAdapter.class.getSimpleName(), "createFragment", "position=", position, "(tab by <position> in itemManager)=", t);
            return ItemsEditorRootFragment.create(t.getId());
        }

        @Override
        public int getItemCount() {
            return itemManager.getTabs().size();
        }
    }
}
