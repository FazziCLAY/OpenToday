package com.fazziclay.opentoday.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtil {
    public static void selectText(Context context, int titleResId, String text) {
        selectText(context, context.getString(titleResId), text);
    }
    public static void selectText(Context context, String title, String text) {
        getManager(context).setPrimaryClip(ClipData.newPlainText(title, text));
    }

    @SuppressWarnings("deprecation")
    public static String getSelectedText(Context context) {
        return String.valueOf(getManager(context).getText());
    }

    private static ClipboardManager getManager(Context context) {
        return context.getSystemService(ClipboardManager.class);
    }
}
