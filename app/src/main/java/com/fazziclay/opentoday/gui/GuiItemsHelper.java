package com.fazziclay.opentoday.gui;

import android.content.Context;

import com.fazziclay.opentoday.app.BeautifyColorManager;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemType;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.TextItem;

public class GuiItemsHelper {
    /**
     * Create item include settings {@link SettingsManager#isRandomItemBackground()} and set text
     */
    public static Item createItem(Context context, ItemType itemType, String text, SettingsManager settingsManager) {
        final ItemsRegistry.ItemInfo registryItem = ItemsRegistry.REGISTRY.get(itemType);
        return createItem(context, registryItem, text, settingsManager);
    }


    /**
     * Create item include settings {@link SettingsManager#isRandomItemBackground()} and set text
     */
    public static Item createItem(Context context, ItemsRegistry.ItemInfo registryItem, String text, SettingsManager settingsManager) {
        final Item item = registryItem.create();
        applyInitRandomColorIfNeeded(context, item, settingsManager);
        if (item instanceof TextItem textItem) {
            textItem.setText(text);
        }

        return item;
    }

    /**
     * Add items to itemsStorage include logic of settings options
     */
    public static void addItem(Item item, ItemsStorage itemsStorage, SettingsManager settingsManager) {
        switch (settingsManager.getItemAddPosition()) {
            case TOP -> itemsStorage.addItem(item, 0);
            case BOTTOM -> itemsStorage.addItem(item);
        }
    }

    /**
     * Set random background color if SettingsManager.ITEM_RANDOM_BACKGROUND enabled
     */
    public static void applyInitRandomColorIfNeeded(Context context, Item item, SettingsManager sm) {
        if (sm.isRandomItemBackground()) {
            item.setViewCustomBackgroundColor(true);
            item.setViewBackgroundColor(BeautifyColorManager.randomBackgroundColor(context));
        }
    }
}
