package ru.fazziclay.opentoday.util;

import android.util.Log;

import java.util.Arrays;

import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.callback.CallbackStorage;
import ru.fazziclay.opentoday.callback.Status;

public class L {
    public static final boolean ENABLED = (App.DEBUG && false);
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
        if (!ENABLED) {
            Log.e("L", Arrays.toString(objects));
            return;
        }
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
        return ENABLED ? "=== LAST LOG ===\n" + text : "(Disabled)";
    }
}
