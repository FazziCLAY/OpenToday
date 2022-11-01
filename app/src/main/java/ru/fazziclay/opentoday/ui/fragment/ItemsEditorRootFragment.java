package ru.fazziclay.opentoday.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManagerNonConfig;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.fragment.app.FragmentTransaction;

import java.util.UUID;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemsStorage;
import ru.fazziclay.opentoday.app.items.tab.LocalItemsTab;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.tab.Tab;
import ru.fazziclay.opentoday.ui.interfaces.NavigationHost;
import ru.fazziclay.opentoday.util.L;

public class ItemsEditorRootFragment extends Fragment implements NavigationHost {
    private static final int ROOT_CONTAINER_ID = R.id.changeOnLeftSwipe;
    private static final String EXTRA_TAB_ID = "items_editor_root_fragment_tabId";
    private static final String TAG = "ItemsEditorRootFragment";
    private TextView path;

    @NonNull
    public static ItemsEditorRootFragment create(@NonNull UUID tabId) {
        ItemsEditorRootFragment fragment = new ItemsEditorRootFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TAB_ID, tabId.toString());
        fragment.setArguments(args);
        return fragment;
    }

    private UUID tabId;
    private Tab tab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.o(TAG, "onCreate", L.nn(savedInstanceState));

        Bundle args = getArguments();
        tabId = UUID.fromString(args.getString(EXTRA_TAB_ID));
        tab = App.get(requireContext()).getItemManager().getTab(tabId);

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(ROOT_CONTAINER_ID, ItemsEditorFragment.createRoot(tabId))
                    .commit();

            getChildFragmentManager().addOnBackStackChangedListener(() -> {
                updatePath();
                triggerUpdateISToCurrent();
            });
            getChildFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> triggerUpdateISToCurrent());
            updateItemStorageContext(tab);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        L.o(TAG, "onCreateView", L.nn(savedInstanceState));
        path = new TextView(requireContext());
        updatePath();

        FrameLayout frameLayout = new FrameLayout(requireContext());
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.setId(ROOT_CONTAINER_ID);

        LinearLayout l = new LinearLayout(requireContext());
        l.setOrientation(LinearLayout.VERTICAL);
        l.addView(path);
        l.addView(frameLayout);

        return l;
    }

    private String getPath() {
        final String PATH_SEPARATOR = " / ";
        StringBuilder s = new StringBuilder(PATH_SEPARATOR);
        int i = 0;
        while (i < getChildFragmentManager().getBackStackEntryCount()) {
            s.append(getChildFragmentManager().getBackStackEntryAt(i).getName().split("\n")[0]).append(PATH_SEPARATOR);
            i++;
        }
        return s.toString().trim();
    }

    private void updatePath() {
        path.setText(getPath());
    }

    @Override
    public void navigate(Fragment fragment, boolean addToBackStack) {
        L.o(TAG, "navigate", "to=", fragment, "back=", addToBackStack);
        if (!(fragment instanceof ItemsEditorFragment)) throw new RuntimeException("Other fragments not allowed.");
        ItemsEditorFragment ief = (ItemsEditorFragment) fragment;

        FragmentTransaction transaction = getChildFragmentManager()
                .beginTransaction()
                .replace(ROOT_CONTAINER_ID, ief);

        String backStackName = null;
        UUID itemId = ief.getItemIdFromExtra();
        Item item = tab.getItemById(itemId);
        if (item != null) {
            backStackName = item.getText().split("\n")[0];
            updateItemStorageContext((ItemsStorage) item);
        }

        if (addToBackStack) transaction.addToBackStack(backStackName);
        transaction.commit();
    }

    @Override
    public boolean popBackStack() {
        L.o(TAG, "popBackStack");

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            updateItemStorageContext(getCurrentItemsStorage());
            return true;
        }
        return false;
    }

    public ItemsStorage getCurrentItemsStorage() {
        ItemsEditorFragment fragment = (ItemsEditorFragment) getChildFragmentManager().findFragmentById(ROOT_CONTAINER_ID);
        if (fragment == null) return null;
        return fragment.getItemStorage();
    }

    private void updateItemStorageContext(ItemsStorage itemsStorage) {
        ItemsTabIncludeFragment f = ((ItemsTabIncludeFragment) getParentFragment());
        if (f == null) {
            L.o(TAG, "updateItemStorageContext", "parent fragment is null!!!!");
            return;
        }
        f.setItemStorageInContext(itemsStorage);
    }

    public void triggerUpdateISToCurrent() {
        if (getCurrentItemsStorage() != null) updateItemStorageContext(getCurrentItemsStorage());
    }

    public void toRoot() {
        FragmentManager fm = getChildFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }
}
