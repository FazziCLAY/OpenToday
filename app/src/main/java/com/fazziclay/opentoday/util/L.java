package com.fazziclay.opentoday.util;

import android.util.Log;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.callback.Status;

import java.util.Arrays;

public class L {
    public static final boolean ENABLED = (App.LOG && true);
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
        return InlineUtil.nullStat(o);
    }

    public CallbackStorage<OnDebugLog> getCallbackStorage_() {
        return callbackStorage;
    }

    public void o_(Object... objects) {
        if (!ENABLED) {
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
