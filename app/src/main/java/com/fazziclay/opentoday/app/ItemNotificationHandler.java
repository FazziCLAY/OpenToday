package com.fazziclay.opentoday.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.app.items.tick.ItemsTickReceiver;
import com.fazziclay.opentoday.gui.activity.AlarmActivity;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.RandomUtil;

public class ItemNotificationHandler {
    private static boolean exceptionOnce = true;

    private final Context context;
    private final App app;

    public ItemNotificationHandler(Context context, App app) {
        this.context = context;
        this.app = app;
    }

    public void setAlarm(ItemNotification itemNotification, long triggerAtMs) {
        if (!itemNotification.isAttached()) {
            throw new IllegalArgumentException("setAlarm required attached ItemNotification for working...");
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = flags | PendingIntent.FLAG_IMMUTABLE;
        }

        try {
            var intent = ItemsTickReceiver.createNotificationTriggerIntent(context, itemNotification);

            var pendingIntent = PendingIntent.getBroadcast(context,
                    itemNotification.getId().hashCode(),
                    intent,
                    flags);

            final AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);

        } catch (Exception e) {
            if (exceptionOnce) {
                App.exception(null, e);
                exceptionOnce = false;
            }
        }
    }

    public void handle(Item item, ItemNotification notification) {
        if (notification instanceof DayItemNotification dayItemNotification) {
            final String nTitle = dayItemNotification.isNotifyTitleFromItemText() ? (item.isParagraphColorize() ? ColorUtil.colorizeToPlain(item.getText()) : item.getText()) : dayItemNotification.getNotifyTitle();
            final String nText = dayItemNotification.isNotifyTextFromItemText() ? (item.isParagraphColorize() ? ColorUtil.colorizeToPlain(item.getText()) : item.getText()) : dayItemNotification.getNotifyText();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.NOTIFICATION_ITEMS_CHANNEL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(nTitle)
                    .setContentText(nText)
                    .setSubText(dayItemNotification.getNotifySubText())
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            if (dayItemNotification.isFullScreen()) {
                PendingIntent pending = PendingIntent.getActivity(context,
                        RandomUtil.nextInt(),
                        AlarmActivity.createIntent(context,
                                item.getId(),
                                dayItemNotification.isPreRenderPreviewMode(),
                                nTitle,
                                dayItemNotification.isSound(),
                                dayItemNotification.getNotificationId()),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);
                builder.setFullScreenIntent(pending, true);
            }
            builder.setUsesChronometer(true);

            context.getSystemService(NotificationManager.class).notify(dayItemNotification.getNotificationId(), builder.build());
        }
    }
}
