package ru.fazziclay.opentoday.app;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Random;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.ItemManager;

public class TickService extends Service {
    private ItemManager itemManager;

    @Override
    public void onCreate() {
        itemManager = App.get(this).getItemManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        itemManager.tick();
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
