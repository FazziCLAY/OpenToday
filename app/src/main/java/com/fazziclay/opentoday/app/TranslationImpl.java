package com.fazziclay.opentoday.app;

import com.fazziclay.opentoday.R;

public class TranslationImpl implements Translation {
    private final TranslateInterface translateInterface;

    public TranslationImpl(TranslateInterface translateInterface) {
        this.translateInterface = translateInterface;
    }

    @Override
    public String get(Object key, Object... args) {
        if (key instanceof Integer integer) {
            int i = integer;
            int resId = switch (i) {
                case KEY_TABS_DEFAULT_MAIN_NAME -> R.string.tab_defaultFirstName;
                default -> throw new IllegalStateException("Unknown translation key: " + i);
            };
            return translateInterface.translate(resId);
        }
        throw new RuntimeException("Unknown translation key type: " + key);
    }

    interface TranslateInterface {
        String translate(int resId);
    }
}
