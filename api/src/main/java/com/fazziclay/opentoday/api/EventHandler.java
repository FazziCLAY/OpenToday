package com.fazziclay.opentoday.api;

public class EventHandler {
    public static void call(Event event) {
        PluginManager.callEvent(event);
    }

    public void handle(Event event) {

    }
}
