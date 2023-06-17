package com.fazziclay.opentoday.gui;

import android.view.Menu;

import org.jetbrains.annotations.NotNull;

public class ActivitySettings implements Cloneable {
    private boolean clockVisible = true;
    private boolean notificationsVisible = true;
    private boolean dateClickCalendar = true;
    private ToolbarSettings toolbarSettings = null;


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

    public ToolbarSettings getToolbarSettings() {
        return toolbarSettings;
    }

    public void setToolbarSettings(ToolbarSettings toolbarSettings) {
        this.toolbarSettings = toolbarSettings;
    }

    public static class ToolbarSettings {
        private String title;
        private int titleResId;
        private boolean backButton;
        private Runnable backButtonRunnable;
        private int menu = 0; // 0 - not-exist
        private MenuInterface menuInterface;

        public static ToolbarSettings createBack(int titleResId, Runnable back) {
            return new ToolbarSettings(null, titleResId, true, back);
        }

        public static ToolbarSettings createBack(String title, Runnable back) {
            return new ToolbarSettings(title, 0, true, back);
        }

        public ToolbarSettings(String title, int titleResId, boolean backButton, Runnable backButtonRunnable) {
            this.title = title;
            this.titleResId = titleResId;
            this.backButton = backButton;
            this.backButtonRunnable = backButtonRunnable;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isBackButton() {
            return backButton;
        }

        public void setBackButton(boolean backButton) {
            this.backButton = backButton;
        }

        public Runnable getBackButtonRunnable() {
            return backButtonRunnable;
        }

        public void setBackButtonRunnable(Runnable backButtonRunnable) {
            this.backButtonRunnable = backButtonRunnable;
        }

        public int getTitleResId() {
            return titleResId;
        }

        public void setTitleResId(int titleResId) {
            this.titleResId = titleResId;
        }

        public int getMenu() {
            return menu;
        }

        public MenuInterface getMenuInterface() {
            return menuInterface;
        }

        public ToolbarSettings setMenu(int resId, MenuInterface menuInterface) {
            this.menu = resId;
            this.menuInterface = menuInterface;
            return this;
        }

        public interface MenuInterface {
            void run(Menu menu);
        }
    }
}
