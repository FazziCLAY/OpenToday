package com.fazziclay.opentoday.gui;

import android.view.Menu;

import org.jetbrains.annotations.NotNull;

public class ActivitySettings implements Cloneable {
    private boolean clockVisible = true;
    private boolean analogClockForceVisible = false;
    private boolean analogClockForceHidden = false;
    private boolean notificationsVisible = true;
    private boolean dateClickCalendar = true;
    private boolean showCanonicalClock = false; // show clock if ACTIONBAR_POSITION in bottom and not show analog clock
    private ToolbarSettings toolbarSettings = null;


    public void analogClockForceVisible(boolean b) {
        this.analogClockForceVisible = b;
    }

    public void analogClockForceHidden(boolean b) {
        this.analogClockForceHidden = b;
    }

    public boolean isShowCanonicalClock() {
        return showCanonicalClock;
    }

    public void setShowCanonicalClock(boolean showCanonicalClock) {
        this.showCanonicalClock = showCanonicalClock;
    }

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

    public boolean isAnalogClockForceVisible() {
        return analogClockForceVisible;
    }

    public boolean isAnalogClockForceHidden() {
        return analogClockForceHidden;
    }

    public boolean isNotificationsVisible() {
        return notificationsVisible;
    }

    @NotNull
    public ActivitySettings clone() {
        try {
            ActivitySettings clone = (ActivitySettings) super.clone();
            clone.toolbarSettings = this.toolbarSettings == null ? null : this.toolbarSettings.clone();
            return clone;
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

    @NotNull
    @Override
    public String toString() {
        return "ActivitySettings{" +
                "clockVisible=" + clockVisible +
                ", notificationsVisible=" + notificationsVisible +
                ", dateClickCalendar=" + dateClickCalendar +
                ", toolbarSettings=" + toolbarSettings +
                '}';
    }

    public static class ToolbarSettings implements Cloneable {
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

        @NotNull
        @Override
        public ToolbarSettings clone() {
            try {
                ToolbarSettings clone = (ToolbarSettings) super.clone();
                clone.menuInterface = menu -> menuInterface.run(menu);
                clone.backButtonRunnable = () -> backButtonRunnable.run();
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

        public interface MenuInterface {
            void run(Menu menu);
        }

        @Override
        public String toString() {
            return "ToolbarSettings{" +
                    "title='" + title + '\'' +
                    ", titleResId=" + titleResId +
                    ", backButton=" + backButton +
                    ", backButtonRunnable=" + backButtonRunnable +
                    ", menu=" + menu +
                    ", menuInterface=" + menuInterface +
                    '}';
        }
    }
}
