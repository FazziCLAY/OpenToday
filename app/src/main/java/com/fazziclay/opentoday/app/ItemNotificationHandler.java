package com.fazziclay.opentoday.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.app.items.tick.ItemsTickReceiver;
import com.fazziclay.opentoday.gui.activity.AlarmActivity;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.util.HashMap;

public class ItemNotificationHandler {
    private static final String TAG = "ItemNotificationHandler";
    private static boolean exceptionOnce = true;

    private final Context context;
    private final App app;

    private final HashMap<Integer, Long> cachedAlarms = new HashMap<>();

    public ItemNotificationHandler(Context context, App app) {
        this.context = context;
        this.app = app;
    }

    public void setAlarm(ItemNotification itemNotification, long triggerAtMs) {
        if (!itemNotification.isAttached()) {
            throw new IllegalArgumentException("setAlarm required attached ItemNotification for working...");
        }
        int requestId = itemNotification.getId().hashCode();

        boolean isCached = isCachedAlarm(requestId, triggerAtMs);
        if (isCached) {
            return;
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = flags | PendingIntent.FLAG_IMMUTABLE;
        }

        try {
            var intent = ItemsTickReceiver.createNotificationTriggerIntent(context, itemNotification);
            var pendingIntent = PendingIntent.getBroadcast(context,
                    requestId,
                    intent,
                    flags);

            final AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
            // owoo
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);

                    } else {
                        Logger.w(TAG, "alarm canScheduleExactAlarms=false. for " + itemNotification + " to " + triggerAtMs + "unix-ms: " + TimeUtil.getDebugDate(triggerAtMs));
                        return;
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
                }
            }
            cachedAlarms.put(requestId, triggerAtMs);
            Logger.d(TAG, "alarm set for " + itemNotification + " to " + triggerAtMs + "unix-ms: " + TimeUtil.getDebugDate(triggerAtMs));

        } catch (Exception e) {
            if (exceptionOnce) {
                App.exception(null, e);
                exceptionOnce = false;
            }
        }
    }

    private boolean isCachedAlarm(int requestId, long triggerAtMs) {
        if (cachedAlarms.containsKey(requestId)) {
            return cachedAlarms.get(requestId) == triggerAtMs;
        }
        return false;
    }

    public void handle(Item item, ItemNotification notification) {
        if (notification instanceof DayItemNotification dayItemNotification) {
            final String nTitle = dayItemNotification.isNotifyTitleFromItemText() ? (item.isParagraphColorize() ? ColorUtil.colorizeToPlain(item.getText()) : item.getText()) : dayItemNotification.getNotifyTitle();
            final String nText = dayItemNotification.isNotifyTextFromItemText() ? (item.isParagraphColorize() ? ColorUtil.colorizeToPlain(item.getText()) : item.getText()) : dayItemNotification.getNotifyText();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.NOTIFICATION_ITEMS_CHANNEL)
                    .setSmallIcon(dayItemNotification.getIcon().getResId())
                    .setContentTitle(nTitle)
                    .setContentText(nText)
                    .setSubText(dayItemNotification.getNotifySubText())
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            if (notification.getColor() != ItemNotification.DEFAULT_COLOR) {
                builder.setColor(notification.getColor());
            }

            if (dayItemNotification.isFullScreen()) {
                PendingIntent pending = PendingIntent.getActivity(context,
                        RandomUtil.nextInt(),
                        AlarmActivity.createIntent(context,
                                item.getId(),
                                dayItemNotification.isPreRenderPreviewMode(),
                                nTitle,
                                dayItemNotification.isSound(),
                                dayItemNotification.getNotificationId()),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setFullScreenIntent(pending, true);
                builder.setCategory(NotificationCompat.CATEGORY_ALARM);
            }
            builder.setUsesChronometer(true);
            NotificationManager systemService = context.getSystemService(NotificationManager.class);
            if (systemService.areNotificationsEnabled()) {
                systemService.notify(dayItemNotification.getNotificationId(), builder.build());
            }
        }
    }
}
