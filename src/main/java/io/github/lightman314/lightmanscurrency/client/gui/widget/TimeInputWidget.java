package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

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

	private TimeInputWidget(Builder builder)
	{
		super(builder);
		this.timeConsumer = builder.handler;
		this.relevantUnits = this.getRelevantUnits(builder.largestUnit,builder.smallestUnit);
		this.spacing = builder.spacing;
		this.minDuration = builder.minDuration;
		this.maxDuration = builder.maxDuration;
		this.setTimeInternal(builder.startTime);
		this.validateTime();
	}

	@Override
	public void addChildren(@Nonnull ScreenArea area) {
		for(int i = 0; i < this.relevantUnits.size(); ++i)
		{
			final TimeUnit unit = this.relevantUnits.get(i);
			int xOff = (20 + this.spacing) * i;
			this.buttons.add(this.addChild(PlainButton.builder()
					.position(area.pos.offset(xOff,0))
					.pressAction(() -> this.addTime(unit))
					.sprite(MoneyValueWidget.SPRITE_UP_ARROW)
					.build()));
			this.buttons.add(this.addChild(PlainButton.builder()
					.position(area.pos.offset(xOff,23))
					.pressAction(() -> this.removeTime(unit))
					.sprite(MoneyValueWidget.SPRITE_DOWN_ARROW)
					.build()));
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

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(0,0); }
		@Override
		protected Builder getSelf() { return this; }

		private int spacing = 10;
		private TimeUnit smallestUnit = TimeUnit.SECOND;
		private TimeUnit largestUnit = TimeUnit.DAY;
		private Consumer<TimeData> handler = t -> {};
		private long minDuration = 0;
		private long maxDuration = Long.MAX_VALUE;
		private long startTime = 0;

		public Builder spacing(int spacing) { this.spacing = spacing; return this; }
		public Builder smallestUnit(TimeUnit unit) { this.smallestUnit = unit; return this; }
		public Builder largestUnit(TimeUnit unit) { this.largestUnit = unit; return this; }
		public Builder unitRange(TimeUnit smallestUnit, TimeUnit largestUnit) { this.smallestUnit = smallestUnit; this.largestUnit = largestUnit; return this; }
		public Builder minDuration(long minDuration) { this.minDuration = minDuration; return this; }
		public Builder maxDuration(long maxDuration) { this.maxDuration = maxDuration; return this; }
		public Builder range(long minDuration, long maxDuration) { this.minDuration = minDuration; this.maxDuration = maxDuration; return this; }
		public Builder startTime(long startTime) { this.startTime = startTime; return this; }
		public Builder startTime(TimeData startTime) { this.startTime = startTime.miliseconds; return this; }
		public Builder handler(Runnable handler) { this.handler = t -> handler.run(); return this; }
		public Builder handler(Consumer<TimeData> handler) { this.handler = handler; return this; }

		public TimeInputWidget build() { return new TimeInputWidget(this); }

	}

}