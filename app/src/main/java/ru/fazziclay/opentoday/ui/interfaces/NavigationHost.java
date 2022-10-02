package ru.fazziclay.opentoday.ui.interfaces;

import androidx.fragment.app.Fragment;

public interface NavigationHost extends ContainBackStack {
    void navigate(Fragment fragment, boolean addToBackStack);
}
