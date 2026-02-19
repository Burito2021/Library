package net.library.util;

import jakarta.servlet.http.HttpServletRequest;
import net.library.exception.FilterLengthException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Utils {

    public static String getJsonErrorBody(String xCorrelationId, int statusCode, String message) {
        return String.format("{\"cid\": \"%s\", \"errorId\": %d, \"errorMsg\": \"%s\"}",
                xCorrelationId, statusCode, message);
    }

    public static String getClientIdentifier(HttpServletRequest request) {
        var xForwardedFor = request.getHeader("X-Forwarded-For");
        return xForwardedFor != null ? xForwardedFor.split(",")[0] : request.getRemoteAddr();
    }

    public static boolean isValidJwtFormat(String token) {
        var parts = token.split("\\.");
        if (parts.length != 3) return false;

        try {
            java.util.Base64.getUrlDecoder().decode(parts[0]); // header
            java.util.Base64.getUrlDecoder().decode(parts[1]); // payload
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String extractTokenFromHeader(String header) {
        return header.substring(7);
    }

    public static String hashToken(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            var hexString = new StringBuilder();
            for (byte b : hash) {
                var hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

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