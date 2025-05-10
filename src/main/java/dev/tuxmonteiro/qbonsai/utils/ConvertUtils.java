package dev.tuxmonteiro.qbonsai.utils;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
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

    public static Object getClassFromString(String className, boolean logError) {
        try {
            Class<?> myClass = Class.forName(className);
            Constructor<?> constructor = myClass.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException |
                ClassNotFoundException |
                InvocationTargetException |
                InstantiationException |
                IllegalAccessException e) {
            if (logError) log.error(e.getMessage(), e);
        }
        return null;
    }
}
