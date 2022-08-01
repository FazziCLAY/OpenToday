package ru.fazziclay.opentoday.app.items;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.fazziclay.opentoday.annotation.GGGetter;
import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;

public class DayRepeatableCheckboxItem extends CheckboxItem {
    protected final static DayRepeatableCheckboxItemIETool IE_TOOL = new DayRepeatableCheckboxItemIETool();
    protected static class DayRepeatableCheckboxItemIETool extends CheckboxItem.CheckboxItemIETool {
        @Override
        protected JSONObject exportItem(Item item) throws Exception {
            DayRepeatableCheckboxItem dayRepeatableCheckboxItem = (DayRepeatableCheckboxItem) item;
            return super.exportItem(dayRepeatableCheckboxItem)
                    .put("startValue", dayRepeatableCheckboxItem.startValue)
                    .put("latestDayOfYear", dayRepeatableCheckboxItem.latestDayOfYear);
        }

        private final DayRepeatableCheckboxItem defaultValues = new DayRepeatableCheckboxItem("<import_error>", false, false, 0);
        @Override
        protected Item importItem(JSONObject json) throws Exception {
            return new DayRepeatableCheckboxItem((CheckboxItem) super.importItem(json), json.optBoolean("startValue", defaultValues.startValue), json.optInt("latestDayOfYear", defaultValues.latestDayOfYear));
        }
    }

    @JSONName(name = "startValue") @RequireSave protected boolean startValue;
    @JSONName(name = "latestDayOfYear") @RequireSave protected int latestDayOfYear;

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

    @GGGetter public boolean getStartValue() { return startValue; }
    @Setter public void setStartValue(boolean v) { this.startValue = v; }
    @GGGetter public int getLatestDayOfYear() { return latestDayOfYear; }
    @Setter public void setLatestDayOfYear(int v) { this.latestDayOfYear = v; }

    @Override
    public void tick() {
        super.tick();
        int dayOfYear = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
        if (dayOfYear != latestDayOfYear) {
            latestDayOfYear = dayOfYear;
            setChecked(startValue);
        }
    }
}
