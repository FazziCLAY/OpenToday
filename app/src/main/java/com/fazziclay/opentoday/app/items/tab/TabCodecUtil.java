package com.fazziclay.opentoday.app.items.tab;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;

import java.util.ArrayList;
import java.util.List;

// 2023.05.13 quality control passed
public class TabCodecUtil {
    private static final String KEY_TABTYPE = "tabType";
    private static final TabsRegistry TABS_REGISTRY = TabsRegistry.REGISTRY;

    @NonNull
    public static CherryOrchard exportTabList(@NonNull final List<Tab> tabs) {
        return exportTabList(tabs.toArray(new Tab[0]));
    }

    @NonNull
    public static CherryOrchard exportTabList(@NonNull final Tab[] tabs) {
        final CherryOrchard ret = new CherryOrchard();
        for (Tab tab : tabs) {
            ret.put(exportTab(tab));
        }
        return ret;
    }

    @NonNull
    public static List<Tab> importTabList(@NonNull final CherryOrchard orchard) {
        final List<Tab> ret = new ArrayList<>();
        orchard.forEachCherry((_ignore, cherry) -> ret.add(importTab(cherry)));
        return ret;
    }

    @NonNull
    public static Tab importTab(@NonNull final Cherry cherry) {
        final AbstractTabCodec codec = TABS_REGISTRY.get(cherry.getString(KEY_TABTYPE)).getCodec();
        return codec.importTab(cherry, null);
    }

    @NonNull
    public static Cherry exportTab(@NonNull final Tab tab) {
        final TabsRegistry.TabInfo tabInfo = TABS_REGISTRY.get(tab.getClass());
        final AbstractTabCodec codec = tabInfo.getCodec();
        return codec.exportTab(tab)
                .put(KEY_TABTYPE, tabInfo.getStringType());
    }
}
