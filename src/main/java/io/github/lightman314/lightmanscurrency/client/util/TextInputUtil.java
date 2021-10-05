package io.github.lightman314.lightmanscurrency.client.util;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class TextInputUtil {

	private static final String INTEGER_WHITELIST = "0123456789";
	private static final String FLOAT_WHITELIST = "0123456789.";
	
	public static boolean isInteger(TextFieldWidget textInput)
	{
		return isInteger(textInput.getText());
	}
	
	public static boolean isInteger(String text)
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
	
	public static int getIntegerValue(TextFieldWidget textInput)
	{
		return getIntegerValue(textInput, 0);
	}
	
	public static int getIntegerValue(TextFieldWidget textInput, int defaultValue)
	{
		if(isInteger(textInput))
			return Integer.parseInt(textInput.getText());
		return defaultValue;
	}
	
	public static boolean isLong(TextFieldWidget textInput)
	{
		return isLong(textInput.getText());
	}
	
	public static boolean isLong(String text)
	{
		if(text == null)
			return false;
		try
		{
			@SuppressWarnings("unused")
			long i = Long.parseLong(text);
		} 
		catch(NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}
	
	public static long getLongValue(TextFieldWidget textInput)
	{
		return getLongValue(textInput, 0);
	}
	
	public static long getLongValue(TextFieldWidget textInput, int defaultValue)
	{
		if(isLong(textInput))
			return Long.parseLong(textInput.getText());
		return defaultValue;
	}
	
	/**
	 * Also works for long values.
	 */
	public static void whitelistInteger(TextFieldWidget textInput)
	{
		whitelistText(textInput, INTEGER_WHITELIST);
	}
	
	/**
	 * Also works for long values.
	 */
	public static void whitelistInteger(TextFieldWidget textInput, long minValue, long maxValue)
	{
		whitelistText(textInput, INTEGER_WHITELIST);
		if(textInput.getText().length() > 0)
		{
			long currentValue = getLongValue(textInput);
			if(currentValue < minValue || currentValue > maxValue)
			{
				currentValue = MathUtil.clamp(currentValue, minValue, maxValue);
				textInput.setText(Long.toString(currentValue));
			}
		}
	}
	
	/**
	 * Also works for double values.
	 */
	public static void whitelistFloat(TextFieldWidget textInput)
	{
		whitelistText(textInput, FLOAT_WHITELIST);
	}
	
	public static void whitelistText(TextFieldWidget textInput, String allowedChars)
	{
		StringBuilder newText = new StringBuilder(textInput.getText());
		for(int i = 0; i < newText.length(); i++)
		{
			boolean allowed = false;
			for(int x = 0; x < allowedChars.length() && !allowed; x++)
			{
				if(allowedChars.charAt(x) == newText.charAt(i))
					allowed = true;
			}
			if(!allowed)
			{
				newText.deleteCharAt(i);
			}
		}
		textInput.setText(newText.toString());
	}
	
}
