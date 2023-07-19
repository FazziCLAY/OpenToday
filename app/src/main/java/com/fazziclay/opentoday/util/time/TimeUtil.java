package com.fazziclay.opentoday.util.time;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.OptionalField;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Utility class, Utils of Time
 *
 * @see TimeUtil#getHumanValue(long, HumanTimeType)
 * @see TimeUtil#convertToHumanTime(int, ConvertMode)
 * @see TimeUtil#toFixed(int, int)
 * @see ConvertMode
 * @see HumanTimeType
 *
 * @author FazziCLAY
 * **/
public class TimeUtil {
    public static final OptionalField<SimpleDateFormat> SIMPLE_DATE_FORMAT = new OptionalField<>(() -> new SimpleDateFormat("yyyy.MM.dd EE HH:mm:ss SSS Z", Locale.US));
    public static final int SECONDS_IN_MINUTE = 60;
    public static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
    public static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
    public static final int SECONDS_IN_WEEK = SECONDS_IN_DAY * 7;

    /**
     * @param seconds seconds
     * @param timeType type of time
     * @return value
     * @see HumanTimeType
     * **/
    public static int getHumanValue(long seconds, HumanTimeType timeType) {
        if (timeType == HumanTimeType.SECONDS_OF_MINUTE) {
            return (int) (seconds % 60);
        } else if (timeType == HumanTimeType.MINUTE_OF_HOUR) {
            return (int) ((seconds % 3600) / 60);
        } else if (timeType == HumanTimeType.HOUR) {
            return (int) (seconds / 3600);
        }
        throw new RuntimeException("Unknown timeType:" + timeType.name());
    }

    /**
     * seconds to 00:00:00
     * <p>use convertMode</p>
     *
     * @param convertMode see {@link ConvertMode}
     * **/
    @NonNull
    public static String convertToHumanTime(int totalSeconds, ConvertMode convertMode) {
        final String SEPARATOR = ":";
        final int FIXED = 2;

        int hours = getHumanValue(totalSeconds, HumanTimeType.HOUR);
        int minutes = getHumanValue(totalSeconds, HumanTimeType.MINUTE_OF_HOUR);
        int seconds = getHumanValue(totalSeconds, HumanTimeType.SECONDS_OF_MINUTE);

        if (convertMode == ConvertMode.HHMMSS) {
            return toFixed(hours, FIXED) + SEPARATOR + toFixed(minutes, FIXED) + SEPARATOR + toFixed(seconds, FIXED);
        } else if (convertMode == ConvertMode.hhMMSS) {
            return ((hours > 0 ? toFixed(hours, FIXED) + SEPARATOR : "")) + toFixed(minutes, FIXED) + SEPARATOR + toFixed(seconds, FIXED);
        } else if (convertMode == ConvertMode.HHMM) {
            return toFixed(hours, FIXED) + SEPARATOR + toFixed(minutes, FIXED);
        }
        throw new RuntimeException("Unknown convertMode: " + convertMode.name());
    }

    @NonNull
    public static String toFixed(int number, int fixedLength) {
        return String.format("%0"+fixedLength+"d", number);
    }

    public static int getDaySeconds() {
        Calendar current = new GregorianCalendar();
        Calendar currentDay = new GregorianCalendar(
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH));
        return (int) ((current.getTimeInMillis() - currentDay.getTimeInMillis()) / 1000);
    }

    public static int getWeekSeconds() {
        return WeekTimeUtil.getWeekSeconds();
    }

    public static long getUnixSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getDebugDate(long t) {
        return SIMPLE_DATE_FORMAT.get().format(t);
    }

    public static String getDebugDate() {
        return SIMPLE_DATE_FORMAT.get().format(System.currentTimeMillis());
    }

    public static void free() {
        SIMPLE_DATE_FORMAT.free();
    }

    public static GregorianCalendar noTimeCalendar(GregorianCalendar gregorianCalendar) {
        return new GregorianCalendar(
                gregorianCalendar.get(Calendar.YEAR),
                gregorianCalendar.get(Calendar.MONTH),
                gregorianCalendar.get(Calendar.DAY_OF_MONTH));
    }
}
