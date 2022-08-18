package ru.fazziclay.opentoday.app.items.notifications;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.util.Calendar;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.TickSession;

public class DayItemNotification implements ItemNotification {
    public static final ItemNotificationIETool IE_TOOL = new IeTool();
    private static class IeTool extends ItemNotificationIETool {
        @Override
        public JSONObject exportNotification(ItemNotification itemNotification) throws Exception {
            DayItemNotification d = (DayItemNotification) itemNotification;
            return new JSONObject()
                    .put("notificationId", d.notificationId)
                    .put("notifyTitle", d.notifyTitle)
                    .put("notifyText", d.notifyText)
                    .put("latestDayOfYear", d.latestDayOfYear)
                    .put("notifySubText", d.notifySubText)
                    .put("time", d.time);
        }

        @Override
        public ItemNotification importNotification(JSONObject json) {
            DayItemNotification o = new DayItemNotification();
            o.notificationId = json.optInt("notificationId", 543);
            o.notifyTitle = json.optString("notifyTitle", "owo");
            o.notifyText = json.optString("notifyText", "owo");
            o.notifySubText = json.optString("notifySubText", "owo");
            o.latestDayOfYear = json.optInt("latestDayOfYear", 0);
            o.time = json.optInt("time", 0);

            return o;
        }
    }

    private int notificationId = 0;
    private String notifyTitle;
    private String notifyText;
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
    public boolean tick(TickSession tickSession) {
        int dayofy = tickSession.getGregorianCalendar().get(Calendar.DAY_OF_YEAR);

        if (dayofy != latestDayOfYear) {
            if (tickSession.getDayTime() >= time) {
                sendNotify(tickSession.getContext());
                latestDayOfYear = dayofy;
                tickSession.saveNeeded();
                return true;
            }
        }
        return false;
    }

    public void sendNotify(Context context) {
        context.getSystemService(NotificationManager.class).notify(this.notificationId,
                new NotificationCompat.Builder(context, App.NOTIFICATION_ITEMS_CHANNEL)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(notifyTitle)
                        .setContentText(notifyText)
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
}
