package com.fazziclay.opentoday.app.items.notification;

import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.tick.TickSession;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.item.Item;

import java.util.Calendar;

public class DayItemNotification implements ItemNotification {
    public static final ItemNotificationCodec CODEC = new Codec();
    private static class Codec extends ItemNotificationCodec {
        @Override
        public Cherry exportNotification(ItemNotification itemNotification) {
            DayItemNotification d = (DayItemNotification) itemNotification;
            return new Cherry()
                    .put("notificationId", d.notificationId)
                    .put("notifyTitle", d.notifyTitle)
                    .put("notifyTitleFromItemText", d.notifyTitleFromItemText)
                    .put("notifyText", d.notifyText)
                    .put("notifyTextFromItemText", d.notifyTextFromItemText)
                    .put("latestDayOfYear", d.latestDayOfYear)
                    .put("notifySubText", d.notifySubText)
                    .put("time", d.time);
        }

        @Override
        public ItemNotification importNotification(Cherry cherry) {
            DayItemNotification o = new DayItemNotification();
            o.notificationId = cherry.optInt("notificationId", 543);
            o.notifyTitle = cherry.optString("notifyTitle", "");
            o.notifyTitleFromItemText = cherry.optBoolean("notifyTitleFromItemText", true);
            o.notifyText = cherry.optString("notifyText", "");
            o.notifyTextFromItemText = cherry.optBoolean("notifyTextFromItemText", true);
            o.notifySubText = cherry.optString("notifySubText", "");
            o.latestDayOfYear = cherry.optInt("latestDayOfYear", 0);
            o.time = cherry.optInt("time", 0);

            return o;
        }
    }

    private int notificationId = 0;
    private String notifyTitle;
    private boolean notifyTitleFromItemText = false;
    private String notifyText;
    private boolean notifyTextFromItemText = false;
    private String notifySubText;
    private int latestDayOfYear;
    private int time;

    public DayItemNotification() {

    }

    public DayItemNotification(int notificationId, String notifyTitle, String notifyText, String notifySubText, int time) {
        this.notificationId = notificationId;
        this.notifyTitle = notifyTitle;
        this.notifyText = notifyText;
        this.notifySubText = notifySubText;
        this.time = time;
    }

    @Override
    public boolean tick(TickSession tickSession, Item item) {
        tickSession.setAlarmDayOfTimeInSeconds(time, item);

        int dayOfYear = tickSession.getGregorianCalendar().get(Calendar.DAY_OF_YEAR);
        if (dayOfYear != latestDayOfYear) {
            boolean isTime = tickSession.getDayTime() >= time;

            if (isTime) {
                sendNotify(tickSession.getContext(), item);
                latestDayOfYear = dayOfYear;
                tickSession.saveNeeded();
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public DayItemNotification clone() {
        try {
            return (DayItemNotification) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone exception", e);
        }
    }

    public void sendNotify(Context context, Item item) {
        final String nTitle = notifyTitleFromItemText ? item.getText() : notifyTitle;
        final String nText = notifyTextFromItemText ? item.getText() : notifyText;

        context.getSystemService(NotificationManager.class).notify(this.notificationId,
                new NotificationCompat.Builder(context, App.NOTIFICATION_ITEMS_CHANNEL)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(nTitle)
                        .setContentText(nText)
                        .setSubText(notifySubText)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build());
    }

    public int getLatestDayOfYear() {
        return latestDayOfYear;
    }

    public void setLatestDayOfYear(int latestDayOfYear) {
        this.latestDayOfYear = latestDayOfYear;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getNotifyTitle() {
        return notifyTitle;
    }

    public void setNotifyTitle(String notifyTitle) {
        this.notifyTitle = notifyTitle;
    }

    public String getNotifyText() {
        return notifyText;
    }

    public void setNotifyText(String notifyText) {
        this.notifyText = notifyText;
    }

    public String getNotifySubText() {
        return notifySubText;
    }

    public void setNotifySubText(String notifySubText) {
        this.notifySubText = notifySubText;
    }

    public boolean isNotifyTitleFromItemText() {
        return notifyTitleFromItemText;
    }

    public boolean isNotifyTextFromItemText() {
        return notifyTextFromItemText;
    }

    public void setNotifyTitleFromItemText(boolean notifyTitleFromItemText) {
        this.notifyTitleFromItemText = notifyTitleFromItemText;
    }

    public void setNotifyTextFromItemText(boolean notifyTextFromItemText) {
        this.notifyTextFromItemText = notifyTextFromItemText;
    }
}
