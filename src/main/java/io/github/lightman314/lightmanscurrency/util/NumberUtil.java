package io.github.lightman314.lightmanscurrency.util;

import java.text.DecimalFormat;

public class NumberUtil {

    public static String GetPrettyString(int count) { return new DecimalFormat().format(count); }

    public static boolean IsIntegerOrEmpty(String text) { return text.isEmpty() || IsInteger(text); }

    public static boolean IsInteger(String text)
    {
        if(text == null) return false;
        try { int i = Integer.parseInt(text);
        } catch(NumberFormatException nfe) { return false; }
        return true;
    }

    public static int GetIntegerValue(String text, int defaultValue)
    {
        if(IsInteger(text))
            return Integer.parseInt(text);
        return defaultValue;
    }

    public static boolean IsLongOrEmpty(String text) { return text.isEmpty() || IsLong(text); }

    public static boolean IsLong(String text)
    {
        if(text == null) return false;
        try { long i = Long.parseLong(text);
        } catch(NumberFormatException nfe) { return false; }
        return true;
    }

    public static long GetLongValue(String text, long defaultValue)
    {
        if(IsLong(text))
            return Long.parseLong(text);
        return defaultValue;
    }

    public static boolean IsFloatOrEmpty(String text) { return text.isEmpty() || IsFloat(text); }

    public static boolean IsFloat(String text)
    {
        if(text == null) return false;
        try { float f = Float.parseFloat(text);
        } catch (NumberFormatException e) { return false; }
        return true;
    }

    public static float GetFloatValue(String text, float defaultValue)
    {
        if(IsFloat(text))
            return Float.parseFloat(text);
        return defaultValue;
    }

    public static boolean IsDoubleOrEmpty(String text) { return text.isEmpty() || IsDouble(text); }

    public static boolean IsDouble(String text)
    {
        if(text == null) return false;
        try { double i = Double.parseDouble(text);
        } catch(NumberFormatException nfe) { return false; }
        return true;
    }

    public static double GetDoubleValue(String text, double defaultValue)
    {
        if(IsDouble(text))
            return Double.parseDouble(text);
        return defaultValue;
    }

}