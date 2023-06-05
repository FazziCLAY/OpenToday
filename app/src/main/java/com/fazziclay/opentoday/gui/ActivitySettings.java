package com.fazziclay.opentoday.gui;

import org.jetbrains.annotations.NotNull;

public class ActivitySettings implements Cloneable {
    private boolean clockVisible = true;
    private boolean notificationsVisible = true;
    private boolean dateClickCalendar = true;


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

    @NotNull
    public ActivitySettings clone() {
        try {
            return (ActivitySettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public ActivitySettings setDateClickCalendar(boolean b) {
        this.dateClickCalendar = b;
        return this;
    }

    public boolean isDateClickCalendar() {
        return dateClickCalendar;
    }
}
