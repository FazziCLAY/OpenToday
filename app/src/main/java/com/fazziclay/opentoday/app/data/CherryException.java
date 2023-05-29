package com.fazziclay.opentoday.app.data;

public class CherryException extends RuntimeException {
    public CherryException() {}

    public CherryException(String s) {
        super(s);
    }

    public CherryException(String s, Throwable e) {
        super(s, e);
    }

    public CherryException(Throwable e) {
        super(e);
    }
}
