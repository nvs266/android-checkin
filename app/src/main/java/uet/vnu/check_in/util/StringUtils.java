package uet.vnu.check_in.util;

public class StringUtils {

    private StringUtils() {

    }

    public static boolean checkNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
