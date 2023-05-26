package com.fazziclay.opentoday.app.items.tick;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ItemsTickReceiver extends BroadcastReceiver {
    public static final String EXTRA_PERSONAL_TICK = "personalTick";

    public static Intent createIntent(Context context) {
        return new Intent(context, ItemsTickReceiver.class);
    }

    public static Intent createIntent(Context context, UUID firstItem) {
        return createIntent(context, new String[]{Objects.toString(firstItem)});
    }

    public static Intent createIntent(Context context, String[] array) {
        return new Intent(context, ItemsTickReceiver.class)
                .putExtra(EXTRA_PERSONAL_TICK, array);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final App app = App.get(context);
        if (app == null) return;

        final TickThread tickThread = app.getTickThread();

        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("debugMessage")) {
            String s = intent.getExtras().getString("debugMessage", "none");
            //Toast.makeText(context, "ItemsTickReceive: " + s, Toast.LENGTH_SHORT).show();
            Logger.d("ItemsTickReceiver", "DebugMessage! " + s);
        }

        debugNotification(context);

        boolean personalMode = (intent != null && (intent.getExtras() != null && intent.getExtras().containsKey(EXTRA_PERSONAL_TICK)));
        if (personalMode) {
            String[] temp = intent.getExtras().getStringArray(EXTRA_PERSONAL_TICK);
            List<UUID> uuids = new ArrayList<>();
            for (String s : temp) {
                uuids.add(UUID.fromString(s));
            }
            tickThread.requestTick(uuids);
        } else {
            tickThread.requestTick();
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
}
