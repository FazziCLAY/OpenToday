package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;

public class CounterItem extends TextItem {
    // START - Save
    public final static CounterItemCodec CODEC = new CounterItemCodec();
    public static class CounterItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            CounterItem counterItem = (CounterItem) item;
            return super.exportItem(item)
                    .put("counter", counterItem.counter)
                    .put("step", counterItem.step);
        }

        private final CounterItem defaultValues = new CounterItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            CounterItem counterItem = item != null ? (CounterItem) item : new CounterItem();
            super.importItem(cherry, counterItem);
            counterItem.counter = cherry.optDouble("counter", defaultValues.counter);
            counterItem.step = cherry.optDouble("step", defaultValues.step);
            return counterItem;
        }
    }
    // END - Save

    @NonNull
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

    @Override
    public ItemType getItemType() {
        return ItemType.COUNTER;
    }

    public void up() {
        counter = counter + step;
        visibleChanged();
        save();
    }

    public void down() {
        counter = counter - step;
        visibleChanged();
        save();
    }

    @Getter public double getCounter() { return counter; }
    @Getter public double getStep() { return step; }
    @Setter public void setCounter(double counter) { this.counter = counter; }
    @Setter public void setStep(double step) { this.step = step; }
}
