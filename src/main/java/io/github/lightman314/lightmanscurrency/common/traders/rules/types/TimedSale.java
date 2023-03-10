package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class TimedSale extends TradeRule {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "timed_sale");
	
	long startTime = 0;
	boolean timerActive() { return this.startTime != 0; }
	long duration = 0;
	int discount = 10;
	public int getDiscountPercent() { return this.discount; }
	public void setDiscountPercent(int percent) { this.discount = MathUtil.clamp(percent, 0, 100); }
	private double getDiscountMult() { return 1d - ((double)discount/100d); }
	private double getIncreaseMult() { return 1d + ((double)discount/100d); }
	
	public TimedSale() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
		{
			switch(event.getTrade().getTradeDirection())
			{
			case SALE:
				event.addHelpful(EasyText.translatable("traderule.lightmanscurrency.timed_sale.info.sale", this.discount, this.getTimeRemaining().getString()));
				break;
			case PURCHASE:
				event.addHelpful(EasyText.translatable("traderule.lightmanscurrency.timed_sale.info.purchase", this.discount, this.getTimeRemaining().getString()));
				break;
				default: //Nothing if direction is NONE
			}
		}
	}
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		if(timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
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
		if(!timerActive())
			return false;
		else if(!TimeUtil.compareTime(this.duration, this.startTime))
		{
			this.startTime = 0;
			return true;
		}
		return false;
	}
	
	@Override
	protected void saveAdditional(CompoundNBT compound) {
		
		//Write start time
		compound.putLong("startTime", this.startTime);
		//Save sale duration
		compound.putLong("duration", this.duration);
		//Save discount
		compound.putInt("discount", this.discount);
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		
		json.addProperty("duration", this.duration);
		json.addProperty("discount", this.discount);
		
		return json;
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		
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
	
	@Override
	public void loadFromJson(JsonObject json) {
		if(json.has("duration"))
			this.duration = json.get("duration").getAsLong();
		if(json.has("discount"))
			this.discount = MathUtil.clamp(this.discount, 0, 100);
	}
	
	@Override
	public void handleUpdateMessage(CompoundNBT updateInfo) {
		if(updateInfo.contains("Discount"))
		{
			this.discount = updateInfo.getInt("Discount");
		}
		else if(updateInfo.contains("Duration"))
		{
			this.duration = updateInfo.getLong("Duration");
		}
		else if(updateInfo.contains("StartSale"))
		{
			if(this.timerActive() == updateInfo.getBoolean("StartSale"))
				return;
			if(this.timerActive())
				this.startTime = 0;
			else
				this.startTime = TimeUtil.getCurrentTime();
		}
		
	}
	
	@Override
	public CompoundNBT savePersistentData() {
		CompoundNBT compound = new CompoundNBT();
		compound.putLong("startTime", this.startTime);
		return compound;
	}
	@Override
	public void loadPersistentData(CompoundNBT data) {
		if(data.contains("startTime", Constants.NBT.TAG_LONG))
			this.startTime = data.getLong("startTime");
	}
	
	public TimeData getTimeRemaining()
	{
		if(!timerActive())
			return new TimeData(0);
		else
		{
			return new TimeData(this.startTime + this.duration - TimeUtil.getCurrentTime());
		}
	}
	
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_TIMED_SALE; }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler
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
		
		TimeInputWidget durationInput;
		
		@Override
		public void initTab() {
			
			
			this.discountInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, EasyText.empty()));
			this.discountInput.setMaxLength(2);
			this.discountInput.setValue(Integer.toString(this.getRule().discount));
			this.buttonSetDiscount = this.addCustomRenderable(new Button(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, EasyText.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));
			
			this.buttonStartSale = this.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 45, 156, 20, this.getButtonText(), this::PressStartButton));
			
			this.durationInput = this.addCustomRenderable(new TimeInputWidget(screen.guiLeft() + 48, screen.guiTop() + 75, 10, TimeUnit.DAY, TimeUnit.MINUTE, this::addCustomRenderable, this::onTimeSet));
			this.durationInput.setTime(this.getRule().duration);
			
		}
		
		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getRule() == null)
				return;
			
			this.screen.getFont().draw(matrixStack, EasyText.translatable("gui.lightmanscurrency.discount.tooltip"), this.discountInput.x + this.discountInput.getWidth() + 4, this.discountInput.y + 3, 0xFFFFFF);
			
			ITextComponent infoText = EasyText.translatable("gui.button.lightmanscurrency.timed_sale.info.inactive", new TimeData(this.getRule().duration).getShortString());
			if(this.getRule().timerActive())
				infoText = EasyText.translatable("gui.button.lightmanscurrency.timed_sale.info.active", this.getRule().getTimeRemaining().getShortString(3));
			
			this.screen.getFont().draw(matrixStack, infoText.getString(), screen.guiLeft() + 10, screen.guiTop() + 35, 0xFFFFFF);
			
			if(this.buttonStartSale.isMouseOver(mouseX, mouseY))
			{
				screen.renderTooltip(matrixStack, this.getButtonTooltip(), mouseX, mouseY);
			}
			
		}
		
		@Override
		public void onScreenTick()
		{
			this.buttonStartSale.setMessage(getButtonText());
			this.buttonStartSale.active = this.getRule().timerActive() || (this.getRule().duration > 0 && this.getRule().isActive());
			TextInputUtil.whitelistInteger(this.discountInput, 0, 99);
			
		}
		
		private ITextComponent getButtonText()
		{
			return EasyText.translatable("gui.button.lightmanscurrency.timed_sale." + (this.getRule().timerActive() ? "stop" : "start"));
		}
		
		private ITextComponent getButtonTooltip()
		{
			return EasyText.translatable("gui.button.lightmanscurrency.timed_sale." + (this.getRule().timerActive() ? "stop" : "start") + ".tooltip");
		}
		
		@Override
		public void onTabClose() {
			
			this.removeCustomWidget(this.discountInput);
			this.removeCustomWidget(this.buttonSetDiscount);
			this.removeCustomWidget(this.buttonStartSale);
			this.durationInput.removeChildren(this::removeCustomWidget);
			this.removeCustomWidget(this.durationInput);
		}
		
		void PressSetDiscountButton(Button button)
		{
			int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
			this.getRule().discount = discount;
			CompoundNBT updateInfo = new CompoundNBT();
			updateInfo.putInt("Discount", discount);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
		void PressStartButton(Button button)
		{
			boolean setActive = !this.getRule().timerActive();
			this.getRule().startTime = this.getRule().timerActive() ? 0 : TimeUtil.getCurrentTime();
			CompoundNBT updateInfo = new CompoundNBT();
			updateInfo.putBoolean("StartSale", setActive);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
		public void onTimeSet(TimeData newTime)
		{
			this.getRule().duration = newTime.miliseconds;
			CompoundNBT updateInfo = new CompoundNBT();
			updateInfo.putLong("Duration", newTime.miliseconds);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
	}
	
	
}