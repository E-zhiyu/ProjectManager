package com.sly.coffer.auxiliary.classes;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class CustomDateTimeFormatter {
    public static final DateTimeFormatter BACKUP = DateTimeFormatter.ofPattern("yyyyMMdd(HHmmss)");
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter LOCAL_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    public static final DateTimeFormatter DATE_WITH_WEEK = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE");
    public static final DateTimeFormatter DATE_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
}
