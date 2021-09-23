package io.github.lightman314.lightmanscurrency.util;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class TimeUtil {

	/**
	 * Gets the current system time in milliseconds
	 */
	public static long getCurrentTime()
	{
		return System.currentTimeMillis() + LightmansCurrency.PROXY.getTimeDesync();
	}
	
	/**
	 * Calculates if the compareTime is less than the duration before the current time.
	 * @param duration The duration of time (in milliseconds) 
	 * @param compareTime
	 * @return Returns true if the time is within the duration.
	 */
	public static boolean compareTime(long duration, long compareTime)
	{
		long ignoreTime = getCurrentTime() - duration;
		return compareTime >= ignoreTime;
	}
	
	public static long getDuration(long hours, long minutes, long seconds)
	{
		minutes += 60 * hours; //60 minutes in an hour
		seconds += 60 * minutes; //60 seconds in a minute
		return seconds * 1000;
	}
	
}

