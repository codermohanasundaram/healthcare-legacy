package com.healthcare.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date helper methods used across servlets and JSPs.
 */
public class DateUtil {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DISPLAY_PATTERN = "dd-MMM-yyyy";

    // shared formatters (created once for performance)
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
    private static SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_PATTERN);

    public static Date parseDate(String value) {
        if (value == null || value.trim().equals("")) {
            return null;
        }
        try {
            dateFormat.setLenient(false);
            return dateFormat.parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String formatDate(Date date) {
        if (date == null) return "";
        return dateFormat.format(date);
    }

    public static String displayDate(Date date) {
        if (date == null) return "";
        return displayFormat.format(date);
    }

    public static java.sql.Date toSqlDate(Date date) {
        if (date == null) return null;
        return new java.sql.Date(date.getTime());
    }

    /** Today's date with the time portion cleared. */
    public static Date today() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String currentMonth() {
        return new SimpleDateFormat("yyyy-MM").format(new Date());
    }
}
