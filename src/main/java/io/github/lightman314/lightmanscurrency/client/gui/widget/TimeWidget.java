package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.tradedata.rules.TradeRule.GUIHandler;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TimeWidget extends Widget{
	
	public static final int WIDTH = 176;
	public static final int HEIGHT = 40;
	
	private TextFieldWidget hourInput;
	private TextFieldWidget minuteInput;
	private TextFieldWidget secondInput;
	Button setTimeButton;
	
	private GUIHandler handler;
	FontRenderer font;
	ITimeInput timeInput;
	ITextComponent noTimeText;
	
	long startingValue = 0;
	
	public TimeWidget(int x, int y, FontRenderer font, long startingValue, GUIHandler handler, @Nullable ITimeInput timeInput, ITextComponent noTimeText) {
		
		super(x, y, WIDTH, HEIGHT, new StringTextComponent(""));
		
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
		this.hourInput = handler.addListener(new TextFieldWidget(this.font, this.x + 10, this.y + 19, 30, 20, new StringTextComponent("")));
		this.hourInput.setMaxStringLength(3);
		this.hourInput.setText(Long.toString(timeData.hours));
		
		this.minuteInput = handler.addListener(new TextFieldWidget(this.font, this.x + 80, this.y + 19, 20, 20, new StringTextComponent("")));
		this.minuteInput.setMaxStringLength(2);
		this.minuteInput.setText(Long.toString(timeData.minutes));
		
		this.secondInput = handler.addListener(new TextFieldWidget(this.font, this.x + 130, this.y + 19, 20, 20, new StringTextComponent("")));
		this.secondInput.setMaxStringLength(2);
		this.secondInput.setText(Long.toString(timeData.seconds));
		
		this.setTimeButton = handler.addButton(new Button(this.x + 80, this.y - 2, 80, 20, new TranslationTextComponent("gui.button.lightmanscurrency.time_widget.settime"), this::PressSetTimeButton));
		this.setTimeButton.visible = this.timeInput != null;
		
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		
		TimeData time = this.getTime();
		//Render the time info
		if(time.miliseconds > 0)
			this.font.drawString(matrixStack, new TranslationTextComponent("gui.widget.lightmanscurrency.time_widget.info", time.hours, time.minutes, time.seconds).getString(), this.x + 10, this.y + 2, 0xFFFFFF);
		else
			this.font.drawString(matrixStack, this.noTimeText.getString(), this.x + 10, this.y + 2, 0xFFFFFF);
		
		//Render the text inputs
		this.getListeners().forEach(listener -> listener.render(matrixStack, mouseX, mouseY, partialTicks));
		
		//Render the h/m/s next to the inputs
		this.font.drawString(matrixStack, "h", this.x + 62, this.y + 25, 0xFFFFFF);
		this.font.drawString(matrixStack, "m", this.x + 112, this.y + 25, 0xFFFFFF);
		this.font.drawString(matrixStack, "s", this.x + 162, this.y + 25, 0xFFFFFF);
		
	}
	
	public void tick()
	{
		this.getListeners().forEach(listener -> listener.tick());
	}
	
	public List<Button> getButtons()
	{
		return ImmutableList.of(setTimeButton);
	}
	
	public List<TextFieldWidget> getListeners()
	{
		return ImmutableList.of(hourInput, minuteInput, secondInput);
	}
	
	public TimeData getTime()
	{
		return new TimeData(inputValue(this.hourInput, false), inputValue(this.minuteInput, true), inputValue(this.secondInput, true));
	}
	
	private static long inputValue(TextFieldWidget textField, boolean clamp)
	{
		if(isNumeric(textField.getText()))
		{
			return MathUtil.clamp(Long.parseLong(textField.getText()), 0, 59);
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

}
