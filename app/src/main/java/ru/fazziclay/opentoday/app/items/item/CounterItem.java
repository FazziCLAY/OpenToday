package ru.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.SaveKey;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;

public class CounterItem extends TextItem {
    // START - Save
    public final static CounterItemIETool IE_TOOL = new CounterItemIETool();
    public static class CounterItemIETool extends TextItem.TextItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            CounterItem counterItem = (CounterItem) item;
            return super.exportItem(item)
                    .put("counter", counterItem.counter)
                    .put("step", counterItem.step);
        }

        private final CounterItem defaultValues = new CounterItem("<import_error>");
        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            CounterItem counterItem = item != null ? (CounterItem) item : new CounterItem();
            super.importItem(json, counterItem);
            counterItem.counter = json.optDouble("counter", defaultValues.counter);
            counterItem.step = json.optDouble("step", defaultValues.step);
            return counterItem;
        }
    }
    // END - Save

    public static CounterItem createEmpty() {
        return new CounterItem("");
    }

    @SaveKey(key = "counter") @RequireSave private double counter = 0;
    @SaveKey(key = "step") @RequireSave private double step = 1;

    protected CounterItem() {}

    public CounterItem(String text) {
        super(text);
    }

    public CounterItem(TextItem textItem) {
        super(textItem);
    }

    public CounterItem(CounterItem copy) {
        super(copy);
        this.counter = copy.counter;
        this.step = copy.step;
    }

    public void up() {
        counter = counter + step;
        save();
        visibleChanged();
    }

    public void down() {
        counter = counter - step;
        save();
        visibleChanged();
    }

    @Getter public double getCounter() { return counter; }
    @Getter public double getStep() { return step; }
    @Setter public void setCounter(double counter) { this.counter = counter; }
    @Setter public void setStep(double step) { this.step = step; }
}
