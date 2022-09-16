package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TimeInputWidget extends AbstractWidget {

	
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
	
	private List<Button> buttons = new ArrayList<>();
	
	public TimeInputWidget(int x, int y, int spacing, TimeUnit largestUnit, TimeUnit smallestUnit, Consumer<AbstractWidget> widgetAdder, Consumer<TimeData> timeConsumer) {
		super(x, y, 0, 0, Component.empty());
		this.timeConsumer = timeConsumer;
		this.relevantUnits = this.getRelevantUnits(largestUnit, smallestUnit);
		this.spacing = spacing;
		
		for(int i = 0; i < this.relevantUnits.size(); ++i)
		{
			final TimeUnit unit = this.relevantUnits.get(i);
			
			int xPos = this.x + ((20 + this.spacing) * i);
			PlainButton addButton = new PlainButton(xPos, this.y, 20, 10, b -> this.addTime(unit), CoinValueInput.GUI_TEXTURE, 0, CoinValueInput.HEIGHT);
			PlainButton removeButton = new PlainButton(xPos, this.y + 23, 20, 10, b -> this.removeTime(unit), CoinValueInput.GUI_TEXTURE, 20, CoinValueInput.HEIGHT);
			widgetAdder.accept(addButton);
			widgetAdder.accept(removeButton);
			this.buttons.add(addButton);
			this.buttons.add(removeButton);
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
		switch(unit)
		{
		case DAY:
			this.days++;
			break;
		case HOUR:
			this.hours++;
			if(this.hours >= 24 && this.validUnit(TimeUnit.DAY))
			{
				this.days += this.hours / 24;
				this.hours = this.hours % 24;
			}
			break;
		case MINUTE:
			this.minutes++;
			if(this.minutes >= 60 && this.validUnit(TimeUnit.HOUR))
			{
				this.hours += this.minutes / 60;
				this.minutes = this.minutes % 60;
			}
			break;
		case SECOND:
			this.seconds++;
			if(this.seconds >= 60 && this.validUnit(TimeUnit.SECOND))
			{
				this.minutes += this.seconds / 60;
				this.seconds = this.seconds % 60;
			}
			break;
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
		switch(unit) {
		case DAY:
			this.days = Math.max(0, this.days - 1);
			break;
		case HOUR:
			this.hours--;
			if(this.hours < 0 && this.days > 0)
			{
				this.removeTimeInternal(TimeUnit.DAY);
				this.hours += 24;
			}
			else
				this.hours = 0;
			break;
		case MINUTE:
			this.minutes--;
			if(this.minutes < 0 && (this.hours > 0 || this.days > 0))
			{
				this.removeTimeInternal(TimeUnit.HOUR);
				this.minutes += 60;
			}
			else
				this.minutes = 0;
			break;
		case SECOND:
			this.seconds--;
			if(this.seconds < 0 && (this.minutes > 0 || this.hours > 0 || this.days > 0))
			{
				this.removeTimeInternal(TimeUnit.MINUTE);
				this.seconds += 60;
			}
			break;
		}
	}
	
	private void validateTime() {
		long duration = this.getTime().miliseconds;
		if(duration > this.maxDuration)
			this.setTimeInternal(this.maxDuration);
		if(duration < this.minDuration)
			this.setTimeInternal(this.minDuration);
	}
	
	private final List<TimeUnit> getRelevantUnits(TimeUnit largestUnit, TimeUnit smallestUnit) {
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
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		for(Button b : this.buttons)
		{
			b.active = this.active;
			b.visible = this.visible;
		}
		
		for(int i = 0; i < this.relevantUnits.size(); ++i)
		{
			TextRenderUtil.drawCenteredText(pose, this.getTime().getUnitString(this.relevantUnits.get(i), true), this.x + ((20 + this.spacing) * i) + 10, this.y + 12, 0xFFFFFF);
		}

	}
	
	public void removeChildren(Consumer<AbstractWidget> remover) { for(Button b : this.buttons) remover.accept(b); }
	
	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {}
	
}
