package com.fazziclay.opentoday.api;

public class EventHandler {
    public static void call(Event event) {
        try {
            PluginManager.callEvent(event);
        } catch (Exception e) {
            PluginManager.callEvent(new EventExceptionEvent(event, e));
        }
    }

    public void handle(Event event) {

    }
}
