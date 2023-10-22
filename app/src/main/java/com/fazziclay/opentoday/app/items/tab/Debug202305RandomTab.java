package com.fazziclay.opentoday.app.items.tab;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Readonly;
import com.fazziclay.opentoday.app.items.callback.ItemCallback;
import com.fazziclay.opentoday.app.items.callback.OnItemsStorageUpdate;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemType;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.SimpleItemsStorage;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.Tickable;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.StreamUtil;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.CallbackStorage;
import com.fazziclay.opentoday.util.callback.Status;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.ToIntFunction;

public class Debug202305RandomTab extends Tab implements Tickable, Readonly {
    public static final TabCodec CODEC = new TabCodec() {
        @NonNull
        @Override
        public Cherry exportTab(@NonNull Tab tab) {
            return super.exportTab(tab);
        }

        @NonNull
        @Override
        public Tab importTab(@NonNull Cherry cherry, @Nullable Tab tab) {
            Debug202305RandomTab t = tab == null ? new Debug202305RandomTab() : (Debug202305RandomTab) tab;
            super.importTab(cherry, t);
            return t;
        }
    };
    private static final boolean GEN_COLORS = true;
    private static final boolean DISABLE_TAB = true; // yes, disable this tab in releases. (this tab causes crashes...)

    private final SimpleItemsStorage itemsStorage = new SimpleItemsStorage((ItemsRoot) null) {
        @Override
        public void save() {
            // do nothing in Debug tab
        }
    };

    double DistanceSquared(int a, int b)
    {
        int deltaR = Color.red(a) - Color.red(b);
        int deltaG = Color.green(a) - Color.green(b);
        int deltaB = Color.blue(a) - Color.blue(b);
        int deltaAlpha = Color.alpha(a) - Color.alpha(b);
        double rgbDistanceSquared = (deltaR * deltaR + deltaG * deltaG + deltaB * deltaB) / 3.0;
        return deltaAlpha * deltaAlpha / 2.0 + rgbDistanceSquared * Color.alpha(a) * Color.alpha(b) / (255 * 255);
    }

