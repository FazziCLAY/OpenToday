package com.fazziclay.opentoday.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.fazziclay.opentoday.app.receiver.ItemsTickReceiver;

public class UITickService {
    private final Context context;
    private final Handler handler;
    private final Runnable runnable = this::tick;

    public UITickService(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.myLooper());
    }

    public void tick() {
        context.sendBroadcast(new Intent(context, ItemsTickReceiver.class));
        this.handler.removeCallbacks(runnable);
        long millis = System.currentTimeMillis() % 1000;
        this.handler.postDelayed(runnable, 1000 - millis);
    }

    public void create() {
        handler.post(runnable);
    }

    public void destroy() {
        handler.removeCallbacks(runnable);
    }
}
