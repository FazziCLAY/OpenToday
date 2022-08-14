package ru.fazziclay.opentoday.app.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.settings.SettingsManager;

public class ItemsTickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        App app = App.get(context);
        SettingsManager settingsManager = app.getSettingsManager();
        ItemManager itemManager = app.getItemManager();

        if (App.DEBUG_TICK_NOTIFICATION) {
            context.getSystemService(NotificationManager.class).notify(321, new NotificationCompat.Builder(context, App.NOTIFICATION_ITEMS_CHANNEL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setSilent(true)
                    .setContentTitle("Tick...")
                    .setSound(null)
                    .setShowWhen(false)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build());
        }

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        Calendar currentDay = new GregorianCalendar(
                gregorianCalendar.get(Calendar.YEAR),
                gregorianCalendar.get(Calendar.MONTH),
                gregorianCalendar.get(Calendar.DAY_OF_MONTH));
        int daySeconds = (int) ((gregorianCalendar.getTimeInMillis() - currentDay.getTimeInMillis()) / 1000);

        TickSession tickSession = new TickSession(context, gregorianCalendar, daySeconds);
        itemManager.tick(tickSession);
        if (tickSession.isSaveNeeded()) {
            itemManager.saveAllDirect();
        }
    }
}
