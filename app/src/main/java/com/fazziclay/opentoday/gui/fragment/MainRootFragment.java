package com.fazziclay.opentoday.gui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.gui.interfaces.ContainBackStack;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.util.L;

public class MainRootFragment extends Fragment implements NavigationHost {
    private static final int CONTAINER_ID = R.id.content;
    private static final String TAG = "MainRootFragment";

    public static MainRootFragment create() {
        return new MainRootFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.o(TAG, "onCreate", L.nn(savedInstanceState));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        L.o(TAG, "onCreateView", "sis=", L.nn(savedInstanceState));
        FrameLayout frameLayout = new FrameLayout(requireContext());
        frameLayout.setId(CONTAINER_ID);
        return frameLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        L.o(TAG, "onViewCreated", "sis=", L.nn(savedInstanceState));
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(CONTAINER_ID, ItemsTabIncludeFragment.create())
                    .commit();
        }
    }

    @Override
    public boolean popBackStack() {
        L.o(TAG, "popBackStack");
        Fragment currentFragment = getChildFragmentManager().findFragmentById(CONTAINER_ID);
        if (currentFragment instanceof ContainBackStack) {
            ContainBackStack currentFragmentBackStack = (ContainBackStack) currentFragment;
            if (currentFragmentBackStack.popBackStack()) {
                return true;
            }
        }

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            return true;
        }
        return false;
    }

    @Override
    public void navigate(Fragment fragment, boolean addToBackStack) {
        L.o(TAG, "navigate", "to=", fragment, "back=", addToBackStack);
        FragmentTransaction transaction = getChildFragmentManager()
                .beginTransaction()
                .replace(CONTAINER_ID, fragment);

        if (addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }
}
