package com.fazziclay.opentoday.gui;

import android.view.View;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class UINotification {
    public static final long DURATION_PERMANENT = -1;
    
    private final View view;
    private final long duration;
    private Runnable onEnded;
    private Runnable remove;

    public UINotification(View view, int duration) {
        this.view = view;
        this.duration = duration;
    }

    public static UINotification create(View view, int duration) {
        return new UINotification(view, duration);
    }
    
    public UINotification setEndCallback(Runnable r) {
        this.onEnded = r;
        return this;
    }

    public View getView() {
        return view;
    }

    public long getDuration() {
        return duration;
    }

    public void attach(@NotNull Function0<Unit> function) {
        this.remove = function::invoke;
    }

    public void remove() {
        if (this.remove != null) this.remove.run();
        if (this.onEnded != null) this.onEnded.run();
    }
}
