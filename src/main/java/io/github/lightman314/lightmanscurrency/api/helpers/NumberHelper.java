package io.github.lightman314.lightmanscurrency.api.helpers;

import javax.annotation.Nullable;

public final class NumberHelper {

    private NumberHelper() {}

    public static boolean isIntegerOrEmpty(String text) { return text.isEmpty() || isInteger(text); }
    public static boolean isInteger(String text) {
        try { int i = Integer.parseInt(text);
        } catch (NumberFormatException e) { return false; }
        return true;
    }
    @Nullable
    public static Integer getInteger(String text, @Nullable Integer defaultValue) {
        if(isInteger(text))
            return Integer.parseInt(text);
        return defaultValue;
    }

    public static boolean isLongOrEmpty(String text) { return text.isEmpty() || isLong(text); }
    public static boolean isLong(String text) {
        try { long i = Long.parseLong(text);
        } catch (NumberFormatException e) { return false; }
        return true;
    }
    @Nullable
    public static Long getLong(String text,@Nullable Long defaultValue) {
        if(isLong(text))
            return Long.parseLong(text);
        return defaultValue;
    }

    public static boolean isFloatOrEmpty(String text) { return text.isEmpty() || isFloat(text); }
    public static boolean isFloat(String text) {
        try { float i = Float.parseFloat(text);
        } catch (NumberFormatException e) { return false; }
        return true;
    }
    @Nullable
    public static Float getFloat(String text,@Nullable Float defaultValue) {
        if(isFloat(text))
            return Float.parseFloat(text);
        return defaultValue;
    }

    public static boolean isDoubleOrEmpty(String text) { return text.isEmpty() || isDouble(text); }
    public static boolean isDouble(String text) {
        try { double i = Double.parseDouble(text);
        } catch (NumberFormatException e) { return false; }
        return true;
    }
    @Nullable
    public static Double getDouble(String text,@Nullable Double defaultValue) {
        if(isDouble(text))
            return Double.parseDouble(text);
        return defaultValue;
    }

}