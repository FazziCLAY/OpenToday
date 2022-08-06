package ru.fazziclay.opentoday.util;

import android.content.Context;
import android.util.TypedValue;

public class ResUtil {
    public static int getAttrColor(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }
}
