package ru.fazziclay.opentoday.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.fazziclay.opentoday.ui.fragment.MainRootFragment;

public class UI {
    @Nullable
    public static Fragment findFragmentInParents(@NonNull androidx.fragment.app.Fragment fragment, Class<? extends androidx.fragment.app.Fragment> find) {
        if (fragment.getParentFragment() == null) {
            return null;
        }
        if (fragment.getParentFragment().getClass() == find) {
            return fragment.getParentFragment();
        } else {
            return findFragmentInParents(fragment.getParentFragment(), find);
        }
    }

    public static void back(Fragment fragment) {
        MainRootFragment host = (MainRootFragment) UI.findFragmentInParents(fragment, MainRootFragment.class);
        host.popBackStack();
    }
}
