package com.fazziclay.opentoday.app.datafixer;

import android.content.Context;

import com.fazziclay.javaneoutil.FileUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class Scheme8Fix9 {
    public static void fix8versionTo9(Context context) {
        // DO NOT EDIT!
        final File itemsDataFile = new File(context.getExternalFilesDir(""), "item_data.json");
        final File itemsDataCompressFile = new File(context.getExternalFilesDir(""), "item_data.gz");

        try {
            if (FileUtil.isExist(itemsDataFile)) {
                JSONObject j = new JSONObject(FileUtil.getText(itemsDataFile, "{}"));
                JSONArray tabs = j.optJSONArray("tabs");
                if (tabs == null) tabs = new JSONArray();

                tabsListFix(context, tabs);

                FileUtil.setText(itemsDataFile, j.toString(2));
                FileUtil.delete(itemsDataCompressFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tabFix(Context context, JSONObject tab) throws Exception {
        JSONArray tabItems = tab.optJSONArray("items");
        if (tabItems == null) return;

        itemsListFix(context, tabItems);
    }

    public static void itemsListFix(Context context, JSONArray items) throws Exception {
        if (items == null) return;
        int i = 0;
        while (i < items.length()) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;

            itemFix(context, item);

            i++;
        }
    }

    public static void itemFix(Context context, JSONObject item) throws Exception {
        String itemType = item.optString("itemType", null);
        if (itemType == null) return;

        if (itemType.equals("CycleListItem")) {
            final String KEY_ITEMS = "itemsCycle";
            itemsListFix(context, item.optJSONArray(KEY_ITEMS));

        } else if (itemType.equals("GroupItem")) {
            final String KEY_ITEMS = "items";
            itemsListFix(context, item.optJSONArray(KEY_ITEMS));


        } else if (itemType.equals("FilterGroupItem")) {
            // do not edit.
            final String KEY_WRAPPERS = "items";
            final String KEY_NEW_TICK_BEHAVIOR = "tickBehavior";
            final String TICK_BEHAVIOR_DEFAULT_OLDEST = "ACTIVE";

            item.put(KEY_NEW_TICK_BEHAVIOR, TICK_BEHAVIOR_DEFAULT_OLDEST);

            JSONArray wrappers = item.optJSONArray(KEY_WRAPPERS);
            if (wrappers == null) return;

            int i = 0;
            while (i < wrappers.length()) {
                JSONObject wrapper = wrappers.optJSONObject(i);
                if (wrapper == null) continue;
                JSONObject wrapperFilter = wrapper.optJSONObject("filter");
                if (wrapperFilter == null) continue;

                JSONObject wrapperItem = wrapper.optJSONObject("item");
                if (wrapperItem == null) continue;

                JSONObject newFilter = new JSONObject()
                        .put("filterType", "LogicContainerItemFilter")
                        .put("filters", new JSONArray().put(new JSONObject(wrapperFilter.toString()).put("filterType", "DateItemFilter")));
                wrapper.put("filter", newFilter);

                itemFix(context, wrapperItem);

                i++;
            }
        }
    }

    public static void tabsListFix(Context context, JSONArray tabs) throws Exception {
        int i = 0;
        while (i < tabs.length()) {
            JSONObject tab = tabs.optJSONObject(i);
            if (tab == null) continue;

            tabFix(context, tab);
            i++;
        }
    }
}
