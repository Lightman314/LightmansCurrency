package io.github.lightman314.lightmanscurrency.util;

import java.util.List;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TimeUtil {

	public static final long DURATION_SECOND = 1000;
	public static final long DURATION_MINUTE = 60 * DURATION_SECOND;
	public static final long DURATION_HOUR = 60 * DURATION_MINUTE;
	public static final long DURATION_DAY = 24 * DURATION_HOUR;
	
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
	
	public static long getDuration(long days, long hours, long minutes, long seconds)
	{
		days = Math.max(days, 0);
		hours = Math.max(hours, 0);
		minutes = Math.max(minutes, 0);
		seconds = Math.max(seconds, 0);
		
		hours += 24 * days; //24 hours in a day
		minutes += 60 * hours; //60 minutes in an hour
		seconds += 60 * minutes; //60 seconds in a minute
		return seconds * 1000;
	}
	
	@Deprecated
	public static TimeData separateDuration(long duration) { return new TimeData(duration); }
	
	public static class TimeData
	{
		public final long days;
		public final long hours;
		public final long minutes;
		public final long seconds;
		public final long miliseconds;
		public TimeData(long days, long hours, long minutes, long seconds) { this(getDuration(days,hours,minutes,seconds)); }
		
		public TimeData(long milliseconds)
		{
			this.miliseconds = Math.max(milliseconds, 0);
			
			long seconds = this.miliseconds / 1000; //Calculate total number of seconds
			long minutes = seconds / 60; //Calculate total number of minutes from the seconds
			seconds = seconds % 60; //Remove excess seconds that got turned into minutes
			long hours = minutes / 60; //Calculate total number of hours from the minutes
			minutes = minutes % 60; //Remove excess minutes that got turned into hours
			long days = hours / 24; //Calculate total number of days from the hours
			hours = hours % 24; //Remove excess hours that got turned into days
			
			this.days = days;
			this.hours = hours;
			this.minutes = minutes;
			this.seconds = seconds;
		}
		
		private long getUnitValue(TimeUnit unit) {
			switch(unit) {
			case DAY:
				return this.days;
			case HOUR:
				return this.hours;
			case MINUTE:
				return this.minutes;
			case SECOND:
				return this.seconds;
				default:
					return 0;
			}
		}
		
		public String getUnitString(TimeUnit unit, boolean shortText) { return this.getUnitString(unit, shortText, true); }
		
		private String getUnitString(TimeUnit unit, boolean shortText, boolean force) {
			StringBuffer text = new StringBuffer();
			long count = this.getUnitValue(unit);
			if(count > 0 || force)
				text.append(count).append(shortText ? unit.getShortText().getString() : (count != 1 ? unit.getPluralText().getString() : unit.getText().getString()));
			return text.toString();
		}
		
		public String getString() { return this.getString(false, Integer.MAX_VALUE); }
		public String getString(int maxCount) { return this.getString(false, maxCount); }
		public String getShortString() { return this.getString(true, Integer.MAX_VALUE); }
		public String getShortString(int maxCount) { return this.getString(true, maxCount); }
		private String getString(boolean shortText, int maxCount)
		{
			StringBuffer text = new StringBuffer();
			int count = 0;
			for(int i = 0; i < TimeUnit.UNITS_LARGE_TO_SMALL.size() && count < maxCount; ++i)
			{
				TimeUnit unit = TimeUnit.UNITS_LARGE_TO_SMALL.get(i);
				String unitText = this.getUnitString(unit, shortText, false);
				if(unitText.length() > 0)
				{
					if(text.length() > 0)
						text.append(" ");
					text.append(unitText);
					count++;
				}
			}
			return text.toString();
		}
	}
	
	public enum TimeUnit {
		SECOND, MINUTE, HOUR, DAY;
		
		public static final List<TimeUnit> UNITS_SMALL_TO_LARGE = ImmutableList.of(TimeUnit.SECOND, TimeUnit.MINUTE, TimeUnit.HOUR, TimeUnit.DAY);
		public static final List<TimeUnit> UNITS_LARGE_TO_SMALL = ImmutableList.of(TimeUnit.DAY, TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND);
		
		public MutableComponent getText() { return Component.translatable("gui.lightmanscurrency.time.unit." + this.name().toLowerCase()); }
		public MutableComponent getPluralText() { return Component.translatable("gui.lightmanscurrency.time.unit." + this.name().toLowerCase() + ".plural"); }
		public MutableComponent getShortText() { return Component.translatable("gui.lightmanscurrency.time.unit." + this.name().toLowerCase() + ".short"); }
		
	}
	
}

