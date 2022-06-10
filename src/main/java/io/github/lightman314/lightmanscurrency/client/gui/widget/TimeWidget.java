package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.TradeRule.GUIHandler;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TimeWidget extends AbstractWidget{
	
	public static final int WIDTH = 176;
	public static final int HEIGHT = 40;
	
	private EditBox hourInput;
	private EditBox minuteInput;
	private EditBox secondInput;
	Button setTimeButton;
	
	private GUIHandler handler;
	Font font;
	ITimeInput timeInput;
	Component noTimeText;
	
	long startingValue = 0;
	
	public TimeWidget(int x, int y, Font font, long startingValue, GUIHandler handler, @Nullable ITimeInput timeInput, Component noTimeText) {
		
		super(x, y, WIDTH, HEIGHT, Component.empty());
		
		this.startingValue = startingValue;
		this.handler = handler;
		this.font = font;
		this.timeInput = timeInput;
		this.noTimeText = noTimeText;
		
		this.init();
		
	}
	
	protected void init()
	{
		TimeData timeData = TimeUtil.separateDuration(this.startingValue);
		this.hourInput = handler.addCustomRenderable(new EditBox(this.font, this.x + 10, this.y + 19, 30, 20, Component.empty()));
		this.hourInput.setMaxLength(3);
		this.hourInput.setValue(Long.toString(timeData.hours));
		
		this.minuteInput = handler.addCustomRenderable(new EditBox(this.font, this.x + 80, this.y + 19, 20, 20, Component.empty()));
		this.minuteInput.setMaxLength(2);
		this.minuteInput.setValue(Long.toString(timeData.minutes));
		
		this.secondInput = handler.addCustomRenderable(new EditBox(this.font, this.x + 130, this.y + 19, 20, 20, Component.empty()));
		this.secondInput.setMaxLength(2);
		this.secondInput.setValue(Long.toString(timeData.seconds));
		
		this.setTimeButton = handler.addCustomRenderable(new Button(this.x + 80, this.y - 2, 80, 20, Component.translatable("gui.button.lightmanscurrency.time_widget.settime"), this::PressSetTimeButton));
		this.setTimeButton.visible = this.timeInput != null;
		
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		
		TimeData time = this.getTime();
		//Render the time info
		if(time.miliseconds > 0)
			this.font.draw(poseStack, Component.translatable("gui.widget.lightmanscurrency.time_widget.info", time.hours, time.minutes, time.seconds).getString(), this.x + 10, this.y + 2, 0xFFFFFF);
		else
			this.font.draw(poseStack, this.noTimeText.getString(), this.x + 10, this.y + 2, 0xFFFFFF);
		
		//Render the h/m/s next to the inputs
		this.font.draw(poseStack, "h", this.x + 62, this.y + 25, 0xFFFFFF);
		this.font.draw(poseStack, "m", this.x + 112, this.y + 25, 0xFFFFFF);
		this.font.draw(poseStack, "s", this.x + 162, this.y + 25, 0xFFFFFF);
		
	}
	
	public List<AbstractWidget> getWidgets()
	{
		return ImmutableList.of(setTimeButton, hourInput, minuteInput, secondInput);
	}
	
	public TimeData getTime()
	{
		return new TimeData(inputValue(this.hourInput, false), inputValue(this.minuteInput, true), inputValue(this.secondInput, true));
	}
	
	private static long inputValue(EditBox textField, boolean clamp)
	{
		if(isNumeric(textField.getValue()))
		{
			return MathUtil.clamp(Long.parseLong(textField.getValue()), 0, 59);
		}
		return 0;
	}
	
	private static boolean isNumeric(String string)
	{
		if(string == null)
			return false;
		try
		{
			@SuppressWarnings("unused")
			long i = Long.parseLong(string);
		} 
		catch(NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}
	
	public static interface ITimeInput
	{
		public void onTimeSet(long newTime);
	}
	
	private void PressSetTimeButton(Button button)
	{
		if(this.timeInput != null)
			this.timeInput.onTimeSet(this.getTime().miliseconds);
	}

	@Override
	public void updateNarration(NarrationElementOutput narration) { }

}
