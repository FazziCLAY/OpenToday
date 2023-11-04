package com.fazziclay.opentoday.gui.fragment.item;

import static com.fazziclay.opentoday.util.InlineUtil.nullStat;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.ItemPath;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Readonly;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.app.settings.Option;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.gui.EnumsRegistry;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.UUID;

public class ItemsEditorRootFragment extends Fragment implements NavigationHost {
    private static final int ROOT_CONTAINER_ID = R.id.changeOnLeftSwipe;
    private static final String EXTRA_TAB_ID = "items_editor_root_fragment_tabId";
    private static final String TAG = "ItemsEditorRootFragment";


    private Context context;
    private TextView path;
    private Item current;
    private final SettingsManager.OptionChangedCallback optionChangedCallback = new SettingsManager.OptionChangedCallback() {
        @Override
        public Status run(Option option, Object value) {
            if (option == SettingsManager.ITEM_PATH_VISIBLE) {
                viewVisible(path, (Boolean) value, View.GONE);
            }
            return Status.NONE;
        }
    };

    @NonNull
    public static ItemsEditorRootFragment create(@NonNull UUID tabId) {
        ItemsEditorRootFragment fragment = new ItemsEditorRootFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TAB_ID, tabId.toString());
        fragment.setArguments(args);
        return fragment;
    }

    private TabsManager tabsManager;
    private SettingsManager sm;
    private UUID tabId;
    private Tab tab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = requireContext();
        Logger.d(TAG, "onCreate", nullStat(savedInstanceState));

        Bundle args = getArguments();
        final App app = App.get(context);
        this.tabsManager = app.getTabsManager();
        this.sm = app.getSettingsManager();
        tabId = UUID.fromString(args.getString(EXTRA_TAB_ID));
        tab = tabsManager.getTabById(tabId);

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(ROOT_CONTAINER_ID, ItemsEditorFragment.createRoot(tabId, (tab instanceof Readonly)))
                    .commit();

            getChildFragmentManager().addOnBackStackChangedListener(() -> {
                Logger.d(TAG, "(child fragment manager)", "onBackStackChanged");
                updatePath();
                triggerUpdateISToCurrent();
            });
            //getChildFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> triggerUpdateISToCurrent());
            //updateItemStorageContext(tab);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView", nullStat(savedInstanceState));
        path = new TextView(context);
        optionChangedCallback.run(SettingsManager.ITEM_PATH_VISIBLE, SettingsManager.ITEM_PATH_VISIBLE.get(sm));
        sm.callbacks.addCallback(CallbackImportance.DEFAULT, optionChangedCallback);
        updatePath();

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setPadding(0, 5, 0, 0);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.setId(ROOT_CONTAINER_ID);

        LinearLayout l = new LinearLayout(context);
        l.setOrientation(LinearLayout.VERTICAL);
        l.addView(path);
        l.addView(frameLayout);
        l.setBackground(getBackgroundDrawable());

        return l;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sm.callbacks.removeCallback(optionChangedCallback);
    }

    private Drawable getBackgroundDrawable() {
        // TODO: 2023.05.22 Add tab background
        return tab.getName().equalsIgnoreCase("FAZZICLAY TAB") ? AppCompatResources.getDrawable(requireContext(), R.mipmap.ic_launcher) : null;
    }

    private String getPath() {
        ItemPath itemPath = tabsManager.getPathTo(current);
        final String PATH_SEPARATOR = " / ";
        StringBuilder s = new StringBuilder(PATH_SEPARATOR);
        for (Object section : itemPath.getSections()) {
            if (section instanceof Item item) {
                String sect = EnumsRegistry.INSTANCE.name(ItemsRegistry.REGISTRY.get(item.getClass()).getItemType(), requireContext());
                s.append(sect).append(PATH_SEPARATOR);
            }
        }
        return s.toString().trim();
    }

    private void updatePath() {
        path.setText(getPath());
    }

    @Override
    public void navigate(@NonNull Fragment fragment, boolean addToBackStack) {
        Logger.d(TAG, "navigate", "to=", fragment, "back=", addToBackStack);
        if (!(fragment instanceof ItemsEditorFragment ief)) throw new RuntimeException("Other fragments not allowed.");

        FragmentTransaction transaction = getChildFragmentManager()
                .beginTransaction()
                .replace(ROOT_CONTAINER_ID, ief);

        String backStackName = null;
        UUID itemId = ief.getItemIdFromExtra();
        Item item = tab.getItemById(itemId);
        if (item != null) {
            backStackName = item.getText().split("\n")[0];
            if (backStackName.isEmpty()) backStackName = getString(EnumsRegistry.INSTANCE.nameResId(ItemsRegistry.REGISTRY.get(item.getClass()).getItemType()));
            updateItemStorageContext((ItemsStorage) item);
        }

        if (addToBackStack) transaction.addToBackStack(backStackName);
        transaction.commit();
    }

    @Override
    public boolean popBackStack() {
        Logger.d(TAG, "popBackStack");

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            updateItemStorageContext(getCurrentItemsStorage());
            return true;
        }
        return false;
    }

    public ItemsStorage getCurrentItemsStorage() {
        ItemsEditorFragment fragment = (ItemsEditorFragment) getChildFragmentManager().findFragmentById(ROOT_CONTAINER_ID);
        Logger.d(TAG, "getCurrentItemsStorage find=", fragment);
        if (fragment == null) return null;
        return fragment.getItemStorage();
    }

    private void updateItemStorageContext(ItemsStorage itemsStorage) {
        Logger.d(TAG, "updateItemStorageContext", itemsStorage);

        ItemsTabIncludeFragment f = ((ItemsTabIncludeFragment) getParentFragment());
        if (f == null) {
            Logger.d(TAG, "updateItemStorageContext", "parent fragment is null!!!!");
            return;
        }
        f.setItemStorageInContext(itemsStorage);
    }

    public void triggerUpdateISToCurrent() {
        Logger.d(TAG, "triggerUpdateISToCurrent");
        if (getCurrentItemsStorage() != null) updateItemStorageContext(getCurrentItemsStorage());
    }

    public void toRoot() {
        Logger.d(TAG, "toRoot");
        FragmentManager fm = getChildFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }

    public void childAttached(ItemsEditorFragment itemsEditorFragment, Item item) {
        Logger.d(TAG, "child attached! item="+item);
        this.current = item;
        updatePath();
    }
}
