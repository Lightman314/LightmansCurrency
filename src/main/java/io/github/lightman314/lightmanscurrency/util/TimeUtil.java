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
		hours = MathUtil.clamp(hours, 0, Long.MAX_VALUE);
		minutes = MathUtil.clamp(minutes, 0, 59);
		seconds = MathUtil.clamp(seconds, 0, 59);
		
		minutes += 60 * hours; //60 minutes in an hour
		seconds += 60 * minutes; //60 seconds in a minute
		return seconds * 1000;
	}
	
	public static TimeData separateDuration(long duration)
	{
		long seconds = duration / 1000; //Calculate total number of seconds
		long minutes = seconds / 60; //Calculate total number of minutes from the seconds
		seconds = seconds % 60; //Remove excess seconds that got turned into minutes
		long hours = minutes / 60; //
		minutes = minutes % 60;
		return new TimeData(hours, minutes, seconds);
	}
	
	public static class TimeData
	{
		public final long hours;
		public final long minutes;
		public final long seconds;
		public final long miliseconds;
		public TimeData(long hours, long minutes, long seconds)
		{
			hours = MathUtil.clamp(hours, 0, Long.MAX_VALUE);
			minutes = MathUtil.clamp(minutes, 0, 59);
			seconds = MathUtil.clamp(seconds, 0, 59);
			this.hours = hours;
			this.minutes = minutes;
			this.seconds = seconds;
			this.miliseconds = getDuration(hours, minutes, seconds);
		}
		
		public TimeData(long milliseconds)
		{
			this.miliseconds = MathUtil.clamp(milliseconds, 0, Long.MAX_VALUE);
			TimeData time = separateDuration(this.miliseconds);
			this.hours = time.hours;
			this.minutes = time.minutes;
			this.seconds = time.seconds;
		}
	}
	
}

