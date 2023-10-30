package com.fazziclay.opentoday.app.settings;

public class Option {
    protected String saveKey;
    protected boolean maybeUndefined;
    protected Object defVal;

    public Option(String saveKey, boolean maybeUndefined, Object defVal) {
        this.saveKey = saveKey;
        this.maybeUndefined = maybeUndefined;
        this.defVal = defVal;
    }

    public String getSaveKey() {
        return saveKey;
    }

    protected Object _get(SettingsManager sm) {
        Object o = sm.getOption(this);
        if (o == null) {
            if (!maybeUndefined) {
                _set(sm, defVal);
            }
            return defVal;
        }
        return o;
    }

    public Object getObject(SettingsManager sm) {
        return _get(sm);
    }

    protected void _set(SettingsManager sm, Object o) {
        sm.setOption(this, o);
    }

    public void def(SettingsManager sm) {
        if (maybeUndefined) {
            sm.clearOption(this);
        } else {
            throw new UnsupportedOperationException("This option can't be undefined");
        }
    }

    public Object parseValue(Object o) {
        return o;
    }

    public Object writeValue(Object o) {
        return o;
    }
}
