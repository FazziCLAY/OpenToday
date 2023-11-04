package com.fazziclay.opentoday.api;

public class PluginException extends RuntimeException {
    private final OpenTodayPlugin plugin;

    public PluginException(OpenTodayPlugin plugin) {
        this.plugin = plugin;
    }

    public PluginException(String message, OpenTodayPlugin plugin) {
        super(message);
        this.plugin = plugin;
    }

    public PluginException(String message, Throwable cause, OpenTodayPlugin plugin) {
        super(message, cause);
        this.plugin = plugin;
    }

    public PluginException(Throwable cause, OpenTodayPlugin plugin) {
        super(cause);
        this.plugin = plugin;
    }

    public PluginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, OpenTodayPlugin plugin) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.plugin = plugin;
    }

    public OpenTodayPlugin getPlugin() {
        return plugin;
    }
}
