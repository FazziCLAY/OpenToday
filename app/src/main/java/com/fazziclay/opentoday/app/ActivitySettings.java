package com.fazziclay.opentoday.app;

public class ActivitySettings {
    private boolean clockVisible = true;
    private boolean notificationsVisible = true;


    public ActivitySettings setClockVisible(boolean clockVisible) {
        this.clockVisible = clockVisible;
        return this;
    }

    public ActivitySettings invertClockVisible() {
        this.clockVisible = !this.clockVisible;
        return this;
    }

    public ActivitySettings setNotificationsVisible(boolean notificationsVisible) {
        this.notificationsVisible = notificationsVisible;
        return this;
    }

    public ActivitySettings invertNotificationsVisible() {
        this.notificationsVisible = !this.notificationsVisible;
        return this;
    }

    public boolean isClockVisible() {
        return clockVisible;
    }

    public boolean isNotificationsVisible() {
        return notificationsVisible;
    }
}
