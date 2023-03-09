package com.fazziclay.opentoday.gui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.gui.fragment.MainRootFragment;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;

public class UI {
    @Nullable
    public static <T extends Fragment> T findFragmentInParents(@NonNull final Fragment fragment, @NonNull final Class<T> find) {
        if (fragment == null) throw new NullPointerException("Fragment is null!");
        if (find == null) throw new NullPointerException("Find is null!");

        Fragment parent = fragment.getParentFragment();
        while (true) {
            if (parent == null) {
                return null;
            }
            if (parent.getClass() == find) {
                return (T) parent;
            }
            parent = parent.getParentFragment();
        }
    }

    public static void rootBack(@NonNull final Fragment fragment) {
        if (fragment == null) throw new NullPointerException("Fragment is null!");
        final MainRootFragment host = UI.findFragmentInParents(fragment, MainRootFragment.class);
        if (host == null) throw new RuntimeException("Fragment is not contains MainRootFragment in parents!");
        host.popBackStack();
    }

    public static void navigate(@NonNull final NavigationHost navigationHost, @NonNull final Fragment fragment, final boolean addToBackStack) {
        navigationHost.navigate(fragment, addToBackStack);
    }
}
