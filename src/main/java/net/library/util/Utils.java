package net.library.util;

import net.library.exception.FilterLengthException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Utils {

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static <T extends Enum<T>> T convertToEnum(String value, Class<T> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String deleteSpacesHyphens(String value) {
        return value.replace(" ", "").replace("-", "").replace("+", "");
    }

    public static LocalDateTime stringToLocalDateConverter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        return LocalDateTime.parse(value, dateTimeFormatter);
    }

    public static boolean isLengthValid(String value, int min, int max) {
        var length = value.length();
        return length >= min && length <= max;
    }

    public static void isLength(String value, int min) {
        if (value == null || value.isEmpty() || value.isBlank()) {
            return;
        }
        var length = value.length();
        if (length < min) {
            throw new FilterLengthException("filer length is less than 3 letters");
        }
    }
    public static LocalDateTime currentDate() {
        return LocalDateTime.now();
    }
}