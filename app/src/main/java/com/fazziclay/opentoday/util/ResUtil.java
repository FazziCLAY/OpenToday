package com.fazziclay.opentoday.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

public class ResUtil {
    public static int getAttrColor(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    public static TypedArray getStyleColor(Context context, int style, int... attrs) {
        return context.obtainStyledAttributes(style, attrs);
    }
}
