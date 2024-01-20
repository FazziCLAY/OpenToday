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
            String itemType = cherry.getString(KEY_ITEMTYPE);
            var itemInfo = ItemsRegistry.REGISTRY.getByKey(itemType);
            AbstractItemCodec codec = itemInfo.getCodec();
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
            AbstractItemCodec codec = itemInfo.getCodec();
            Cherry cherry = codec.exportItem(item);
            if (!(item instanceof MissingNoItem)) {
                cherry.put(KEY_ITEMTYPE, itemInfo.getIdentifier().string());
            }
            return cherry;
        } catch (Exception e) {
            throw new RuntimeException("Exception while export item " + item, e);
        }
    }
}
