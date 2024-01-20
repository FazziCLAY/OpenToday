package com.betterbrainmemory.opentoday.gui;

import android.content.Context;

import com.betterbrainmemory.opentoday.app.BeautifyColorManager;
import com.betterbrainmemory.opentoday.app.items.ItemsStorage;
import com.betterbrainmemory.opentoday.app.items.item.Item;
import com.betterbrainmemory.opentoday.app.items.item.ItemsRegistry;
import com.betterbrainmemory.opentoday.app.items.item.TextItem;
import com.betterbrainmemory.opentoday.app.settings.SettingsManager;

public class GuiItemsHelper {
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
