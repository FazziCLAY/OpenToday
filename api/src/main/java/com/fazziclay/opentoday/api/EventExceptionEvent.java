package com.fazziclay.opentoday.api;

public class EventExceptionEvent implements Event {
    private final Event event;
    private final Exception exception;

    public EventExceptionEvent(Event event, Exception e) {
        this.event = event;
        this.exception = e;
    }

    public Event getEvent() {
        return event;
    }

    public Exception getException() {
        return exception;
    }
}
