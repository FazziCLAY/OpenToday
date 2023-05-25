package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.tick.TickSession;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;

public class DebugTickCounterItem extends TextItem {
    // START - Save
    public final static DebugTickCounterItemCodec CODEC = new DebugTickCounterItemCodec();
    public static class DebugTickCounterItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            DebugTickCounterItem debugTickCounterItem = (DebugTickCounterItem) item;
            return super.exportItem(debugTickCounterItem)
                    .put("counter", debugTickCounterItem.counter);
        }

        private final DebugTickCounterItem defaultValues = new DebugTickCounterItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            DebugTickCounterItem debugTickCounterItem = item != null ? (DebugTickCounterItem) item : new DebugTickCounterItem();
            super.importItem(cherry, debugTickCounterItem);
            debugTickCounterItem.counter = cherry.optInt("counter", defaultValues.counter);
            return debugTickCounterItem;
        }
    }
    // END - Save

    @NonNull
    public static DebugTickCounterItem createEmpty() {
        return new DebugTickCounterItem("", 0);
    }

    @SaveKey(key = "counter") @RequireSave
    private int counter;

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

    @Getter
    public int getCounter() { return counter; }
}
