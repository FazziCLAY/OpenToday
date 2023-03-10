package com.fazziclay.opentoday.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.app.receiver.ItemsTickReceiver;
import com.fazziclay.opentoday.gui.fragment.MainRootFragment;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;

import java.util.UUID;

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

    public static class Debug {
        public static void showPersonalTickDialog(Context context) {
            EditText view = new EditText(context);
            new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton("TICK", (dfsd, fdsg) -> {
                        // TODO: 3/10/23 review
                        try {
                            UUID id = UUID.fromString(view.getText().toString());
                            context.sendBroadcast(new Intent(context, ItemsTickReceiver.class).putExtra(ItemsTickReceiver.EXTRA_PERSONAL_TICK, new String[]{id.toString()}).putExtra("debugMessage", "Debug personal tick is work!"));
                        } catch (Exception e) {
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
    }
}
