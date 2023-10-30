package com.fazziclay.opentoday.util;

import android.view.View;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.util.profiler.Profiler;

import org.intellij.lang.annotations.MagicConstant;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

// Utility-class
// USE: import static com.fazziclay.opentoday.util.InlineUtil.*;
public class InlineUtil {
    public static final Profiler IPROF = App.createProfiler("InlineUtil_Profiler");
    /**
     * Print message to System.in stream (System.out.println(o))
     * **/
    public static void l(Object... o) {
        Logger.d("*InlineUtil* l()", o);
    }

    public static String nullStat(Object o) {
        return o == null ? "null" : "notnull";
    }

    public static String str(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString();
    }
    
    public static void viewVisible(@NonNull View v, boolean b, @MagicConstant(intValues = {View.GONE, View.INVISIBLE}) int c) {
        v.setVisibility(b ? View.VISIBLE : c);
    }

    public static void viewLong(View v, BooleanRunnable r) {
        viewLong(v, ignore -> r.run());
    }

    public static void viewLong(View v, Runnable r) {
        viewLong(v, r == null ? null : ignore -> {
            r.run();
            return true;
        });
    }

    public static void viewLong(View v, View.OnLongClickListener r) {
        v.setOnLongClickListener(r);
    }


    /**
     * Set onClickListener for view
     * **/
    public static void viewClick(View v, Runnable r) {
        viewClick(v, r == null ? null : ignore -> r.run());
    }

    /**
     * Set onClickListener for view
     * **/
    public static void viewClick(View v, View.OnClickListener r) {
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
