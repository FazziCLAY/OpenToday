package ru.fazziclay.opentoday.util.time;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class WeekTimeUtil {
    /**
     * Диапазон выдачи <code>0 <= x <= 604800 // ((24*60*60) * 7)</code>
     * @return количество секунд прошедших с начала недели
     * **/
    public static int getWeekSeconds() {
        Calendar current = new GregorianCalendar();
        int previousWeekDaysSeconds = TimeUtil.SECONDS_IN_DAY * (current.get(Calendar.DAY_OF_WEEK) - 1);
        return previousWeekDaysSeconds + TimeUtil.getDaySeconds();
    }
}
