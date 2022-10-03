package ru.fazziclay.opentoday.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import ru.fazziclay.opentoday.app.receiver.ItemsTickReceiver;

// Тик во время активного интерфейса приложения (раз в секунду) для того что бы изменения
// Были видры резче
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
