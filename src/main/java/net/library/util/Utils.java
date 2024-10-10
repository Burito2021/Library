package net.library.util;

import java.util.UUID;

public class Utils {

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String deleteSpacesHyphens(String value) {
        return value.replace(" ", "").replace("-", "").replace("+", "");
    }
}
