package com.manager.assistant.auxiliary.classes;

import java.time.format.DateTimeFormatter;

public class CustomDateTimeFormatter {
    public static final DateTimeFormatter BACKUP = DateTimeFormatter.ofPattern("yyyyMMdd(HHmmss)");
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_WITH_WEEK = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE");
    public static final DateTimeFormatter DATE_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
}
