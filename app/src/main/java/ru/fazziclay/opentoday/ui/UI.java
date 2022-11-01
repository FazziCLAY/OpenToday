package ru.fazziclay.opentoday.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.fazziclay.opentoday.ui.fragment.MainRootFragment;
import ru.fazziclay.opentoday.ui.interfaces.NavigationHost;

public class UI {
    @Nullable
    public static Fragment findFragmentInParents(@NonNull final Fragment fragment, @NonNull final Class<? extends Fragment> find) {
        if (fragment.getParentFragment() == null) {
            return null;
        }
        if (fragment.getParentFragment().getClass() == find) {
            return fragment.getParentFragment();
        } else {
            return findFragmentInParents(fragment.getParentFragment(), find);
        }
    }

    public static void back(@NonNull final Fragment fragment) {
        if (fragment == null) throw new NullPointerException("Fragment is null!");
        final MainRootFragment host = (MainRootFragment) UI.findFragmentInParents(fragment, MainRootFragment.class);
        if (host == null) throw new RuntimeException("fragment is not in MainRootFragment tree!");
        host.popBackStack();
    }

    public static void navigate(@NonNull final NavigationHost navigationHost, @NonNull final Fragment fragment, final boolean addToBackStack) {
        navigationHost.navigate(fragment, addToBackStack);
    }
}
