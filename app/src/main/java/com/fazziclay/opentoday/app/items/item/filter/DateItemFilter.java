package com.fazziclay.opentoday.app.items.item.filter;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateItemFilter extends ItemFilter implements Cloneable {
    public static final FilterImportExportTool IE_TOOL = new FilterImportExportTool() {
        @Override
        public JSONObject exportFilter(ItemFilter filter) throws Exception {
            DateItemFilter f = (DateItemFilter) filter;
            JSONObject j = new JSONObject().put("filterType", "DateItemFilter");
            if (f.year != null) j.put("year", f.year.exportI());
            if (f.month != null) j.put("month", f.month.exportI());
            if (f.dayOfWeek != null) j.put("dayOfWeek", f.dayOfWeek.exportI());
            if (f.dayOfMonth != null) j.put("dayOfMonth", f.dayOfMonth.exportI());
            if (f.weekOfYear != null) j.put("weekOfYear", f.weekOfYear.exportI());
            if (f.dayOfYear != null) j.put("dayOfYear", f.dayOfYear.exportI());
            if (f.hour != null) j.put("hour", f.hour.exportI());
            if (f.minute != null) j.put("minute", f.minute.exportI());
            if (f.second != null) j.put("second", f.second.exportI());

            return j;
        }

        @Override
        public ItemFilter importFilter(JSONObject json, ItemFilter d) {
            DateItemFilter i = new DateItemFilter();

            i.year = IntegerValue.importI(json.optJSONObject("year"));
            i.month = IntegerValue.importI(json.optJSONObject("month"));
            i.dayOfWeek = IntegerValue.importI(json.optJSONObject("dayOfWeek"));
            i.dayOfMonth = IntegerValue.importI(json.optJSONObject("dayOfMonth"));
            i.weekOfYear = IntegerValue.importI(json.optJSONObject("weekOfYear"));
            i.dayOfYear = IntegerValue.importI(json.optJSONObject("dayOfYear"));
            i.hour = IntegerValue.importI(json.optJSONObject("hour"));
            i.minute = IntegerValue.importI(json.optJSONObject("minute"));
            i.second = IntegerValue.importI(json.optJSONObject("second"));

            return i;
        }
    };
    private IntegerValue year = null;
    private IntegerValue month = null;
    private IntegerValue dayOfMonth = null;
    private IntegerValue dayOfWeek = null;
    private IntegerValue weekOfYear = null;
    private IntegerValue dayOfYear = null;
    private IntegerValue hour = null;
    private IntegerValue minute = null;
    private IntegerValue second = null;


    @Override
    public boolean isFit(FitEquip fitEquip) {
        GregorianCalendar calendar = fitEquip.getGregorianCalendar();
        return check(calendar, year, Calendar.YEAR) &&
                check(calendar, month, Calendar.MONTH) &&
                check(calendar, dayOfMonth, Calendar.DAY_OF_MONTH) &&
                check(calendar, dayOfWeek, Calendar.DAY_OF_WEEK) &&
                check(calendar, dayOfYear, Calendar.DAY_OF_YEAR) &&
                check(calendar, weekOfYear, Calendar.WEEK_OF_YEAR) &&
                check(calendar, hour, Calendar.HOUR_OF_DAY) &&
                check(calendar, minute, Calendar.MINUTE) &&
                check(calendar, second, Calendar.SECOND);
    }

    @Override
    public ItemFilter copy() {
        try {
            return clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean check(Calendar calendar, IntegerValue integerValue, int field) {
        if (integerValue != null) {
            return integerValue.isFit(calendar.get(field));
        }
        return true;
    }

    public abstract static class Value implements Cloneable {
        private boolean isInvert = false;

        public boolean isInvert() {
            return isInvert;
        }

        public void setInvert(boolean invert) {
            isInvert = invert;
        }

        public JSONObject exportI() throws JSONException {
            return new JSONObject()
                    .put("isInvert", isInvert);
        }

        @NonNull
        @Override
        protected Value clone() throws CloneNotSupportedException {
            return (Value) super.clone();
        }
    }

    public static class IntegerValue extends Value implements Cloneable {
        private int shift = 0;
        private int value = 0;
        private String mode;

        public boolean isFit(int i) {
            boolean isFit = true;
            if (shift != 0) {
                i = i + shift;
            }

            if (mode == null) {
                isFit = i == value;
            } else {
                switch (mode) {
                    case "==":
                        isFit = i == value;
                        break;
                    case ">":
                        isFit = i > value;
                        break;
                    case "<":
                        isFit = i < value;
                        break;
                    case ">=":
                        isFit = i >= value;
                        break;
                    case "<=":
                        isFit = i <= value;
                        break;

                    case "%":
                        if (value != 0) {
                            isFit = i % value == 0;
                        } else {
                            isFit = false;
                        }
                        break;

                }
            }

            return (isInvert() != isFit);
        }

        public JSONObject exportI() throws JSONException {
            return super.exportI()
                    .put("value", value)
                    .put("mode", mode)
                    .put("shift", shift);
        }

        public static IntegerValue importI(JSONObject json) {
            if (json == null) {
                return null;
            }
            IntegerValue integerValue = new IntegerValue();
            integerValue.value = json.optInt("value", 0);
            integerValue.setInvert(json.optBoolean("isInvert", false));
            integerValue.mode = json.optString("mode", "==");
            integerValue.shift = json.optInt("shift", 0);
            return integerValue;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public int getShift() {
            return shift;
        }

        public void setShift(int shift) {
            this.shift = shift;
        }

        @NonNull
        @Override
        protected IntegerValue clone() throws CloneNotSupportedException {
            return (IntegerValue) super.clone();
        }
    }

    public IntegerValue getYear() {
        return year;
    }

    public void setYear(IntegerValue year) {
        this.year = year;
    }

    public IntegerValue getMonth() {
        return month;
    }

    public void setMonth(IntegerValue month) {
        this.month = month;
    }

    public IntegerValue getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(IntegerValue dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public IntegerValue getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(IntegerValue dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public IntegerValue getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(IntegerValue dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public IntegerValue getWeekOfYear() {
        return weekOfYear;
    }

    public void setWeekOfYear(IntegerValue weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public IntegerValue getHour() {
        return hour;
    }

    public void setHour(IntegerValue hour) {
        this.hour = hour;
    }

    public IntegerValue getMinute() {
        return minute;
    }

    public void setMinute(IntegerValue minute) {
        this.minute = minute;
    }

    public IntegerValue getSecond() {
        return second;
    }

    public void setSecond(IntegerValue second) {
        this.second = second;
    }

    @NonNull
    @Override
    public DateItemFilter clone() throws CloneNotSupportedException {
        DateItemFilter c = (DateItemFilter) super.clone();
        c.year = this.year != null ? this.year.clone() : null;
        c.month = this.month != null ? this.month.clone() : null;
        c.dayOfMonth = this.dayOfMonth != null ? this.dayOfMonth.clone() : null;
        c.dayOfYear = this.dayOfYear != null ? this.dayOfYear.clone() : null;
        c.dayOfWeek = this.dayOfWeek != null ? this.dayOfWeek.clone() : null;
        c.weekOfYear = this.weekOfYear != null ? this.weekOfYear.clone() : null;
        c.hour = this.hour != null ? this.hour.clone() : null;
        c.minute = this.minute != null ? this.minute.clone() : null;
        c.second = this.second != null ? this.second.clone() : null;

        return c;
    }
}
