package ru.fazziclay.opentoday.util;

import android.util.Log;

import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.callback.Status;

public class L {
    private static final L instance = new L();

    public static L getInstance() {
        return instance;
    }

    private String text = "=== FIRST LOG ===";
    private final CallbackStorage<OnDebugLog> callbackStorage = new CallbackStorage<>();

    public static void o(Object... objects) {
        getInstance().o_(objects);
    }

    public static CallbackStorage<OnDebugLog> getCallbackStorage() {
        return getInstance().getCallbackStorage_();
    }

    public static Object nn(Object o) {
        return o == null ? null : "non-null";
    }

    public CallbackStorage<OnDebugLog> getCallbackStorage_() {
        return callbackStorage;
    }

    public void o_(Object... objects) {
        StringBuilder temp = new StringBuilder();
        String time = "";//System.currentTimeMillis() + "";
        temp.append("[").append(time).append("] ");
        for (Object object : objects) {
            temp.append(object).append(" ");
        }

        Log.e("L", temp.toString());
        text = temp + "\n" + text;

        callbackStorage.run((callbackStorage, callback) -> {
            callback.run(text);
            return Status.NONE;
        });
    }

    public String getFinalText() {
        return "=== LAST LOG ===\n" + text;
    }
}
