package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;

import java.util.ArrayList;
import java.util.List;

public class ItemCodecUtil {
    private static final String KEY_ITEMTYPE = "item_type";

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
            // TODO: 14.01.2024 gjhklt6
            /*get class by itemType*/Class<? extends Item> itemClass = ItemsRegistry.REGISTRY.getByKey(itemType).getClassType();
            /*get IETool by class*/
            AbstractItemCodec codec = ItemsRegistry.REGISTRY.getByKey(itemClass).getCodec();
            return codec.importItem(cherry, null);
        } catch (Exception e) {
            return ((MissingNoItem)MissingNoItem.CODEC.importItem(cherry, null))
                    .putException(e);
        }
    }

    // export item (Item -> JSON)
    public static Cherry exportItem(Item item) {
        try {
            ItemsRegistry.ItemInfo itemInfo = ItemsRegistry.REGISTRY.getByKey(item.getClass());

            // TODO: 14.01.2024

            /*IETool from itemClass*/
            AbstractItemCodec codec = itemInfo.getCodec();
            /*Export from IETool*/
            Cherry cherry = codec.exportItem(item);
            /*Put itemType to json*/
            if (!(item instanceof MissingNoItem)) {
                cherry.put(KEY_ITEMTYPE, itemInfo.getIdentifier().string());
            }
            return cherry;
        } catch (Exception e) {
            throw new RuntimeException("Exception while export item " + item, e);
        }
    }
}
