package ru.fazziclay.opentoday.app.items.item;

import org.json.JSONObject;

import java.util.Calendar;

import ru.fazziclay.opentoday.annotation.Getter;
import ru.fazziclay.opentoday.annotation.JSONName;
import ru.fazziclay.opentoday.annotation.RequireSave;
import ru.fazziclay.opentoday.annotation.Setter;
import ru.fazziclay.opentoday.app.TickSession;

public class DayRepeatableCheckboxItem extends CheckboxItem {
    // START - Save
    public final static DayRepeatableCheckboxItemIETool IE_TOOL = new DayRepeatableCheckboxItemIETool();
    public static class DayRepeatableCheckboxItemIETool extends CheckboxItem.CheckboxItemIETool {
        @Override
        public JSONObject exportItem(Item item) throws Exception {
            DayRepeatableCheckboxItem dayRepeatableCheckboxItem = (DayRepeatableCheckboxItem) item;
            return super.exportItem(dayRepeatableCheckboxItem)
                    .put("startValue", dayRepeatableCheckboxItem.startValue)
                    .put("latestDayOfYear", dayRepeatableCheckboxItem.latestDayOfYear);
        }

        private final DayRepeatableCheckboxItem defaultValues = new DayRepeatableCheckboxItem("<import_error>", false, false, 0);
        @Override
        public Item importItem(JSONObject json) throws Exception {
            return new DayRepeatableCheckboxItem((CheckboxItem) super.importItem(json), json.optBoolean("startValue", defaultValues.startValue), json.optInt("latestDayOfYear", defaultValues.latestDayOfYear));
        }
    }
    // END - Save

    public static DayRepeatableCheckboxItem createEmpty() {
        return new DayRepeatableCheckboxItem("", false, false, 0);
    }

    @JSONName(name = "startValue") @RequireSave private boolean startValue;
    @JSONName(name = "latestDayOfYear") @RequireSave private int latestDayOfYear;

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
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        int dayOfYear = tickSession.getGregorianCalendar().get(Calendar.DAY_OF_YEAR);
        if (dayOfYear != latestDayOfYear) {
            latestDayOfYear = dayOfYear;
            setChecked(startValue);
            updateUi();
        }
    }

    @Getter public boolean getStartValue() { return startValue; }
    @Setter public void setStartValue(boolean v) { this.startValue = v; }
    @Getter public int getLatestDayOfYear() { return latestDayOfYear; }
    @Setter public void setLatestDayOfYear(int v) { this.latestDayOfYear = v; }
}
