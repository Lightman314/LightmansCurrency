package io.github.lightman314.lightmanscurrency.util;

import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;

public class NumberUtil {

    public static String GetPrettyString(int count) { return new DecimalFormat().format(count); }

    public static boolean IsInteger(String text)
    {
        if(text == null)
            return false;
        try
        {
            @SuppressWarnings("unused")
            int i = Integer.parseInt(text);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static int GetIntegerValue(String text, int defaultValue)
    {
        if(IsInteger(text))
            return Integer.parseInt(text);
        return defaultValue;
    }

    public static String getAsStringOfLength(int i, int length)
    {
        StringBuilder value = new StringBuilder(Integer.toString(i));
        while(value.length() < length)
            value.insert(0, "0");
        return value.toString();
    }

    public static String prettyInteger(int i) { return new DecimalFormat().format(i); }

}