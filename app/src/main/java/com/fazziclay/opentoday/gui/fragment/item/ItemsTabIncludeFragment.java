package com.fazziclay.opentoday.gui.fragment.item;

import static com.fazziclay.opentoday.util.InlineUtil.nullStat;
import static com.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.api.EventHandler;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.events.gui.CurrentItemsStorageContextChanged;
import com.fazziclay.opentoday.app.icons.IconsRegistry;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Unique;
import com.fazziclay.opentoday.app.items.callback.OnTabsChanged;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.app.settings.enums.FirstTab;
import com.fazziclay.opentoday.databinding.FragmentItemsTabIncludeBinding;
import com.fazziclay.opentoday.gui.GuiItemsHelper;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.dialog.DialogSelectItemType;
import com.fazziclay.opentoday.gui.fragment.ImportFragment;
import com.fazziclay.opentoday.gui.fragment.MainRootFragment;
import com.fazziclay.opentoday.gui.interfaces.CurrentItemsTab;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.gui.toolbar.AppToolbar;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.QuickNote;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;
import com.google.android.material.tabs.TabLayout;

import java.util.UUID;

public class ItemsTabIncludeFragment extends Fragment implements CurrentItemsTab, NavigationHost {
    private static final String TAG = "ItemsTabIncludeFragment";

    public static final QuickNote.QuickNoteInterface QUICK_NOTE_NOTIFICATIONS_PARSE = QuickNote.QUICK_NOTE_NOTIFICATIONS_PARSE;
    public static ItemsTabIncludeFragment create() {
        return new ItemsTabIncludeFragment();
    }



    private FragmentItemsTabIncludeBinding binding;
    private App app;
    private TabsManager tabsManager;
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
        this.tabsManager = app.getTabsManager();
        this.settingsManager = app.getSettingsManager();
        this.selectionManager = app.getSelectionManager();
        this.rootNavigationHost = UI.findFragmentInParents(this, MainRootFragment.class);
        binding = FragmentItemsTabIncludeBinding.inflate(getLayoutInflater());

        if (settingsManager.getFirstTab() == FirstTab.TAB_ON_CLOSING) {
            currentTab = getLastTabId();
            if (currentTab == null || tabsManager.getTabById(currentTab) == null) {
                currentTab = tabsManager.getFirstTab().getId();
                currentItemsStorage = tabsManager.getFirstTab();
            } else {
                currentItemsStorage = tabsManager.getTabById(currentTab);
            }
        } else if (settingsManager.getFirstTab() == FirstTab.FIRST) {
            currentTab = tabsManager.getFirstTab().getId();
            currentItemsStorage = tabsManager.getFirstTab();
        } else {
            throw new RuntimeException("Unknown firstTab settings!");
        }
        EventHandler.call(new CurrentItemsStorageContextChanged(currentItemsStorage));

        this.toolbar = new AppToolbar(requireActivity(), tabsManager, settingsManager, selectionManager, currentItemsStorage, rootNavigationHost, binding.toolbar, binding.toolbarMore);

        // Tabs
        tabsManager.getOnTabsChangedCallbacks().addCallback(CallbackImportance.DEFAULT, localOnTabChanged);
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
        viewClick(binding.addTabInlineButton, () -> {
            AppToolbar.showAddTabDialog(requireContext(), tabsManager);
        });


        this.toolbar.setOnMoreVisibleChangedListener(visible -> {
            binding.quickNote.setVisibility(!visible ? View.VISIBLE : View.INVISIBLE);
            final InputMethodManager imm = getContext().getSystemService(InputMethodManager.class);
            if (imm.isActive()) {
                binding.quickNote.requestFocus();
            }
        });
        this.toolbar.create();

