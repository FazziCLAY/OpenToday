package com.fazziclay.opentoday.app.settings;

public class IntegerOption extends Option {
    public IntegerOption(String saveKey, boolean maybeUndefined, int defVal) {
        super(saveKey, maybeUndefined, defVal);
    }

    public void set(SettingsManager sm, int s) {
        _set(sm, s);
    }

    public int get(SettingsManager sm) {
        return (int) _get(sm);
    }

    @Override
    public Object parseValue(Object o) {
        return (int) o;
    }

    @Override
    public Object writeValue(Object o) {
        return (int) o;
    }
}
