package com.fazziclay.opentoday.app.settings;

public class StringOption extends Option {
    public StringOption(String saveKey, boolean maybeUndefined, String defVal) {
        super(saveKey, maybeUndefined, defVal);
    }

    public void set(SettingsManager sm, String s) {
        _set(sm, s);
    }

    public String get(SettingsManager sm) {
        return (String) _get(sm);
    }

    @Override
    public Object parseValue(Object o) {
        return (String) o;
    }

    @Override
    public Object writeValue(Object o) {
        return (String) o;
    }
}
