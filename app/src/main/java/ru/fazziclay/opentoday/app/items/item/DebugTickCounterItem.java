package ru.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.app.TickSession;

public class DebugTickCounterItem extends TextItem {
    // START - Save
    public final static DebugTickCounterItemIETool IE_TOOL = new DebugTickCounterItemIETool();
    public static class DebugTickCounterItemIETool extends TextItem.TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            DebugTickCounterItem debugTickCounterItem = (DebugTickCounterItem) item;
            return super.exportItem(debugTickCounterItem)
                    .put("counter", debugTickCounterItem.counter);
        }

        private final DebugTickCounterItem defaultValues = new DebugTickCounterItem();
        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            DebugTickCounterItem debugTickCounterItem = item != null ? (DebugTickCounterItem) item : new DebugTickCounterItem();
            super.importItem(json, debugTickCounterItem);
            debugTickCounterItem.counter = json.optInt("counter", defaultValues.counter);
            return debugTickCounterItem;
        }
    }
    // END - Save

    @NonNull
    public static DebugTickCounterItem createEmpty() {
        return new DebugTickCounterItem("", 0);
    }

    @SaveKey(key = "counter") @RequireSave private int counter;

    protected DebugTickCounterItem() {
        super();
    }

    public DebugTickCounterItem(String text, int counter) {
        super(text);
        this.counter = counter;
    }

    // Append
    public DebugTickCounterItem(TextItem textItem, int counter) {
        super(textItem);
        this.counter = counter;
    }

    // Copy
    public DebugTickCounterItem(DebugTickCounterItem copy) {
        super(copy);
        this.counter = copy.counter;
    }

    @Override
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        counter++;
        visibleChanged();
        tickSession.saveNeeded();
    }

    @Getter public int getCounter() { return counter; }
}
