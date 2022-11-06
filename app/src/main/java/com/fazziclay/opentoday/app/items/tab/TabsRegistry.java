package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TabsRegistry {
    public static final TabsRegistry REGISTRY = new TabsRegistry();

    private static final TabInfo[] TABS = new TabInfo[]{
            new TabInfo(LocalItemsTab.class, "LocalItemsTab", LocalItemsTab.IE_TOOL)
    };

    private TabsRegistry() {}

    @NonNull
    public TabInfo[] getAllTabs() {
        return TABS.clone();
    }

    @Nullable
    public TabInfo getTabInfoByStringName(@NonNull String s) {
        for (TabInfo tab : TABS) {
            if (s.equals(tab.stringType)) return tab;
        }
        return null;
    }

    @Nullable
    public TabInfo getTabInfoByClass(Class<? extends Tab> s) {
        for (TabInfo tab : TABS) {
            if (s == tab.classType) return tab;
        }
        return null;
    }

    public static class TabInfo {
        private final Class<? extends Tab> classType;
        private final String stringType;
        private final TabImportExportTool importExportTool;

        public TabInfo(Class<? extends Tab> c, String v, TabImportExportTool importExportTool) {
            this.classType = c;
            this.stringType = v;
            this.importExportTool = importExportTool;
        }

        public Class<? extends Tab> getClassType() {
            return classType;
        }

        public String getStringType() {
            return stringType;
        }

        public TabImportExportTool getImportExportTool() {
            return importExportTool;
        }
    }
}
