package io.github.lightman314.lightmanscurrency.util;

public class EnumUtil {

	public static <T extends Enum<?>> T enumFromOrdinal(int ordinal, T[] allValues, T defaultValue)
	{
		for(T val : allValues)
		{
			if(val.ordinal() == ordinal)
				return val;
		}
		return defaultValue;
	}

	public static <T extends Enum<?>> T enumFromString(String string, T[] allValues, T defaultValue)
	{
		for(T val : allValues)
		{
			if(val.toString().equalsIgnoreCase(string))
				return val;
		}
		return defaultValue;
	}
	
}
