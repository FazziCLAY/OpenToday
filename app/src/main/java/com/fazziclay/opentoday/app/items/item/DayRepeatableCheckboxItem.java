package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.TickSession;
import com.fazziclay.opentoday.util.annotation.Getter;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;
import com.fazziclay.opentoday.util.annotation.Setter;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DayRepeatableCheckboxItem extends CheckboxItem {
    // START - Save
    public final static DayRepeatableCheckboxItemIETool IE_TOOL = new DayRepeatableCheckboxItemIETool();
    public static class DayRepeatableCheckboxItemIETool extends CheckboxItem.CheckboxItemIETool {
        @NonNull
        @Override
        public JSONObject exportItem(@NonNull Item item) throws Exception {
            DayRepeatableCheckboxItem dayRepeatableCheckboxItem = (DayRepeatableCheckboxItem) item;
            return super.exportItem(dayRepeatableCheckboxItem)
                    .put("startValue", dayRepeatableCheckboxItem.startValue)
                    .put("latestDayOfYear", dayRepeatableCheckboxItem.latestDayOfYear);
        }

        private final DayRepeatableCheckboxItem defaultValues = new DayRepeatableCheckboxItem();
        @NonNull
        @Override
        public Item importItem(@NonNull JSONObject json, Item item) throws Exception {
            DayRepeatableCheckboxItem dCheckboxItem = item != null ? (DayRepeatableCheckboxItem) item : new DayRepeatableCheckboxItem();
            super.importItem(json, dCheckboxItem);
            dCheckboxItem.startValue = json.optBoolean("startValue", defaultValues.startValue);
            dCheckboxItem.latestDayOfYear = json.optInt("latestDayOfYear", defaultValues.latestDayOfYear);
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
    public void setChecked(boolean s) {
        latestDayOfYear = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
        super.setChecked(s);
    }

    @Override
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        int dayOfYear = tickSession.getGregorianCalendar().get(Calendar.DAY_OF_YEAR);
        if (dayOfYear != latestDayOfYear) {
            latestDayOfYear = dayOfYear;
            if (isChecked() != startValue) {
                setChecked(startValue);
                visibleChanged();
                tickSession.saveNeeded();
            }
        }
    }

    @Getter public boolean getStartValue() { return startValue; }
    @Setter public void setStartValue(boolean v) { this.startValue = v; }
    @Getter public int getLatestDayOfYear() { return latestDayOfYear; }
    @Setter public void setLatestDayOfYear(int v) { this.latestDayOfYear = v; }
}
