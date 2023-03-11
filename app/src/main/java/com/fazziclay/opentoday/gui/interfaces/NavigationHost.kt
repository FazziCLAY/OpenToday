package com.fazziclay.opentoday.gui.interfaces;

import androidx.fragment.app.Fragment;

public interface NavigationHost extends BackStackMember {
    void navigate(Fragment fragment, boolean addToBackStack);
}