    public Debug202305RandomTab() {
        super("Debug202305RandomTab");

        String[] s = new String[0];
        try {
            s = StreamUtil.read(App.get().getAssets().open("beautify_colors.txt")).split("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        Arrays.parallelSort(s, Comparator.comparingInt(s12 -> {
//            return (int) DistanceSquared(Color.WHITE, Color.parseColor(s12));
//        }));
        for (String s1 : s) {
            if (s1.startsWith("//")) continue;
            s1 = s1.split(";")[0];
            TextItem item = (TextItem) ItemsRegistry.REGISTRY.get(ItemType.TEXT).create();
            try {
                int c = Color.parseColor(s1);
                item.setViewCustomBackgroundColor(true);
                item.setViewBackgroundColor(c);
                item.setText("Всем привет, это тестовый текстик <3");
            } catch (Exception e) {
                item.setText("Exception: " + e);
            }
            itemsStorage.addItem(item);
        }

    }

    @Override
    public void addItem(Item item) {

    }

    @Override
    public void addItem(Item item, int position) {

    }

    @Override
    public void deleteItem(Item item) {

    }

    @NonNull
    @Override
    public Item copyItem(Item item) {
        return new TextItem("Coped item lol");
    }

    @Override
    public int getItemPosition(Item item) {
        return itemsStorage.getItemPosition(item);
    }

    @Nullable
    @Override
    public Item getItemById(UUID itemId) {
        return itemsStorage.getItemById(itemId);
    }

    @Override
    public void move(int positionFrom, int positionTo) {

    }


    @NonNull
    @Override
    public Item[] getAllItems() {
        return itemsStorage.getAllItems();
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;
        if (DISABLE_TAB) {
            return;
        }
        if (GEN_COLORS) {
            for (int i = 0; i < 2; i++) {
                tick_genColors(tickSession);
            }

            return;
        }
        tick111(itemsStorage);
        for (Item item : getAllItems()) {
            if (item instanceof ItemsStorage) {
                tick111((ItemsStorage) item);
            }
        }
    }

    boolean flag = true;
    int color;
    ItemCallback itemCallback = new ItemCallback() {
        @Override
        public Status click(Item item) {
            String color = ColorUtil.colorToHex(item.getViewBackgroundColor());
            FileUtil.addText(new File(App.get().getExternalCacheDir(), "beautify_colors.txt"), color + "\n");
            item.setMinimize(true);
            if (item instanceof TextItem t) {
                t.setText("[YES]  " + t.getText());
            }
            item.visibleChanged();
            return super.click(item);
        }
    };
    private void tick_genColors(TickSession tickSession) {
        if (flag) {
            color = RandomUtil.nextInt() | 0xFF000000;
            Item item = ItemsRegistry.REGISTRY.get(ItemType.values()[RandomUtil.nextInt(ItemType.values().length)]).create();
            if (item instanceof TextItem textItem) {
                textItem.setViewCustomBackgroundColor(true);
                textItem.setViewBackgroundColor(color);
                textItem.setText("Hello everybody!");
            }
            item.getItemCallbacks().addCallback(CallbackImportance.DEFAULT, itemCallback);
            itemsStorage.addItem(item);
        }
        if (!flag) {
            Item item = ItemsRegistry.REGISTRY.get(ItemType.values()[RandomUtil.nextInt(ItemType.values().length)]).create();
            if (item instanceof TextItem textItem) {
                textItem.setViewCustomBackgroundColor(true);
                textItem.setViewBackgroundColor(color);
                textItem.setParagraphColorize(true);
                textItem.setText("$[-#ffffff]White text!  $[-#000000] Black text $[-#ff00ff]" + ColorUtil.colorToHex(color));
            }
            item.getItemCallbacks().addCallback(CallbackImportance.DEFAULT, itemCallback);
            itemsStorage.addItem(item);
        }
        flag = !flag;
    }

    public void tick111(ItemsStorage itemsStorage) {
        byte mode = (byte) (RandomUtil.nextBoolean() ? 0 : 1); // 0 remove;  1 add
        if (itemsStorage.isEmpty()) mode = 1;

        if (mode == 0) {
            itemsStorage.deleteItem(itemsStorage.getAllItems()[RandomUtil.nextInt(itemsStorage.size())]);
        } else {
            Item item = ItemsRegistry.REGISTRY.get(ItemType.values()[RandomUtil.nextInt(ItemType.values().length)]).create();
            item.setViewBackgroundColor(RandomUtil.nextInt());
            item.setViewCustomBackgroundColor(true);
            item.setMinimize(RandomUtil.nextBoolean());
            if (item instanceof TextItem textItem) {
                textItem.setText(RandomUtil.nextIntPositive()+"");
            }
            if (item instanceof CounterItem counterItem) {
                counterItem.setCounter(RandomUtil.nextInt());
            }
            if (item instanceof CycleListItem cycleListItem) {
                cycleListItem.next();
            }
            if (item instanceof CheckboxItem checkboxItem) {
                checkboxItem.setChecked(RandomUtil.nextBoolean());
            }
            itemsStorage.addItem(item);
        }
    }

    @Override
    public int size() {
        return itemsStorage.size();
    }

    @Override
    public int totalSize() {
        return itemsStorage.totalSize();
    }

    @NonNull
    @Override
    public CallbackStorage<OnItemsStorageUpdate> getOnItemsStorageCallbacks() {
        return itemsStorage.getOnItemsStorageCallbacks();
    }

    @Override
    public boolean isEmpty() {
        return itemsStorage.isEmpty();
    }

    @Override
    public Item getItemAt(int position) {
        return itemsStorage.getItemAt(position);
    }
}
