package com.fazziclay.opentoday.util;

import android.content.Context;

import com.fazziclay.opentoday.gui.EnumsRegistry;

public class EnumUtil {
    public static <T extends Enum<?>> void addToSimpleSpinnerAdapter(Context context, SimpleSpinnerAdapter<T> adapter, T[] enums) {
        for (T anEnum : enums) {
            addToSimpleSpinnerAdapter(context, adapter, anEnum);
        }
    }

    public static <T extends Enum<?>> void addToSimpleSpinnerAdapter(Context context, SimpleSpinnerAdapter<T> adapter, T anEnum) {
        adapter.add(EnumsRegistry.INSTANCE.name(anEnum, context), anEnum);
    }
}
