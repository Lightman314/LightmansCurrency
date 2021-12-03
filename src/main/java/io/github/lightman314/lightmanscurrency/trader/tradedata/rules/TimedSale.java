package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeWidget;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class TimedSale extends TradeRule {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "timed_sale");
	
	long startTime = 0;
	boolean isActive() { return this.startTime != 0; }
	long duration = 0;
	int discount = 10;
	public int getDiscountPercent() { return this.discount; }
	public void setDiscountPercent(int percent) { this.discount = MathUtil.clamp(percent, 0, 100); }
	private double getDiscountMult() { return 1d - ((double)discount/100d); }
	private double getIncreaseMult() { return 1d + ((double)discount/100d); }
	
	public TimedSale() { super(TYPE); }
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		if(isActive() && TimeUtil.compareTime(this.duration, this.startTime))
		{
			switch(event.getTrade().getTradeDirection())
			{
			case SALE:
				event.applyCostMultiplier(this.getDiscountMult());
				break;
			case PURCHASE:
				event.applyCostMultiplier(this.getIncreaseMult());
				break;
				default: //Nothing if direction is NONE
			}
		}
	}
	
	@Override
	public void afterTrade(PostTradeEvent event)
	{
		if(confirmStillActive())
			event.markDirty();
	}
	
	private boolean confirmStillActive()
	{
		if(!isActive())
			return false;
		else if(!TimeUtil.compareTime(this.duration, this.startTime))
		{
			this.startTime = 0;
			return true;
		}
		return false;
	}
	
	@Override
	protected CompoundNBT write(CompoundNBT compound) {
		
		//Write start time
		compound.putLong("startTime", this.startTime);
		//Save sale duration
		compound.putLong("duration", this.duration);
		//Save discount
		compound.putInt("discount", this.discount);
		
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		
		//Load start time
		if(compound.contains("startTime", Constants.NBT.TAG_LONG))
			this.startTime = compound.getLong("startTime");
		//Load duration
		if(compound.contains("duration", Constants.NBT.TAG_LONG))
			this.duration = compound.getLong("duration");
		//Load discount
		if(compound.contains("discount", Constants.NBT.TAG_INT))
			this.discount = compound.getInt("discount");
		
	}
	
	public TimeData getTimeRemaining()
	{
		if(!isActive())
			return new TimeData(0);
		else
		{
			return new TimeData(this.startTime + this.duration - TimeUtil.getCurrentTime());
		}
	}
	
	@Override
	public ITextComponent getButtonText() { return new TranslationTextComponent("gui.button.lightmanscurrency.timed_sale"); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler implements TimeWidget.ITimeInput
	{
		
		protected final TimedSale getRule()
		{
			if(getRuleRaw() instanceof TimedSale)
				return (TimedSale)getRuleRaw();
			return null;
		}
		
		GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			super(screen, rule);
		}
		
		TextFieldWidget discountInput;
		
		Button buttonSetDiscount;
		Button buttonStartSale;
		
		TimeWidget durationInput;
		
		@Override
		public void initTab() {
			
			
			this.discountInput = this.addListener(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, new StringTextComponent("")));
			this.discountInput.setMaxStringLength(2);
			this.discountInput.setText(Integer.toString(this.getRule().discount));
			this.buttonSetDiscount = this.addButton(new Button(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));
			
			this.buttonStartSale = this.addButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 45, 156, 20, this.getButtonText(), this::PressStartButton));
			
			this.durationInput = this.addListener(new TimeWidget(screen.guiLeft(), screen.guiTop() + 75, screen.getFont(), this.getRule().duration, this, this, new TranslationTextComponent("gui.widget.lightmanscurrency.timed_sale.noduration")));
			
		}
		
		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getRule() == null)
				return;
			
			this.discountInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.screen.getFont().drawString(matrixStack, new TranslationTextComponent("gui.lightmanscurrency.discount.tooltip").getString(), this.discountInput.x + this.discountInput.getWidth() + 4, this.discountInput.y + 3, 0xFFFFFF);
			
			this.durationInput.render(matrixStack, mouseX, mouseY, partialTicks);
			
			ITextComponent infoText = new TranslationTextComponent("gui.button.lightmanscurrency.timed_sale.info.inactive", new TimeData(this.getRule().duration).toString());
			if(this.getRule().isActive())
				infoText = new TranslationTextComponent("gui.button.lightmanscurrency.timed_sale.info.active", this.getRule().getTimeRemaining().toString());
			
			this.screen.getFont().drawString(matrixStack, infoText.getString(), screen.guiLeft() + 10, screen.guiTop() + 35, 0xFFFFFF);
			
			if(this.buttonStartSale.isMouseOver(mouseX, mouseY))
			{
				screen.renderTooltip(matrixStack, this.getButtonTooltip(), mouseX, mouseY);
			}
			
		}
		
		@Override
		public void onScreenTick()
		{
			if(this.getRule().confirmStillActive())
				screen.markRulesDirty();
			this.buttonStartSale.setMessage(getButtonText());
			this.buttonStartSale.active = this.getRule().isActive() || this.getRule().duration > 0;
			TextInputUtil.whitelistInteger(this.discountInput, 0, 99);
			this.durationInput.tick();
			this.discountInput.tick();
			
		}
		
		private ITextComponent getButtonText()
		{
			return new TranslationTextComponent("gui.button.lightmanscurrency.timed_sale." + (this.getRule().isActive() ? "stop" : "start"));
		}
		
		private ITextComponent getButtonTooltip()
		{
			return new TranslationTextComponent("gui.button.lightmanscurrency.timed_sale." + (this.getRule().isActive() ? "stop" : "start") + ".tooltip");
		}
		
		@Override
		public void onTabClose() {
			
			screen.removeListener(this.discountInput);
			screen.removeButton(this.buttonSetDiscount);
			screen.removeButton(this.buttonStartSale);
			this.durationInput.getButtons().forEach(button -> screen.removeButton(button));
			this.durationInput.getListeners().forEach(listener -> screen.removeListener(listener));
			screen.removeListener(this.durationInput);
		}
		
		void PressSetDiscountButton(Button button)
		{
			int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
			this.getRule().discount = discount;
			this.screen.markRulesDirty();
		}
		
		void PressStartButton(Button button)
		{
			this.getRule().startTime = this.getRule().isActive() ? 0 : TimeUtil.getCurrentTime();
			this.screen.markRulesDirty();
		}
		
		@Override
		public void onTimeSet(long newTime)
		{
			this.getRule().duration = newTime;
			this.screen.markRulesDirty();
		}
		
	}
	
	
}
