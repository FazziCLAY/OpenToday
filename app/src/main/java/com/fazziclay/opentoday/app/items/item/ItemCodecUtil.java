package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;

import java.util.ArrayList;
import java.util.List;

public class ItemCodecUtil {
    private static final String KEY_ITEMTYPE = "itemType";

    @NonNull
    public static CherryOrchard exportItemList(@NonNull List<Item> items) {
        return exportItemList(items.toArray(new Item[0]));
    }

    public static CherryOrchard exportItemList(Item[] items) {
        CherryOrchard o = new CherryOrchard();
        for (Item item : items) {
            o.put(exportItem(item));
        }
        return o;
    }

    public static List<Item> importItemList(CherryOrchard orchard) {
        List<Item> o = new ArrayList<>();
        int i = 0;
        while (i < orchard.length()) {
            o.add(importItem(orchard.getCherryAt(i)));
            i++;
        }
        return o;
    }

    // import item (JSON -> Item)
    public static Item importItem(Cherry cherry) {
        try {
            /*get itemType form json*/String itemType = cherry.getString(KEY_ITEMTYPE);
            /*get class by itemType*/Class<? extends Item> itemClass = ItemsRegistry.REGISTRY.get(itemType).getClassType();
            /*get IETool by class*/
            AbstractItemCodec codec = ItemsRegistry.REGISTRY.get(itemClass).getCodec();
            return codec.importItem(cherry, null);
        } catch (Exception e) {
            return ((MissingNoItem)ItemsRegistry.REGISTRY.get(ItemType.MISSING_NO).getCodec().importItem(cherry, null))
                    .putException(e);
        }
    }

    // export item (Item -> JSON)
    public static Cherry exportItem(Item item) {
        try {
            /*IETool from itemClass*/
            AbstractItemCodec codec = ItemsRegistry.REGISTRY.get(item.getClass()).getCodec();
            /*Export from IETool*/
            Cherry cherry = codec.exportItem(item);
            /*Put itemType to json*/
            if (!(item instanceof MissingNoItem)) {
                cherry.put(KEY_ITEMTYPE, ItemsRegistry.REGISTRY.get(item.getClass()).getStringType());
            }
            return cherry;
        } catch (Exception e) {
            throw new RuntimeException("Exception while export item " + item, e);
        }
    }
}
