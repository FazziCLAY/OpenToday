package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DayRepeatableCheckboxItem extends CheckboxItem {
    private static final boolean DEBUG_RANDOM_STATE_CHANGES = false; // NO NOT TOUCH! RANDOM STATES ARE SAVED TO FILE!!!! DEFAULT & NORMAL VALUE = FALSE!
    // START - Save
    public final static DayRepeatableCheckboxItemCodec CODEC = new DayRepeatableCheckboxItemCodec();
    public static class DayRepeatableCheckboxItemCodec extends CheckboxItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            DayRepeatableCheckboxItem dayRepeatableCheckboxItem = (DayRepeatableCheckboxItem) item;
            return super.exportItem(dayRepeatableCheckboxItem)
                    .put("startValue", dayRepeatableCheckboxItem.startValue)
                    .put("latestDayOfYear", dayRepeatableCheckboxItem.latestDayOfYear);
        }

        private final DayRepeatableCheckboxItem defaultValues = new DayRepeatableCheckboxItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            DayRepeatableCheckboxItem dCheckboxItem = item != null ? (DayRepeatableCheckboxItem) item : new DayRepeatableCheckboxItem();
            super.importItem(cherry, dCheckboxItem);
            dCheckboxItem.startValue = cherry.optBoolean("startValue", defaultValues.startValue);
            dCheckboxItem.latestDayOfYear = cherry.optInt("latestDayOfYear", defaultValues.latestDayOfYear);
            return dCheckboxItem;
        }
    }
    // END - Save

    @NonNull
    public static DayRepeatableCheckboxItem createEmpty() {
        return new DayRepeatableCheckboxItem();
    }

    @SaveKey(key = "startValue") @RequireSave private boolean startValue;
    @SaveKey(key = "latestDayOfYear") @RequireSave private int latestDayOfYear;

    protected DayRepeatableCheckboxItem() {
        super();
    }

    // Full
    public DayRepeatableCheckboxItem(String text, boolean checked, boolean startValue, int latestDayOfYear) {
        super(text, checked);
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
    public ItemType getItemType() {
        return ItemType.CHECKBOX_DAY_REPEATABLE;
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
            profPush(tickSession, "checkbox_day_repeatable_update");
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
            profPop(tickSession);
        }
    }

    @Getter public boolean getStartValue() { return startValue; }
    @Setter public void setStartValue(boolean v) { this.startValue = v; }
    @Getter public int getLatestDayOfYear() { return latestDayOfYear; }
    @Setter public void setLatestDayOfYear(int v) { this.latestDayOfYear = v; }
}
