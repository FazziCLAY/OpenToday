package ru.fazziclay.opentoday.util.time;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Utility class, Предназначен для удобной работы со временем
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
    public static final int SECONDS_IN_MINUTE = 60;
    public static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
    public static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
    public static final int SECONDS_IN_WEEK = SECONDS_IN_DAY * 7;

    /**
     * Получить человеческое значение из секунд
     * @param seconds секунды которые нужно преобразовать
     * @param timeType что хотим получить
     * @return желаемое человеческое значение
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
     * Преобразует секунды в человеческий вид времени 00:00:00
     * <p>Итоговый вид изменяется через параметр convertMode</p>
     *
     * @param convertMode Изменяет итовоый вид времени, подробнее в {@link ConvertMode}
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

    /**
     * Возвращает число строкой с фиксировонной длинной
     * <p>Например: <p><code>toFixed(9, 3).equals("009") == true</code></p></p>
     * @param number Число которое нужно преобразовать в строку
     * @param fixedLength Фиксированная длинна строки, начало будет заполнятся нулями
     * @return Цифра в виде строки
     * @see String#format(String, Object...)
     */
    @NonNull
    public static String toFixed(int number, int fixedLength) {
        return String.format("%0"+fixedLength+"d", number);
    }

    /**
     * <p>Диапазон выдачи <code>0 <= x <= 86400 //(24*60*60)</code></p>
     * @return количество секунд прошедших с начала суток
     * **/
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
}
