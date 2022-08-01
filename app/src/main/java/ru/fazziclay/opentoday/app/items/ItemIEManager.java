package ru.fazziclay.opentoday.app.items;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.fazziclay.javaneoutil.FileUtil;

public class ItemIEManager {
    // const
    private static final String KEY_ITEMTYPE = "itemType";
    private static final String ITEMTYPE_ITEM = "Item";
    private static final String ITEMTYPE_TEXT_ITEM = "TextItem";
    private static final String ITEMTYPE_CHECKBOX_ITEM = "CheckboxItem";
    private static final String ITEMTYPE_DAY_REPEATABLE_CHECKBOX_ITEM = "DayRepeatableCheckboxItem";
    private static final String ITEMTYPE_CYCLE_LIST_ITEM = "CycleListItem";
    private static final String ITEMTYPE_COUNTER_ITEM = "CounterItem";
    private static final int JSON_INTENT_SPACES = 2;
    private static final IEInstructions HELP_SET = new IEInstructions() {
        @Override
        public Item.ItemIETool getIETool(Class<? extends Item> item) {
            if (item == Item.class) {
                return Item.IE_TOOL;
            } else if (item == TextItem.class) {
                return TextItem.IE_TOOL;
            } else if (item == CheckboxItem.class) {
                return CheckboxItem.IE_TOOL;
            } else if (item == DayRepeatableCheckboxItem.class) {
                return DayRepeatableCheckboxItem.IE_TOOL;
            } else if (item == CycleListItem.class) {
                return CycleListItem.IE_TOOL;
            } else if (item == CounterItem.class) {
                return CounterItem.IE_TOOL;
            }
            throw new RuntimeException("Unknown class '" + item.getName() + "' extends Item! (check ItemIEManager!)");
        }

        @Override
        public String stringItemTypeFromItem(Item item) {
            Class<? extends Item> clazz = item.getClass();
            if (clazz == Item.class) {
                return ITEMTYPE_ITEM;
            } else if (clazz == TextItem.class) {
                return ITEMTYPE_TEXT_ITEM;
            } else if (clazz == CheckboxItem.class) {
                return ITEMTYPE_CHECKBOX_ITEM;
            } else if (clazz == DayRepeatableCheckboxItem.class) {
                return ITEMTYPE_DAY_REPEATABLE_CHECKBOX_ITEM;
            } else if (clazz == CycleListItem.class) {
                return ITEMTYPE_CYCLE_LIST_ITEM;
            } else if (clazz == CounterItem.class) {
                return ITEMTYPE_COUNTER_ITEM;
            }
            throw new RuntimeException("Unknown class '" + clazz.getName() + "' extends Item! (check ItemIEManager!)");
        }

        @Override
        public Class<? extends Item> stringItemTypeToClass(String itemType) {
            switch (itemType) {
                case ITEMTYPE_ITEM:
                    return Item.class;
                case ITEMTYPE_TEXT_ITEM:
                    return TextItem.class;
                case ITEMTYPE_CHECKBOX_ITEM:
                    return CheckboxItem.class;
                case ITEMTYPE_DAY_REPEATABLE_CHECKBOX_ITEM:
                    return DayRepeatableCheckboxItem.class;
                case ITEMTYPE_CYCLE_LIST_ITEM:
                    return CycleListItem.class;
                case ITEMTYPE_COUNTER_ITEM:
                    return CounterItem.class;
            }
            throw new RuntimeException("Unknown itemType '" + itemType + "' (check ItemIEManager!)");
        }
    };

    // manager
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
        /*get class by itemType*/Class<? extends Item> itemClass = HELP_SET.stringItemTypeToClass(itemType);
        /*get IETool by class*/Item.ItemIETool ieTool = HELP_SET.getIETool(itemClass);
        return ieTool.importItem(jsonItem);
    }

    // export item (Item -> JSON)
    public static JSONObject exportItem(Item item) throws Exception {
        /*IETool from itemClass*/Item.ItemIETool itemIETool = HELP_SET.getIETool(item.getClass());
        /*Export from IETool*/JSONObject jsonItem = itemIETool.exportItem(item);
        /*Put itemType to json*/jsonItem.put(KEY_ITEMTYPE, HELP_SET.stringItemTypeFromItem(item));
        return jsonItem;
    }

    private interface IEInstructions {
        Item.ItemIETool getIETool(Class<? extends Item> item);

        String stringItemTypeFromItem(Item item);
        Class<? extends Item> stringItemTypeToClass(String itemType);
    }
}
