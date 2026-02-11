package com.symteo.global.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
    public static long getSecondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfToday = now.toLocalDate().atTime(23, 59, 59);
        return Math.max(ChronoUnit.SECONDS.between(now, endOfToday), 0);
    }
}