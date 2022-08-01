package ru.fazziclay.opentoday.app;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.ItemManager;

public class MainService extends Service {
    private Handler handler;
    private Runnable runnable;

    private ItemManager itemManager;

    @Override
    public void onCreate() {
        super.onCreate();

        startForeground(App.NOTIFICATION_FOREGROUND_ID, new NotificationCompat.Builder(this, App.NOTIFICATION_FOREGROUND_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSilent(true)
                .setSound(null)
                .setShowWhen(false)
                .setContentTitle(getString(R.string.notification_foreground_title))
                .setContentText(getString(R.string.notification_foreground_text))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build());

        App app = App.get(this);
        itemManager = app.getItemManager();
        handler = new Handler(getMainLooper());
        runnable = () -> {
            itemManager.tick();
            handler.postDelayed(runnable, 1000);
        };
        handler.post(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}