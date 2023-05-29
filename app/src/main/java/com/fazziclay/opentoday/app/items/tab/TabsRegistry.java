package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Registry of tabs. Contains links between the following:
 * <p>* Java class. E.g. LocalItemsTab.class</p>
 * <p>* String type. E.g. "LocalItemsTab"</p>
 * <p>* Codec. see {@link AbstractTabCodec}</p>
 */
public class TabsRegistry {
    public static final TabsRegistry REGISTRY = new TabsRegistry();

    private static final TabInfo[] TABS = new TabInfo[]{
            new TabInfo(LocalItemsTab.class, "LocalItemsTab", LocalItemsTab.CODEC),
            new TabInfo(Debug202305RandomTab.class, "Debug202305RandomTab", Debug202305RandomTab.CODEC),
    };

    private TabsRegistry() {}

    @NonNull
    public TabInfo[] getAllTabs() {
        return TABS.clone();
    }

    @Nullable
    public TabInfo get(@NonNull String s) {
        for (TabInfo tab : TABS) {
            if (s.equals(tab.stringType)) return tab;
        }
        return null;
    }

    @Nullable
    public TabInfo get(Class<? extends Tab> s) {
        for (TabInfo tab : TABS) {
            if (s == tab.classType) return tab;
        }
        return null;
    }

    public static class TabInfo {
        private final Class<? extends Tab> classType;
        private final String stringType;
        private final AbstractTabCodec codec;

        public TabInfo(Class<? extends Tab> classType, String stringType, AbstractTabCodec codec) {
            this.classType = classType;
            this.stringType = stringType;
            this.codec = codec;
        }

        public Class<? extends Tab> getClassType() {
            return classType;
        }

        public String getStringType() {
            return stringType;
        }

        public AbstractTabCodec getCodec() {
            return codec;
        }
    }
}
