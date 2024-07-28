package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.TimedSaleTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TimedSale extends PriceTweakingTradeRule {

	public static final TradeRuleType<TimedSale> TYPE = new TradeRuleType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "timed_sale"),TimedSale::new);
	
	long startTime = 0;
	public void setStartTime(long time) { this.startTime = time; }
	public boolean timerActive() { return this.startTime != 0; }
	long duration = 0;
	public long getDuration() { return this.duration; }
	public void setDuration(long duration) { this.duration = MathUtil.clamp(duration, 1000, Long.MAX_VALUE); }
	int discount = 10;
	public int getDiscount() { return this.discount; }
	public void setDiscount(int discount) { this.discount = MathUtil.clamp(discount, 1, 100); }
	
	private TimedSale() { super(TYPE); }
	
	@Override
	public void beforeTrade(@Nonnull PreTradeEvent event)
	{
		if(this.timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE ->
						event.addHelpful(LCText.TRADE_RULE_TIMED_SALE_INFO_SALE.get(this.discount, this.getTimeRemaining().getString()));
				case PURCHASE ->
						event.addHelpful(LCText.TRADE_RULE_TIMED_SALE_INFO_PURCHASE.get(this.discount, this.getTimeRemaining().getString()));
				default -> { } //Nothing if direction is NONE
			}
		}
	}
	
	@Override
	public void tradeCost(@Nonnull TradeCostEvent event)
	{
		if(timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE -> event.giveDiscount(this.discount);
				case PURCHASE -> event.hikePrice(this.discount);
				default -> {} //Nothing if direction is NONE
			}
		}
	}
	
	@Override
	public void afterTrade(@Nonnull PostTradeEvent event)
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
	protected void saveAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
		//Write start time
		compound.putLong("startTime", this.startTime);
		//Save sale duration
		compound.putLong("duration", this.duration);
		//Save discount
		compound.putInt("discount", this.discount);
	}
	
	@Override
	public JsonObject saveToJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
		
		json.addProperty("duration", this.duration);
		json.addProperty("discount", this.discount);
		
		return json;
	}

	@Override
	protected void loadAdditional(@Nonnull CompoundTag compound, @Nonnull HolderLookup.Provider lookup) {
		
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
	public void loadFromJson(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
		if(json.has("duration"))
			this.duration = json.get("duration").getAsLong();
		if(json.has("discount"))
			this.discount = MathUtil.clamp(this.discount, 0, 100);
	}
	
	@Override
	public void handleUpdateMessage(@Nonnull LazyPacketData updateInfo) {
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
	public CompoundTag savePersistentData(@Nonnull HolderLookup.Provider lookup) {
		CompoundTag compound = new CompoundTag();
		compound.putLong("startTime", this.startTime);
		return compound;
	}
	@Override
	public void loadPersistentData(@Nonnull CompoundTag data, @Nonnull HolderLookup.Provider lookup) {
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

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new TimedSaleTab(parent); }
	
	
}
