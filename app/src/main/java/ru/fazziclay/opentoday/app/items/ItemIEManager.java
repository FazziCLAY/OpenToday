package ru.fazziclay.opentoday.app.items;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.fazziclay.javaneoutil.FileUtil;
import ru.fazziclay.opentoday.app.items.item.Item;

public class ItemIEManager {
    private static final String KEY_ITEMTYPE = "itemType";
    private static final int JSON_INTENT_SPACES = 2;

    private final File saveFile;

    public ItemIEManager(File saveFile) {
        this.saveFile = saveFile;
    }

    public DataTransferPacket loadFromFile() {
        if (!FileUtil.isExist(saveFile)) {
            return null;
        }
        try {
            JSONObject root = new JSONObject(FileUtil.getText(saveFile, "{}"));
            JSONArray jsonItems = root.optJSONArray("items");
            if (jsonItems == null) jsonItems = new JSONArray();
            
            DataTransferPacket dataTransferPacket = new DataTransferPacket();
            dataTransferPacket.items = importItemList(jsonItems);
            return dataTransferPacket;
            
        } catch (Exception e) {
            throw new RuntimeException("Load exception", e);
        }
    }

    public void saveToFile(DataTransferPacket dataTransferPacket) {
        try {
            JSONObject root = new JSONObject();
            JSONArray jsonItems = exportItemList(dataTransferPacket.items);
            root.put("items", jsonItems);

            FileUtil.setText(saveFile, root.toString(JSON_INTENT_SPACES));

        } catch (Exception e) {
            throw new RuntimeException("Save exception", e);
        }
    }

    public static JSONArray exportItemList(List<Item> items) throws Exception {
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
        return ieTool.importItem(jsonItem);
    }

    // export item (Item -> JSON)
    public static JSONObject exportItem(Item item) throws Exception {
        /*IETool from itemClass*/ItemImportExportTool itemIETool = ItemsRegistry.REGISTRY.getItemInfoByClass(item.getClass()).getItemIETool();
        /*Export from IETool*/JSONObject jsonItem = itemIETool.exportItem(item);
        /*Put itemType to json*/jsonItem.put(KEY_ITEMTYPE, ItemsRegistry.REGISTRY.getItemInfoByClass(item.getClass()).getStringType());
        return jsonItem;
    }
}
