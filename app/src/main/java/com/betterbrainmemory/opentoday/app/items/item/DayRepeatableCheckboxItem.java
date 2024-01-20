package com.betterbrainmemory.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.betterbrainmemory.opentoday.app.data.Cherry;
import com.betterbrainmemory.opentoday.app.items.tick.TickSession;
import com.betterbrainmemory.opentoday.app.items.tick.TickTarget;
import com.betterbrainmemory.opentoday.util.RandomUtil;
import com.betterbrainmemory.opentoday.util.annotation.Getter;
import com.betterbrainmemory.opentoday.util.annotation.Setter;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DayRepeatableCheckboxItem extends CheckboxItem {
    private static final String TAG = "DayRepeatableCheckboxItem";
    private static final boolean DEBUG_RANDOM_STATE_CHANGES = false; // NO NOT TOUCH! RANDOM STATES ARE SAVED TO FILE!!!! DEFAULT & NORMAL VALUE = FALSE!

    public static final DayRepeatableCheckboxItemCodec CODEC = new DayRepeatableCheckboxItemCodec();
    public static final ItemFactory<DayRepeatableCheckboxItem> FACTORY = new DayRepeatableCheckboxItemFactory();


    private boolean startValue;
    private int latestDayOfYear;

    public DayRepeatableCheckboxItem() {
        super();
    }

    // Full
    public DayRepeatableCheckboxItem(String text, boolean checked, boolean startValue, int latestDayOfYear) {
        super(text, checked);
        this.startValue = startValue;
        this.latestDayOfYear = latestDayOfYear;
    }

    // Append
    public DayRepeatableCheckboxItem(TextItem textItem, boolean checked, boolean startValue, int latestDayOfYear) {
        super(textItem, checked);
        this.startValue = startValue;
        this.latestDayOfYear = latestDayOfYear;
    }

    // Append
    public DayRepeatableCheckboxItem(CheckboxItem checkboxItem, boolean startValue, int latestDayOfYear) {
        super(checkboxItem);
        this.startValue = startValue;
        this.latestDayOfYear = latestDayOfYear;
    }

    // Copy
    public DayRepeatableCheckboxItem(DayRepeatableCheckboxItem copy) {
        super(copy);
        this.startValue = copy.startValue;
        this.latestDayOfYear = copy.latestDayOfYear;
    }

    @Override
    public void setChecked(boolean s) {
        latestDayOfYear = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
        super.setChecked(s);
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;

        super.tick(tickSession);
        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_DAY_REPEATABLE_CHECKBOX_UPDATE)) {
            profilerPush(tickSession, "checkbox_day_repeatable_update");
            int dayOfYear = tickSession.getGregorianCalendar().get(Calendar.DAY_OF_YEAR);
            if (dayOfYear != latestDayOfYear) {
                latestDayOfYear = dayOfYear;
                if (isChecked() != startValue) {
                    setChecked(startValue);
                    visibleChanged();
                    tickSession.saveNeeded();
                }
            }
            if (DEBUG_RANDOM_STATE_CHANGES) {
                boolean nextValue = RandomUtil.nextBoolean();
                if (isChecked() != nextValue) {
                    setChecked(nextValue);
                    visibleChanged();
                    tickSession.saveNeeded();
                }
            }
            profilerPop(tickSession);
        }
    }

    @Getter public boolean getStartValue() { return startValue; }
    @Setter public void setStartValue(boolean v) { this.startValue = v; }
    @Getter public int getLatestDayOfYear() { return latestDayOfYear; }
    @Setter public void setLatestDayOfYear(int v) { this.latestDayOfYear = v; }



    // Import - Export - Factory
    public static class DayRepeatableCheckboxItemCodec extends CheckboxItemCodec {
        private static final String KEY_START_VALUE = "checkbox_start_value";
        private static final String KEY_LATEST_DAY_OF_YEAR = "checkbox_latest_day_of_year";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            DayRepeatableCheckboxItem dayRepeatableCheckboxItem = (DayRepeatableCheckboxItem) item;
            return super.exportItem(dayRepeatableCheckboxItem)
                    .put(KEY_START_VALUE, dayRepeatableCheckboxItem.startValue)
                    .put(KEY_LATEST_DAY_OF_YEAR, dayRepeatableCheckboxItem.latestDayOfYear);
        }

        private final DayRepeatableCheckboxItem DEFAULT_VALUES = new DayRepeatableCheckboxItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            var drcb = fallback(item, DayRepeatableCheckboxItem::new);
            super.importItem(cherry, drcb);

            drcb.startValue = cherry.optBoolean(KEY_START_VALUE, DEFAULT_VALUES.startValue);
            drcb.latestDayOfYear = cherry.optInt(KEY_LATEST_DAY_OF_YEAR, DEFAULT_VALUES.latestDayOfYear);
            return drcb;
        }
    }

    private static class DayRepeatableCheckboxItemFactory implements ItemFactory<DayRepeatableCheckboxItem> {
        @Override
        public DayRepeatableCheckboxItem create() {
            return new DayRepeatableCheckboxItem();
        }

        @Override
        public DayRepeatableCheckboxItem copy(Item item) {
            return new DayRepeatableCheckboxItem((DayRepeatableCheckboxItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof CheckboxItem checkboxItem) {
                return Transform.Result.allow(() -> new DayRepeatableCheckboxItem(checkboxItem, false, 0));

            } else if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new DayRepeatableCheckboxItem(textItem, false, false, 0));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
