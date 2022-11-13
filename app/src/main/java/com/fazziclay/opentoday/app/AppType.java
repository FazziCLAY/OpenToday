package com.fazziclay.opentoday.app;

public enum AppType {
    OLD_RED,
    NEW_GREEN;

    public static AppType parse(String applicationId) {
        return applicationId.charAt(1) == 'o'? NEW_GREEN : OLD_RED;
    }
}
