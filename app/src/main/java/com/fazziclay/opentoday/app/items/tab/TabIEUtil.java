package com.fazziclay.opentoday.app.items.tab;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TabIEUtil {
    private static final String KEY_TABTYPE = "tabType";
    private static final TabsRegistry TABS_REGISTRY = TabsRegistry.REGISTRY;

    public static JSONArray exportTabs(List<Tab> tabs) throws Exception {
        return exportTabs(tabs.toArray(new Tab[0]));
    }

    public static JSONArray exportTabs(Tab[] tabs) throws Exception {
        JSONArray o = new JSONArray();
        for (Tab tab : tabs) {
            o.put(exportTab(tab));
        }
        return o;
    }

    public static List<Tab> importTabs(JSONArray jsonTabs) throws Exception {
        List<Tab> o = new ArrayList<>();
        int i = 0;
        while (i < jsonTabs.length()) {
            o.add(importTab(jsonTabs.getJSONObject(i)));
            i++;
        }
        return o;
    }

    public static Tab importTab(JSONObject jsonTab) throws Exception {
        String tabType = jsonTab.optString(KEY_TABTYPE);
        TabImportExportTool ie = TABS_REGISTRY.getTabInfoByStringName(tabType).getImportExportTool();
        return ie.importTab(jsonTab, null);
    }

    public static JSONObject exportTab(Tab tab) throws Exception {
        TabsRegistry.TabInfo tabInfo = TABS_REGISTRY.getTabInfoByClass(tab.getClass());
        TabImportExportTool ie = tabInfo.getImportExportTool();
        JSONObject jsonItem = ie.exportTab(tab);
        jsonItem.put(KEY_TABTYPE, tabInfo.getStringType());
        return jsonItem;
    }
}
