package ru.fazziclay.opentoday.app.receiver.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import ru.fazziclay.opentoday.app.receiver.ItemsTickReceiver;

// Тик во время активного интерфейса приложения (раз в секунду) для того что бы изменения
// Были видры резче
public class UITickService extends Service {
    private final Handler handler;
    private final Runnable runnable = this::tick;

    public UITickService() {
        this.handler = new Handler(Looper.myLooper());
    }

    public void tick() {
        sendBroadcast(new Intent(this, ItemsTickReceiver.class));
        this.handler.removeCallbacks(runnable);
        long millis = System.currentTimeMillis() % 1000;
        this.handler.postDelayed(runnable, 1000 - millis);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(runnable);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
