package ru.fazziclay.opentoday.app.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.TickSession;
import ru.fazziclay.opentoday.app.items.ItemManager;

public class ItemsTickReceiver extends BroadcastReceiver {
    public static final String EXTRA_PERSONAL_TICK = "personalTick";

    @Override
    public void onReceive(Context context, Intent intent) {
        App app = App.get(context);
        if (app == null) return;
        ItemManager itemManager = app.getItemManager();
        if (itemManager == null) return;

        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("debugMessage")) {
            String s = intent.getExtras().getString("debugMessage", "none");
            Log.d("ItemsTickReceiver", "DebugMessage! " + s);
        }

        debugNotification(context);

        TickSession tickSession = createTickSession(context);
        boolean personalMode = (intent != null && (intent.getExtras() != null && intent.getExtras().containsKey(EXTRA_PERSONAL_TICK)));
        if (personalMode) {
            String[] temp = intent.getExtras().getStringArray(EXTRA_PERSONAL_TICK);
            List<UUID> uuids = new ArrayList<>();
            for (String s : temp) {
                uuids.add(UUID.fromString(s));
            }
            itemManager.tick(tickSession, uuids);
        } else {
            itemManager.tick(tickSession);
        }

        // Post tick commands
        if (tickSession.isSaveNeeded()) {
            itemManager.saveAllDirect();
        }
    }

    private void debugNotification(Context context) {
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
    }

    private TickSession createTickSession(Context context) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();

        // START - day seconds
        GregorianCalendar noTimeCalendar = new GregorianCalendar(
                gregorianCalendar.get(Calendar.YEAR),
                gregorianCalendar.get(Calendar.MONTH),
                gregorianCalendar.get(Calendar.DAY_OF_MONTH));
        int daySeconds = (int) ((gregorianCalendar.getTimeInMillis() - noTimeCalendar.getTimeInMillis()) / 1000);
        // END - day seconds

        return new TickSession(context, gregorianCalendar, noTimeCalendar, daySeconds);
    }
}
