package com.fazziclay.opentoday.app.items.tick;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ItemsTickReceiver extends BroadcastReceiver {
    private static final String TAG = "ItemsTickReceiver";
    public static final String EXTRA_PERSONAL_TICK = "personalTick";
    public static final String EXTRA_PERSONAL_TICK_MODE = "personalTickMode";

    public static Intent createIntent(Context context) {
        return new Intent(context, ItemsTickReceiver.class);
    }

    public static Intent createIntent(Context context, UUID firstItem, boolean usePaths) {
        return createIntent(context, new String[]{Objects.toString(firstItem)}, usePaths);
    }

    public static Intent createIntent(Context context, String[] array, boolean usePaths) {
        return new Intent(context, ItemsTickReceiver.class)
                .putExtra(EXTRA_PERSONAL_TICK, array)
                .putExtra(EXTRA_PERSONAL_TICK_MODE, (usePaths ? PersonalTickMode.PERSONAL_AND_PATH : PersonalTickMode.PERSONAL_ONLY).name());
    }

    public static Intent createNotificationTriggerIntent(Context context, ItemNotification itemNotification) {
        final Item item = itemNotification.getParentItem();
        return createIntent(context, item.getId(), true);
        // TODO: 22.10.2023 currently while item tick ticked all item notifications! maybe need specify by notify id?
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "onReceive(...)");
        final App app = App.get(context);
        if (app == null) return;

        final TickThread tickThread = app.getTickThread();

        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("debugMessage")) {
            String s = intent.getExtras().getString("debugMessage", "none");
            if (Debug.SHOW_DEBUG_MESSAGE_TOAST_IN_ITEMSTICKRECEIVER) {
                Toast.makeText(context, "ItemsTickReceive: " + s, Toast.LENGTH_SHORT).show();
            }
            Logger.d("ItemsTickReceiver", "DebugMessage! " + s);
        }

        debugNotification(context);

        boolean isPersonalTick = (intent != null && (intent.getExtras() != null && intent.getExtras().containsKey(EXTRA_PERSONAL_TICK)));
        if (isPersonalTick) {
            PersonalTickMode personalTickMode;
            try {
                personalTickMode = PersonalTickMode.valueOf(intent.getExtras().getString(EXTRA_PERSONAL_TICK_MODE));
            } catch (Exception e) {
                Logger.e(TAG,"PersonalTickMode unspecified by extras. Set to PERSONAL_ONLY", e);
                personalTickMode = PersonalTickMode.PERSONAL_ONLY;
            }
            String[] temp = intent.getExtras().getStringArray(EXTRA_PERSONAL_TICK);
            List<UUID> uuids = new ArrayList<>();
            for (String s : temp) {
                uuids.add(UUID.fromString(s));
            }
            tickThread.requestPersonalTick(uuids, personalTickMode.isUsePaths());
        } else {
            tickThread.requestTick();
        }
    }

    private void debugNotification(Context context) {
        if (Debug.DEBUG_TICK_NOTIFICATION) {
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

    public enum PersonalTickMode {
        PERSONAL_ONLY(false),
        PERSONAL_AND_PATH(true)
        ;

        private final boolean usePaths;

        PersonalTickMode(boolean usePaths) {
            this.usePaths = usePaths;
        }

        public boolean isUsePaths() {
            return usePaths;
        }
    }
}
