package com.fazziclay.opentoday.app.items.notification;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.ItemNotificationHandler;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.util.Calendar;

public class DayItemNotification extends ItemNotification {
    public static final DayItemNotificationCodec CODEC = new DayItemNotificationCodec();
    private static class DayItemNotificationCodec extends ItemNotification.ItemNotificationCodec {
        @Override
        public Cherry exportNotification(ItemNotification itemNotification) {
            DayItemNotification d = (DayItemNotification) itemNotification;
            return super.exportNotification(itemNotification)
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
        public ItemNotification importNotification(Cherry cherry, ItemNotification notification) {
            DayItemNotification o = notification == null ? new DayItemNotification() : (DayItemNotification) notification;
            super.importNotification(cherry, o);

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
        // do nothing
    }

    @Override
    public boolean tick(TickSession tickSession) {
        boolean isTimeToSend = tickSession.getDayTime() >= time;

        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_NOTIFICATION_SCHEDULE)) {
            final long shift = isTimeToSend ? TimeUtil.SECONDS_IN_DAY : 0;
            final long baseDayMs = tickSession.getNoTimeCalendar().getTimeInMillis() + (shift * 1000L);
            long triggerAtMs = baseDayMs + (time * 1000L) + 599;
            tickSession.getItemNotificationHandler().setAlarm(this, triggerAtMs);
        }

        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_NOTIFICATION_UPDATE)) {
            int dayOfYear = tickSession.getGregorianCalendar().get(Calendar.DAY_OF_YEAR);
            if (dayOfYear != latestDayOfYear) {
                if (isTimeToSend) {
                    sendNotify(tickSession.getItemNotificationHandler());
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
        return (DayItemNotification) super.clone();
    }

    public void sendNotify(ItemNotificationHandler itemNotificationHandler) {
        itemNotificationHandler.handle(getParentItem(), this);
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

    @NonNull
    @Override
    public String toString() {
        return "DayItemNotification{" +
                "notificationId=" + notificationId +
                ", notifyTitle='" + notifyTitle + '\'' +
                ", notifyTitleFromItemText=" + notifyTitleFromItemText +
                ", notifyText='" + notifyText + '\'' +
                ", notifyTextFromItemText=" + notifyTextFromItemText +
                ", notifySubText='" + notifySubText + '\'' +
                ", latestDayOfYear=" + latestDayOfYear +
                ", time=" + time +
                ", fullScreen=" + fullScreen +
                ", isPreRenderPreviewMode=" + isPreRenderPreviewMode +
                ", sound=" + sound +
                "} " + super.toString();
    }
}
