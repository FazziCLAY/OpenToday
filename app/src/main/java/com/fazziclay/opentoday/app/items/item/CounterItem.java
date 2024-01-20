package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.Setter;

public class CounterItem extends TextItem {
    public final static CounterItemCodec CODEC = new CounterItemCodec();
    public static final ItemFactory<CounterItem> FACTORY = new CounterItemFactory();


    private double counter = 0;
    private double step = 1;

    public CounterItem() {
        super();
    }

    public CounterItem(@Nullable TextItem textItem) {
        super(textItem);
    }

    public CounterItem(CounterItem copy) {
        super(copy);
        this.counter = copy.counter;
        this.step = copy.step;
    }

    public void increase() {
        counter += step;
        visibleChanged();
        save();
    }

    public void decrease() {
        counter -= step;
        visibleChanged();
        save();
    }

    @Getter public double getCounter() { return counter; }
    @Getter public double getStep() { return step; }
    @Setter public void setCounter(double counter) { this.counter = counter; }
    @Setter public void setStep(double step) { this.step = step; }



    // Import - Export - Factory
    public static class CounterItemCodec extends TextItemCodec {
        private static final String KEY_COUNTER_VALUE = "counter_value";
        private static final String KEY_COUNTER_STEP = "counter_step";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            CounterItem counterItem = (CounterItem) item;
            return super.exportItem(item)
                    .put(KEY_COUNTER_VALUE, counterItem.counter)
                    .put(KEY_COUNTER_STEP, counterItem.step);
        }

        private final CounterItem DEFAULT_VALUES = new CounterItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            CounterItem counterItem = fallback(item, CounterItem::new);
            super.importItem(cherry, counterItem);

            counterItem.counter = cherry.optDouble(KEY_COUNTER_VALUE, DEFAULT_VALUES.counter);
            counterItem.step = cherry.optDouble(KEY_COUNTER_STEP, DEFAULT_VALUES.step);
            return counterItem;
        }
    }

    private static class CounterItemFactory implements ItemFactory<CounterItem> {
        @Override
        public CounterItem create() {
            return new CounterItem();
        }

        @Override
        public CounterItem copy(Item item) {
            return new CounterItem((CounterItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new CounterItem(textItem));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
