package com.fazziclay.opentoday.app.settings;

public class BooleanOption extends Option {
    public BooleanOption(String saveKey, boolean maybeUndefined, boolean defVal) {
        super(saveKey, maybeUndefined, defVal);
    }

    public void set(SettingsManager sm, boolean val) {
        _set(sm, val);
    }

    public boolean get(SettingsManager sm) {
        Object o = _get(sm);
        if (o instanceof Boolean bool) {
            return bool;
        }
        throw new RuntimeException("SettingsManager.BooleanOption can't be get because internal error :(");
    }

    @Override
    public Object parseValue(Object o) {
        return (Boolean) o;
    }

    @Override
    public Object writeValue(Object o) {
        return (Boolean) o;
    }
}
