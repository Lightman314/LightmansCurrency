package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;

import javax.annotation.Nonnull;

public class TimeInputWidget extends EasyWidgetWithChildren {

	private final List<TimeUnit> relevantUnits;
	private final int spacing;
	public long maxDuration = Long.MAX_VALUE;
	public long minDuration = 0;
	private final Consumer<TimeData> timeConsumer;
	
	long days = 0;
	long hours = 0;
	long minutes = 0;
	long seconds = 0;
	
	public TimeData getTime() { return new TimeData(this.days, this.hours, this.minutes, this.seconds); }
	
	private final List<EasyButton> buttons = new ArrayList<>();
	
	public TimeInputWidget(ScreenPosition pos, int spacing, TimeUnit largestUnit, TimeUnit smallestUnit, Consumer<TimeData> timeConsumer) { this(pos.x, pos.y, spacing, largestUnit, smallestUnit, timeConsumer); }
	public TimeInputWidget(int x, int y, int spacing, TimeUnit largestUnit, TimeUnit smallestUnit, Consumer<TimeData> timeConsumer) {
		super(x, y, 0, 0);
		this.timeConsumer = timeConsumer;
		this.relevantUnits = this.getRelevantUnits(largestUnit, smallestUnit);
		this.spacing = spacing;
	}

	@Override
	public TimeInputWidget withAddons(WidgetAddon... addons) { this.withAddonsInternal(addons); return this; }

	@Override
	public void addChildren() {
		for(int i = 0; i < this.relevantUnits.size(); ++i)
		{
			final TimeUnit unit = this.relevantUnits.get(i);
			int xPos = this.getX() + ((20 + this.spacing) * i);
			this.buttons.add(this.addChild(new PlainButton(xPos, this.getY(), b -> this.addTime(unit), MoneyValueWidget.SPRITE_UP_ARROW)));
			this.buttons.add(this.addChild(new PlainButton(xPos, this.getY() + 23, b -> this.removeTime(unit), MoneyValueWidget.SPRITE_DOWN_ARROW)));
		}
	}

	public void setTime(long milliseconds) {
		this.setTimeInternal(milliseconds);
		this.validateTime();
		this.timeConsumer.accept(this.getTime());
	}
	
	public void setTime(TimeData time) {
		this.setTimeInternal(time);
		this.validateTime();
		this.timeConsumer.accept(this.getTime());
	}
	
	public void setTime(long days, long hours, long minutes, long seconds) { 
		this.setTimeInternal(days, hours, minutes, seconds);
		this.validateTime();
		this.timeConsumer.accept(this.getTime());
	}
	
	private void setTimeInternal(long milliseconds) { this.setTimeInternal(new TimeData(milliseconds)); }
	
	private void setTimeInternal(TimeData time) { this.setTimeInternal(time.days, time.hours, time.minutes, time.seconds); }
	
	private void setTimeInternal(long days, long hours, long minutes, long seconds) {
		this.days = days;
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		
		if(!this.validUnit(TimeUnit.DAY))
		{
			this.hours += this.days * 24;
			this.days = 0;
		}
		if(!this.validUnit(TimeUnit.HOUR))
		{
			this.minutes += this.hours * 60;
			this.hours = 0;
		}
		if(!this.validUnit(TimeUnit.MINUTE))
		{
			this.seconds += this.minutes * 60;
			this.minutes = 0;
		}
		if(!this.validUnit(TimeUnit.SECOND))
			this.seconds = 0;
	}
	
	private boolean validUnit(TimeUnit unit) { return this.relevantUnits.contains(unit); }
	
	private void addTime(TimeUnit unit) {
		switch (unit) {
			case DAY -> this.days++;
			case HOUR -> {
				this.hours++;
				if (this.hours >= 24 && this.validUnit(TimeUnit.DAY)) {
					this.days += this.hours / 24;
					this.hours = this.hours % 24;
				}
			}
			case MINUTE -> {
				this.minutes++;
				if (this.minutes >= 60 && this.validUnit(TimeUnit.HOUR)) {
					this.hours += this.minutes / 60;
					this.minutes = this.minutes % 60;
				}
			}
			case SECOND -> {
				this.seconds++;
				if (this.seconds >= 60 && this.validUnit(TimeUnit.SECOND)) {
					this.minutes += this.seconds / 60;
					this.seconds = this.seconds % 60;
				}
			}
		}
		this.validateTime();
		this.timeConsumer.accept(this.getTime());
	}
	
	private void removeTime(TimeUnit unit) {
		this.removeTimeInternal(unit);
		this.validateTime();
		this.timeConsumer.accept(this.getTime());
	}
	
	private void removeTimeInternal(TimeUnit unit) {
		switch (unit) {
			case DAY -> this.days = Math.max(0, this.days - 1);
			case HOUR -> {
				this.hours--;
				if (this.hours < 0) {
					if(this.days > 0)
					{
						this.removeTimeInternal(TimeUnit.DAY);
						this.hours += 24;
					}
					else
						this.hours = 0;
				}
			}
			case MINUTE -> {
				this.minutes--;
				if (this.minutes < 0) {
					if(this.hours > 0 || this.days > 0)
					{
						this.removeTimeInternal(TimeUnit.HOUR);
						this.minutes += 60;
					}
					else
						this.minutes = 0;
				}
			}
			case SECOND -> {
				this.seconds--;
				if (this.seconds < 0) {
					if(this.minutes > 0 || this.hours > 0 || this.days > 0)
					{
						this.removeTimeInternal(TimeUnit.MINUTE);
						this.seconds += 60;
					}
					else
						this.seconds = 0;
				}
			}
		}
	}
	
	private void validateTime() {
		long duration = this.getTime().miliseconds;
		if(duration > this.maxDuration)
			this.setTimeInternal(this.maxDuration);
		if(duration < this.minDuration)
			this.setTimeInternal(this.minDuration);
	}
	
	private List<TimeUnit> getRelevantUnits(TimeUnit largestUnit, TimeUnit smallestUnit) {
		List<TimeUnit> results = new ArrayList<>();
		List<TimeUnit> units = TimeUnit.UNITS_LARGE_TO_SMALL;
		int startIndex = units.indexOf(largestUnit);
		if(startIndex < 0)
			throw new RuntimeException("TimeUnit '" + largestUnit + "' could not be found on the TimeUnit list!");
		for(int i = startIndex; i < units.size(); ++i)
		{
			TimeUnit unit = units.get(i);
			results.add(unit);
			if(unit == smallestUnit)
				break;
		}
		return results;
	}

	@Override
	protected void renderTick() {
		for(EasyButton b : this.buttons) { b.active = this.active; b.visible = this.visible; }
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		
		for(int i = 0; i < this.relevantUnits.size(); ++i)
		{
			TextRenderUtil.drawCenteredText(gui, this.getTime().getUnitString(this.relevantUnits.get(i), true), ((20 + this.spacing) * i) + 10, 12, 0xFFFFFF);
		}

	}

}
