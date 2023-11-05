package com.fazziclay.opentoday.gui.item;

import com.fazziclay.opentoday.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class Destroyer {
    private static final String TAG = "Destroyer";

    private final List<Runnable> listeners = new ArrayList<>();
    private boolean destroyed = false;

    public void add(final Runnable runnable) {
        listeners.add(runnable);
    }

    public void destroy() {
        if (!destroyed) {
            for (final Runnable listener : listeners) {
                listener.run();
            }
            destroyed = true;
        } else {
            Logger.w(TAG, "destroy() called when it already destroyed!");
        }
    }

    public void recycle() {
        destroyed = false;
        listeners.clear();
    }
}
