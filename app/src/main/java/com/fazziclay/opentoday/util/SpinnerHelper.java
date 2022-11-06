package com.fazziclay.opentoday.util;

public class SpinnerHelper<T> {
    private final T[] values;
    private final String[] names;

    public SpinnerHelper(String[] names, T[] values) {
        this.names = names;
        this.values = values;
    }

    public int getPosition(T value) {
        int s = 0;
        for (T tempValue : values) {
            if (tempValue == value) {
                break;
            }
            s++;
        }
        return s;
    }

    public T getValue(int position) {
        return values[position];
    }

    public T[] getValues() {
        return values;
    }

    public String getName(int position) {
        return names[position];
    }

    public String[] getNames() {
        return names;
    }
}
