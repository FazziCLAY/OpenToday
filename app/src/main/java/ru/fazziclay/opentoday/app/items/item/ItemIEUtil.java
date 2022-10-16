package ru.fazziclay.opentoday.app.items.item;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItemIEUtil {
    private static final String KEY_ITEMTYPE = "itemType";

    public static JSONArray exportItemList(List<Item> items) throws Exception {
        return exportItemList(items.toArray(new Item[0]));
    }

    public static JSONArray exportItemList(Item[] items) throws Exception {
        JSONArray o = new JSONArray();
        for (Item item : items) {
            o.put(exportItem(item));
        }
        return o;
    }

    public static List<Item> importItemList(JSONArray jsonItems) throws Exception {
        List<Item> o = new ArrayList<>();
        int i = 0;
        while (i < jsonItems.length()) {
            o.add(importItem(jsonItems.getJSONObject(i)));
            i++;
        }
        return o;
    }

    // import item (JSON -> Item)
    public static Item importItem(JSONObject jsonItem) throws Exception {
        /*get itemType form json*/String itemType = jsonItem.optString(KEY_ITEMTYPE);
        /*get class by itemType*/Class<? extends Item> itemClass = ItemsRegistry.REGISTRY.getItemInfoByStringName(itemType).getClassType();
        /*get IETool by class*/ItemImportExportTool ieTool = ItemsRegistry.REGISTRY.getItemInfoByClass(itemClass).getItemIETool();
        return ieTool.importItem(jsonItem, null);
    }

    // export item (Item -> JSON)
    public static JSONObject exportItem(Item item) throws Exception {
        /*IETool from itemClass*/ItemImportExportTool itemIETool = ItemsRegistry.REGISTRY.getItemInfoByClass(item.getClass()).getItemIETool();
        /*Export from IETool*/JSONObject jsonItem = itemIETool.exportItem(item);
        /*Put itemType to json*/jsonItem.put(KEY_ITEMTYPE, ItemsRegistry.REGISTRY.getItemInfoByClass(item.getClass()).getStringType());
        return jsonItem;
    }
}