        setupQuickNote();
    }

    /**
     * Get latest active tab (saved)
     */
    private UUID getLastTabId() {
        String s = requireContext().getSharedPreferences(App.SHARED_NAME, Context.MODE_PRIVATE).getString(App.SHARED_KEY_LAST_TAB, "");
        try {
            return UUID.fromString(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Save latest tab id for {@link #getLastTabId()}
     * @param id data for save
     */
    private void setLastTabId(final UUID id) {
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
        tabsManager.getOnTabsChangedCallbacks().removeCallback(localOnTabChanged);
        if (toolbar != null) toolbar.destroy();
        EventHandler.call(new CurrentItemsStorageContextChanged(null));
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
                Item item = GuiItemsHelper.createItem(requireContext(), settingsManager.getDefaultQuickNoteType(), text, settingsManager);
                if (settingsManager.isParseTimeFromQuickNote()) item.addNotifications(QUICK_NOTE_NOTIFICATIONS_PARSE.run(text));
                GuiItemsHelper.addItem(item, currentItemsStorage, settingsManager);
            }
        });

        binding.quickNoteAdd.setOnLongClickListener(v -> {
            new DialogSelectItemType(requireContext(), (type) -> {
                final ItemsRegistry.ItemInfo registryItem = ItemsRegistry.REGISTRY.get(type);
                if (settingsManager.isChangeDefaultQuickNoteInLongSendClick()) {
                    settingsManager.setDefaultQuickNoteType(registryItem);
                    settingsManager.save();
                }

                String text = binding.quickNoteText.getText().toString();
                binding.quickNoteText.setText("");

                Item item = GuiItemsHelper.createItem(requireContext(), registryItem, text, settingsManager);
                if (settingsManager.isParseTimeFromQuickNote()) item.addNotifications(QUICK_NOTE_NOTIFICATIONS_PARSE.run(text));

                GuiItemsHelper.addItem(item, currentItemsStorage, settingsManager);
            }).show();
            return true;
        });
    }

    public void setItemStorageInContext(ItemsStorage itemsStorage) {
        Logger.d(TAG, "setItemStorageInContext", itemsStorage);
        this.currentItemsStorage = itemsStorage;
        this.toolbar.setItemStorage(itemsStorage);
        EventHandler.call(new CurrentItemsStorageContextChanged(currentItemsStorage));
    }

    private void updateViewPager(boolean smoothScroll) {
        int position = tabsManager.getTabPosition(tabsManager.getTabById(currentTab));
        tabsViewPagerAdapter.notifyItemChanged(position);
        tabsViewPagerAdapter.getItemViewType(position);
        binding.viewPager.setCurrentItem(position, smoothScroll);
    }

    private void setupTabs() {
        binding.tabs.removeAllTabs();

        for (final Tab tab : tabsManager.getAllTabs()) {
            TabLayout.Tab tabView = binding.tabs.newTab();
            tabView.setTag(tab.getId().toString());
            tabView.setText(tab.getName());
            IconsRegistry.Icon icon = tab.getIcon();
            if (icon != IconsRegistry.REGISTRY.NONE) {
                tabView.setIcon(icon.getResId());
            }
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

        binding.tabsScrollView.setVisibility(binding.tabs.getTabCount() == 1 ? View.GONE : View.VISIBLE);
        binding.tabs.addOnTabSelectedListener(uiOnTabSelectedListener);
    }

    @NonNull
    @Override
    public UUID getCurrentTabId() {
        return currentTab;
    }

    @NonNull
    @Override
    public Tab getCurrentTab() {
        return tabsManager.getTabById(getCurrentTabId());
    }

    @Override
    public void setCurrentTab(UUID id) {
        currentTab = id;
        setLastTabId(id);
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
    public void navigate(@NonNull Fragment navigateTo, boolean addToBackStack) {
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

    // On tabs changed
    private class LocalOnTabChanged extends OnTabsChanged {
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
                id = tabsManager.getFirstTab().getId();
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
            Tab tab = tabsManager.getAllTabs()[position];
            Logger.d(TAG, LocalViewPagerAdapter.class.getSimpleName(), "createFragment", "position=", position, "(tab by <position> in tabsManager)=", tab);
            return ItemsEditorRootFragment.create(tab.getId());
        }

        @Override
        public int getItemCount() {
            return tabsManager.getAllTabs().length;
        }
    }
}
