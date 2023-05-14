package com.fazziclay.opentoday.gui.fragment;

import static com.fazziclay.opentoday.util.InlineUtil.nullStat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.SelectionManager;
import com.fazziclay.opentoday.app.items.callback.ItemCallback;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.databinding.ItemsStorageEmptyBinding;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.activity.MainActivity;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.gui.interfaces.StorageEditsActions;
import com.fazziclay.opentoday.gui.item.ItemStorageDrawer;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ItemsEditorFragment extends Fragment {
    private static final int RES_FILTER_BUTTON_IMAGE = android.R.drawable.ic_menu_manage;
    private static final int COLOR_FILTER_GROUP_ACTIVE = Color.GREEN;
    private static final int COLOR_FILTER_GROUP_INACTIVE = Color.RED;
    private static final String EXTRA_TAB_ID = "items_editor_fragment_tabId";
    private static final String EXTRA_ITEM_ID = "items_editor_fragment_itemId";
    private static final String EXTRA_PREVIEW_MODE = "items_editor_fragment_previewMode";
    private static final String TAG = "ItemsEditorFragment";

    private MainActivity activity;
    private NavigationHost navigationHost;
    private NavigationHost rootNavigationHost;
    private ItemManager itemManager;
    private SettingsManager settingsManager;
    private SelectionManager selectionManager;
    private ItemsStorage itemsStorage;
    private boolean previewMode;
    private LinearLayout layout;
    private boolean currentlyIsNone;
    private ItemStorageDrawer itemStorageDrawer;

    private UUID tabId;
    private UUID itemId;
    private boolean isRoot;

    private Tab tab;
    private Item item;
    private final List<Runnable> onCreateListeners = new ArrayList<>();
    private OnItemsStorageUpdate onItemStorageChangeCallback;
    private ItemCallback itemCallback;

    public static ItemsEditorFragment createRoot(UUID tab) {
        return ItemsEditorFragment.create(tab, null, false);
    }

    public static ItemsEditorFragment createItem(UUID tab, UUID item) {
        return ItemsEditorFragment.create(tab, item, false);
    }

    private static ItemsEditorFragment create(UUID tab, UUID item, boolean previewMode) {
        ItemsEditorFragment fragment = new ItemsEditorFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_PREVIEW_MODE, previewMode);
        args.putString(EXTRA_TAB_ID, tab.toString());
        if (item != null) args.putString(EXTRA_ITEM_ID, item.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate", nullStat(savedInstanceState));
        activity = (MainActivity) requireActivity();
        navigationHost = (NavigationHost) getParentFragment();
        rootNavigationHost = UI.findFragmentInParents(this, MainRootFragment.class);
        App app = App.get(requireContext());
        itemManager = app.getItemManager();
        settingsManager = app.getSettingsManager();
        selectionManager = app.getSelectionManager();

        Bundle args = getArguments();
        previewMode = args.getBoolean(EXTRA_PREVIEW_MODE);
        tabId = UUID.fromString(args.getString(EXTRA_TAB_ID));
        tab = itemManager.getTab(tabId);

        isRoot = !args.containsKey(EXTRA_ITEM_ID);
        if (isRoot) {
            this.itemsStorage = tab;

        } else {
            itemId = UUID.fromString(args.getString(EXTRA_ITEM_ID));
            item = tab.getItemById(itemId);

            if (item instanceof ItemsStorage) {
                itemsStorage = (ItemsStorage) item;
            } else {
                throw new RuntimeException("Cannot get ItemStorage from item. Item=" + item + "; id=" + itemId + "; tab=" + tab + "; tabId="+tabId);
            }
        }

        if (previewMode) {
            this.itemStorageDrawer = ItemStorageDrawer.builder(activity, itemManager, settingsManager, selectionManager, itemsStorage)
                    .setPreviewMode()
                    .build();
        } else {
            this.itemStorageDrawer = ItemStorageDrawer.builder(activity, itemManager, settingsManager, selectionManager, itemsStorage)
                    .setOnItemOpenEditor((item) -> rootNavigationHost.navigate(ItemEditorFragment.edit(item.getId()), true))
                    .setStorageEditsActions(new StorageEditsActions() {
                        @Override
                        public void onGroupEdit(@NonNull GroupItem groupItem) {
                            navigationHost.navigate(createItem(tabId, groupItem.getId()), true);
                        }

                        @Override
                        public void onCycleListEdit(@NonNull CycleListItem cycleListItem) {
                            navigationHost.navigate(createItem(tabId, cycleListItem.getId()), true);
                        }

                        @Override
                        public void onFilterGroupEdit(@NonNull FilterGroupItem filterGroupItem) {
                            navigationHost.navigate(createItem(tabId, filterGroupItem.getId()), true);
                        }
                    })
                    .build();
        }

        if (item instanceof FilterGroupItem) {
            applyFilterGroupViewPatch((FilterGroupItem) item);
        }

        itemStorageDrawer.create();

        layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Empty plug
        updateNotFoundState(true, itemsStorage.isEmpty());
        onItemStorageChangeCallback = new OnItemsStorageUpdate() {
            @Override
            public Status onAdded(Item item, int position) {
                updateNotFoundState(false, false);
                return Status.NONE;
            }

            @Override
            public Status onPostDeleted(Item item, int position) {
                updateNotFoundState(false, itemsStorage.isEmpty());
                return Status.NONE;
            }
        };
        itemsStorage.getOnUpdateCallbacks().addCallback(CallbackImportance.MIN, onItemStorageChangeCallback);
        runOnCreateListeners();
    }

    private void updateNotFoundState(boolean ignoreCache, boolean none) {
        if (!ignoreCache && currentlyIsNone == none) {
            return;
        }
        currentlyIsNone = none;
        layout.removeAllViews();
        if (none) {
            ItemsStorageEmptyBinding b = ItemsStorageEmptyBinding.inflate(getLayoutInflater());
            b.getRoot().setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.addView(b.getRoot());
        } else {
            layout.addView(itemStorageDrawer.getView());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView", nullStat(savedInstanceState));
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.itemStorageDrawer != null) this.itemStorageDrawer.destroy();
        if (this.itemsStorage != null) itemsStorage.getOnUpdateCallbacks().deleteCallback(onItemStorageChangeCallback);
        if (this.item != null) item.getItemCallbacks().deleteCallback(itemCallback);
    }

    public ItemsStorage getItemStorage() {
        return itemsStorage;
    }

    public UUID getTabId() {
        return tabId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void addOnCreateListener(Runnable o) {
        onCreateListeners.add(o); // TODO: 3/11/23 remove unused onCreateListener
    }

    @Nullable
    public UUID getItemIdFromExtra() {
        if (getArguments().containsKey(EXTRA_ITEM_ID)) {
            return UUID.fromString(getArguments().getString(EXTRA_ITEM_ID));
        }
        return null;
    }

    private FilterGroupItem getFilterGroupItem() {
        if (item instanceof FilterGroupItem) {
            return (FilterGroupItem) item;
        }
        throw new RuntimeException("this.item is not a FilterGroupItem", new ClassCastException("this.item is a " + item.getClass().getName()));
    }

    private void filterGroup_setEditButtonBackground(View view, Item item) {
        FilterGroupItem filterGroup = getFilterGroupItem();
        view.setBackgroundTintList(ColorStateList.valueOf(filterGroup.isActiveItem(item) ? COLOR_FILTER_GROUP_ACTIVE : COLOR_FILTER_GROUP_INACTIVE));
    }

    private final HashMap<Item, ImageButton> buttons = new HashMap<>(); // TODO: 5/9/23 FIX THIIS: NOT DELETING OLDEST
    private void applyFilterGroupViewPatch(FilterGroupItem filterGroupItem) {
        itemStorageDrawer.setItemViewWrapper((item, view) -> {
            LinearLayout layout = new LinearLayout(view.getContext());
            layout.addView(view);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            ImageButton filter = new ImageButton(view.getContext());
            filter.setImageResource(RES_FILTER_BUTTON_IMAGE);
            filterGroup_setEditButtonBackground(filter, item);
            filter.setOnClickListener(v -> editFilterGroupItemFilter(filterGroupItem, item));
            layout.addView(filter);
            filter.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 70, 0));
            buttons.put(item, filter);
            return layout;
        });

        itemCallback = new ItemCallback() {
            @Override
            public Status updateUi(Item item) {
                buttons.forEach((imageItem, imageButton) -> filterGroup_setEditButtonBackground(imageButton, imageItem));
                return Status.NONE;
            }
        };
        if (item != null) item.getItemCallbacks().addCallback(CallbackImportance.MIN, itemCallback);
    }

    private void editFilterGroupItemFilter(FilterGroupItem filterGroupItem, Item item) {
        rootNavigationHost.navigate(FilterGroupItemFilterEditorFragment.create(filterGroupItem.getId(), item.getId()), true);
    }

    @Deprecated
    private void runOnCreateListeners() {
        for (Runnable e : onCreateListeners) {
            e.run();
        }
    }
}
