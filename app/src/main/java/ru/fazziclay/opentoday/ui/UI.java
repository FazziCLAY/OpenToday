package ru.fazziclay.opentoday.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class UI {
    @Nullable
    public static Fragment findFragmentInParents(@NonNull Fragment fragment, Class<? extends Fragment> find) {
        if (fragment.getParentFragment() == null) {
            return null;
        }
        if (fragment.getParentFragment().getClass() == find) {
            return fragment.getParentFragment();
        } else {
            return findFragmentInParents(fragment.getParentFragment(), find);
        }
    }
}
