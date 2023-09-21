package com.fazziclay.opentoday.app.items.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.gui.activity.AlarmActivity;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.RandomUtil;

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
                    .put("time", d.time)
                    .put("isPreRenderPreviewMode", d.isPreRenderPreviewMode)
                    .put("sound", d.sound)
                    .put("fullScreen", d.fullScreen);

                    //- (WARNING) "isVibrate" & "vibration" key removed since 1.0.4 version. Don't forget handle maybe-oldest-values in DataFixer if re-add this keys.
                    //- .put("isVibrate", false)
                    //- .put("vibration", CherryOrchard.of(new JSONArray()));
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
            o.isPreRenderPreviewMode = cherry.optBoolean("isPreRenderPreviewMode", true);
            o.sound = cherry.optBoolean("sound", true);
            o.fullScreen = cherry.optBoolean("fullScreen", true);
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

    private boolean fullScreen = true;
    private boolean isPreRenderPreviewMode = true;
    private boolean sound = true;

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
        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_NOTIFICATION_SCHEDULE)) tickSession.setAlarmDayOfTimeInSeconds(time, item);
        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_NOTIFICATION_UPDATE)) {
            int dayOfYear = tickSession.getGregorianCalendar().get(Calendar.DAY_OF_YEAR);
            if (dayOfYear != latestDayOfYear) {
                boolean isTimeToSend = tickSession.getDayTime() >= time;

                if (isTimeToSend) {
                    sendNotify(tickSession.getContext(), item);
                    latestDayOfYear = dayOfYear;
                    tickSession.saveNeeded();
                    tickSession.importantSaveNeeded();
                    return true;
                }
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
        final String nTitle = notifyTitleFromItemText ? (item.isParagraphColorize() ? ColorUtil.colorizeToPlain(item.getText()) : item.getText()) : notifyTitle;
        final String nText = notifyTextFromItemText ? (item.isParagraphColorize() ? ColorUtil.colorizeToPlain(item.getText()) : item.getText()) : notifyText;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.NOTIFICATION_ITEMS_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(nTitle)
                .setContentText(nText)
                .setSubText(notifySubText)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        if (fullScreen) {
            PendingIntent pending = PendingIntent.getActivity(context, RandomUtil.nextInt(), AlarmActivity.createIntent(context, item.getId(), isPreRenderPreviewMode, nTitle, sound), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setFullScreenIntent(pending, true);
        }

        context.getSystemService(NotificationManager.class).notify(this.notificationId, builder.build());
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

    public boolean isPreRenderPreviewMode() {
        return isPreRenderPreviewMode;
    }

    public void setPreRenderPreviewMode(boolean preRenderPreviewMode) {
        isPreRenderPreviewMode = preRenderPreviewMode;
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }
}
