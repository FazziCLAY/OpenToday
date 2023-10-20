package com.fazziclay.opentoday.gui.item;

import com.fazziclay.opentoday.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class HolderDestroyer {
    private static final String TAG = "HolderDestroyer";

    private final List<Runnable> listeners = new ArrayList<>();
    private boolean destroyed = false;

    public void addDestroyListener(Runnable runnable) {
        listeners.add(runnable);
    }

    public void destroy() {
        if (!destroyed) {
            for (Runnable listener : listeners) {
                listener.run();
            }
            destroyed = true;
        } else {
            Logger.d(TAG, "destroy() called in already destroyed!");
        }
    }
}
