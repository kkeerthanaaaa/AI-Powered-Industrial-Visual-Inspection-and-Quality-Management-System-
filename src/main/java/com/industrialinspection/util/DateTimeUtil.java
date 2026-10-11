package com.industrialinspection.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateTimeUtil {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static String formatTimestamp(Timestamp ts) {
        if (ts == null) return "";
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(ts);
    }

    public static Date parseDate(String str) {
        if (str == null || str.trim().isEmpty()) return new Date(System.currentTimeMillis());
        try {
            java.util.Date parsed = new SimpleDateFormat(DATE_FORMAT).parse(str.trim());
            return new Date(parsed.getTime());
        } catch (ParseException e) {
            return new Date(System.currentTimeMillis());
        }
    }

    public static Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Date currentDate() {
        return new Date(System.currentTimeMillis());
    }
}
