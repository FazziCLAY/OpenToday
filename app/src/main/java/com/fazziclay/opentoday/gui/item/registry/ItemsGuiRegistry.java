package com.fazziclay.opentoday.gui.item.registry;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.fazziclay.opentoday.app.Registry;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.gui.item.renderer.MissingNoRenderer;
import com.fazziclay.opentoday.util.Identifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;

public final class ItemsGuiRegistry extends Registry<Identifier, ItemsGuiRegistry.ItemGuiInfo> {
    public static final ItemsGuiRegistry REGISTRY = new ItemsGuiRegistry();

    private final HashMap<Identifier, ItemGuiInfo> ITEMS = new HashMap<>();

    private ItemsGuiRegistry() {
        MissingNoRenderer m = new MissingNoRenderer();
        Identifier identifier = Identifier.of("base:item/missing_no");
        ITEMS.put(identifier, new ItemGuiInfo(identifier, m, m));
    }

    public void appendFromJson(String json) throws JSONException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        JSONObject jsonObject = new JSONObject(json).getJSONObject("items");

        Iterator<String> iterator = jsonObject.keys();

        while (iterator.hasNext()) {
            String idStr = iterator.next();
            Identifier id = Identifier.of(idStr);
            JSONObject entry = jsonObject.getJSONObject(idStr);

            ItemRenderer<Item> itemRenderer = null;
            NameResolver nameResolver = null;
            if (entry.has("renderer")) {
                String renderer = entry.getString("renderer");
                itemRenderer = (ItemRenderer<Item>) Class.forName(renderer).getConstructor().newInstance();
            }

            if (entry.has("name_resolver")) {
                String name_resolver = entry.getString("name_resolver");
                nameResolver = (NameResolver) Class.forName(name_resolver).getConstructor().newInstance();
            }

            if (entry.has("renderer_and_name_resolver")) {
                String both = entry.getString("renderer_and_name_resolver");
                nameResolver = (NameResolver) Class.forName(both).getConstructor().newInstance();
                itemRenderer = (ItemRenderer) nameResolver;
            }

            if (getByKey(Identifier.of(idStr)) != null) {
                throw new RuntimeException("This content already registered!");
            }


            var info = new ItemGuiInfo(id, itemRenderer, nameResolver);
            ITEMS.put(id, info);
        }
    }

    public static class ItemGuiInfo {
        private final Identifier identifier;
        private final ItemRenderer<Item> renderer;
        private final NameResolver nameResolver;

        public ItemGuiInfo(Identifier identifier, ItemRenderer renderer, NameResolver nameResolver) {
            this.identifier = identifier;
            this.renderer = renderer;
            this.nameResolver = nameResolver;
        }

        public Identifier getIdentifier() {
            return identifier;
        }

        public ItemRenderer<?> getRenderer() {
            return renderer;
        }

        public NameResolver getNameResolver() {
            return nameResolver;
        }
    }

    @Override
    public ItemGuiInfo getByKey(Identifier identifier) {
        return ITEMS.get(identifier);
    }

    public ItemRenderer<Item> rendererForItem(Identifier identifier) {
        ItemGuiInfo itemGuiInfo = getByKey(identifier);
        if (itemGuiInfo == null) {
            return (item, context, layoutInflater, parent, behavior, onItemClick, itemViewGenerator, previewMode, destroyer) -> {
                TextView textView = new TextView(context);
                textView.setTextColor(Color.RED);
                textView.setText("Renderer not found for: " + item.getClass().getCanonicalName() + " Identifier: " + identifier);
                return textView;
            };
        }
        return itemGuiInfo.renderer;
    }

    public String nameOf(Context context, Item item) {
        if (item == null) return "<null>";

        ItemGuiInfo itemGuiInfo = getByKey(item.getItemType());
        if (itemGuiInfo == null) {
            return "<Name can't resolved: "+item.getItemType()+">";
        }

        return itemGuiInfo.nameResolver.resolveName(context);
    }

    public String nameOf(Context context, ItemsRegistry.ItemInfo itemInfo) {
        if (itemInfo == null) return "<null>";

        ItemGuiInfo itemGuiInfo = getByKey(itemInfo.getIdentifier());
        if (itemGuiInfo == null) {
            return "<Name can't resolved: "+itemInfo.getIdentifier()+">";
        }

        return itemGuiInfo.nameResolver.resolveName(context);
    }

    public String descriptionOf(Context context, ItemsRegistry.ItemInfo itemInfo) {
        if (itemInfo == null) return "<null>";

        ItemGuiInfo itemGuiInfo = getByKey(itemInfo.getIdentifier());
        if (itemGuiInfo == null) {
            return "<Description can't resolved: "+ itemInfo.getIdentifier()+">";
        }

        return itemGuiInfo.nameResolver.resolveDescription(context);
    }


}
