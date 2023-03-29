package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.TimedSaleTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TimedSale extends PriceTweakingTradeRule {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "timed_sale");
	
	long startTime = 0;
	public void setStartTime(long time) { this.startTime = time; }
	public boolean timerActive() { return this.startTime != 0; }
	long duration = 0;
	public long getDuration() { return this.duration; }
	public void setDuration(long duration) { this.duration = MathUtil.clamp(duration, 1000, Long.MAX_VALUE); }
	int discount = 10;
	public int getDiscount() { return this.discount; }
	public void setDiscount(int discount) { this.discount = MathUtil.clamp(discount, 1, 100); }
	private double getDiscountMult() { return 1d - ((double)discount/100d); }
	private double getIncreaseMult() { return 1d + ((double)discount/100d); }
	
	public TimedSale() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE ->
						event.addHelpful(Component.translatable("traderule.lightmanscurrency.timed_sale.info.sale", this.discount, this.getTimeRemaining().getString()));
				case PURCHASE ->
						event.addHelpful(Component.translatable("traderule.lightmanscurrency.timed_sale.info.purchase", this.discount, this.getTimeRemaining().getString()));
				default -> { } //Nothing if direction is NONE
			}
		}
	}
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		if(timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE -> event.applyCostMultiplier(this.getDiscountMult());
				case PURCHASE -> event.applyCostMultiplier(this.getIncreaseMult());
				default -> {} //Nothing if direction is NONE
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
	protected void saveAdditional(CompoundTag compound) {
		
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
	protected void loadAdditional(CompoundTag compound) {
		
		//Load start time
		if(compound.contains("startTime", Tag.TAG_LONG))
			this.startTime = compound.getLong("startTime");
		//Load duration
		if(compound.contains("duration", Tag.TAG_LONG))
			this.duration = compound.getLong("duration");
		//Load discount
		if(compound.contains("discount", Tag.TAG_INT))
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
	public void handleUpdateMessage(CompoundTag updateInfo) {
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
	public CompoundTag savePersistentData() {
		CompoundTag compound = new CompoundTag();
		compound.putLong("startTime", this.startTime);
		return compound;
	}
	@Override
	public void loadPersistentData(CompoundTag data) {
		if(data.contains("startTime", Tag.TAG_LONG))
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

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new TimedSaleTab(parent); }
	
	
}
