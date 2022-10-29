package ru.fazziclay.opentoday.util;

import android.view.View;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

// Utility-class
// USE: import static ru.fazziclay.lifeschedule.util.InlineUtil.*;
public class InlineUtil {
    /**
     * Print message to System.in stream (System.out.println(o))
     * **/
    public static void l(Object o) {
        System.out.println(o);
    }

    /**
     * Set onClickListener for view
     * **/
    public static void fcu_viewOnClick(View v, Runnable r) {
        fcu_viewOnClick(v, ignore -> r.run());
    }

    /**
     * Set onClickListener for view
     * **/
    public static void fcu_viewOnClick(View v, View.OnClickListener r) {
        v.setOnClickListener(r);
    }

    /**
     * Если параметр calendar текущая дата
     * **/
    public static boolean fcu_isCalendarToday(GregorianCalendar calendar) {
        return fcu_isDateEqual(calendar, new GregorianCalendar());
    }

    /**
     * Если c1 совпадает с датой c2
     * **/
    public static boolean fcu_isDateEqual(GregorianCalendar c1, GregorianCalendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Получить UUID из 1 long цифры
     * **/
    public static UUID fcu_dufl(long l) {
        return new UUID(0, l);
    }
}
