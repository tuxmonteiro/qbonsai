package dev.tuxmonteiro.qbonsai.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ConvertUtils {

    public static Instant getInstantFromString(String instantStr, ZoneOffset zoneOffset) {
        if (instantStr.matches(".*[A-Za-z:.-].*")) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            LocalDateTime localDateTime = LocalDateTime.parse(instantStr, formatter);
            return localDateTime.toInstant(zoneOffset);
        }
        long instantLong = Long.parseLong(instantStr);
        return getInstantFromLong(instantLong);
    }

    public static Instant getInstantFromString(String instantStr) {
        return getInstantFromString(instantStr, ZoneOffset.UTC);
    }

    public static Instant getInstantFromLong(long instantLong) {
        long instantLongNano = 0L;
        if (instantLong > 9999999999999L) {
            instantLong = instantLong / 1000L;
            instantLongNano = instantLong % 1000L;
        }
        return Instant.ofEpochMilli(instantLong).plusNanos(instantLongNano);
    }
}
