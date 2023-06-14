package com.fazziclay.opentoday.app.items.item.filter;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateItemFilter extends ItemFilter implements Cloneable {
    public static final FilterCodec CODEC = new DateItemFilterCodec();
    private static class DateItemFilterCodec extends FilterCodec {
        private final static String KEY_DESCRIPTION = "description";

        @NonNull
        @Override
        public Cherry exportFilter(@NonNull ItemFilter filter) {
            DateItemFilter f = (DateItemFilter) filter;
            return new Cherry()
                    .put(KEY_DESCRIPTION, f.description)
                    .put("year", f.year == null ? null : f.year.exportCherry())
                    .put("month", f.month == null ? null : f.month.exportCherry())
                    .put("dayOfWeek", f.dayOfWeek == null ? null : f.dayOfWeek.exportCherry())
                    .put("dayOfMonth", f.dayOfMonth == null ? null : f.dayOfMonth.exportCherry())
                    .put("weekOfYear", f.weekOfYear == null ? null : f.weekOfYear.exportCherry())
                    .put("dayOfYear", f.dayOfYear == null ? null : f.dayOfYear.exportCherry())
                    .put("hour", f.hour == null ? null : f.hour.exportCherry())
                    .put("minute", f.minute == null ? null : f.minute.exportCherry())
                    .put("second", f.second == null ? null : f.second.exportCherry());
        }

        @NonNull
        @Override
        public ItemFilter importFilter(@NonNull Cherry cherry, ItemFilter d) {
            DateItemFilter i = new DateItemFilter();

            i.description = cherry.optString(KEY_DESCRIPTION, i.description);
            i.year = IntegerValue.importCherry(cherry.getCherry("year"));
            i.month = IntegerValue.importCherry(cherry.getCherry("month"));
            i.dayOfWeek = IntegerValue.importCherry(cherry.getCherry("dayOfWeek"));
            i.dayOfMonth = IntegerValue.importCherry(cherry.getCherry("dayOfMonth"));
            i.weekOfYear = IntegerValue.importCherry(cherry.getCherry("weekOfYear"));
            i.dayOfYear = IntegerValue.importCherry(cherry.getCherry("dayOfYear"));
            i.hour = IntegerValue.importCherry(cherry.getCherry("hour"));
            i.minute = IntegerValue.importCherry(cherry.getCherry("minute"));
            i.second = IntegerValue.importCherry(cherry.getCherry("second"));

            return i;
        }
    }

    private String description = "";
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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String s) {
        if (description == null) throw new NullPointerException("description can't be null!");
        description = s;
    }

    private boolean check(Calendar calendar, IntegerValue integerValue, int field) {
        if (integerValue != null) {
            return integerValue.isFit(calendar.get(field));
        }
        return true;
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
