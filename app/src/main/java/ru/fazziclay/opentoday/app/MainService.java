package ru.fazziclay.opentoday.app;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import ru.fazziclay.opentoday.R;

public class MainService extends Service {
    private App app;
    private Handler handler;
    private Runnable runnable;

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

        this.app = App.get(this);
        this.handler = new Handler(Looper.myLooper());
        this.runnable = () -> {
            startService(new Intent(this, TickService.class));
            this.handler.removeCallbacks(runnable);
            this.handler.postDelayed(runnable, app.isAppInForeground() ? 1000 : 60000);
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.handler.post(this.runnable);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.handler.removeCallbacks(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}