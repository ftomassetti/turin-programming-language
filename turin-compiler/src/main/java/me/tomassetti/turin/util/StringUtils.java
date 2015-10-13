package me.tomassetti.turin.util;

public class StringUtils {

    public static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        if (s.length() == 1) {
            return s.toUpperCase();
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

}
