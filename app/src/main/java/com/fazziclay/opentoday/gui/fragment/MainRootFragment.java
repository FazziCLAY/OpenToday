package com.fazziclay.opentoday.gui.fragment;

import static com.fazziclay.opentoday.util.InlineUtil.nullStat;

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
import com.fazziclay.opentoday.gui.interfaces.BackStackMember;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.util.Logger;

public class MainRootFragment extends Fragment implements NavigationHost {
    private static final int CONTAINER_ID = R.id.content;
    private static final String TAG = "MainRootFragment";

    public static MainRootFragment create() {
        return new MainRootFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate", nullStat(savedInstanceState));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView inflater=", nullStat(inflater), "container=", nullStat(container), "savedInstanceState", nullStat(savedInstanceState));
        FrameLayout frameLayout = new FrameLayout(requireContext());
        frameLayout.setId(CONTAINER_ID);
        return frameLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated", nullStat(savedInstanceState));

        if (savedInstanceState == null) {
            Logger.d(TAG, "onViewCreated", "fragment replaced", ItemsTabIncludeFragment.class.getCanonicalName());
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(CONTAINER_ID, ItemsTabIncludeFragment.create())
                    .commit();
        }
    }

    @Override
    public boolean popBackStack() {
        Logger.d(TAG, "popBackStack");
        Fragment currentFragment = getChildFragmentManager().findFragmentById(CONTAINER_ID);
        if (currentFragment instanceof BackStackMember) {
            Logger.d(TAG, "popBackStack", "current fragment is BackStackMember!");
            BackStackMember t = (BackStackMember) currentFragment;
            if (t.popBackStack()) {
                return true;
            }
        }

        Logger.d(TAG, "popBackStack", "pop internal");
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    @Override
    public void navigate(Fragment fragment, boolean addToBackStack) {
        Logger.d(TAG, "navigate to=", fragment, "addToBack=", addToBackStack);
        FragmentTransaction transaction = getChildFragmentManager()
                .beginTransaction()
                .replace(CONTAINER_ID, fragment);

        if (addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }
}
