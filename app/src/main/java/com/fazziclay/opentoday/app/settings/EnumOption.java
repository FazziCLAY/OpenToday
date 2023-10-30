package com.fazziclay.opentoday.app.settings;

public class EnumOption<T extends Enum<T>> extends Option {
    private final Class<T> clazz;

    public EnumOption(String saveKey, boolean maybeUndefined, T defVal) {
        super(saveKey, maybeUndefined, defVal);
        this.clazz = (Class<T>) defVal.getClass();
    }

    public T get(SettingsManager sm) {
        Object o = _get(sm);
        if (o instanceof Enum<?> anEnum) {
            return (T) anEnum;
        }
        throw new RuntimeException("SettingsManager unknown value in EnumOption");
    }

    public void set(SettingsManager sm, T t) {
        _set(sm, t);
    }

    @Override
    public Object parseValue(Object o) {
        String s = (String) o;
        return Enum.valueOf(clazz, s);
    }

    @Override
    public Object writeValue(Object o) {
        T anEnum = (T) o;
        return anEnum.name();
    }
}
