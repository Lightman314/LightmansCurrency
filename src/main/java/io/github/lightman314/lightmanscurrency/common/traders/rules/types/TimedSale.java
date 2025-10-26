package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.TimedSaleTab;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimedSale extends PriceTweakingTradeRule implements ICopySupportingRule {

	public static final TradeRuleType<TimedSale> TYPE = new TradeRuleType<>(VersionUtil.lcResource("timed_sale"),TimedSale::new);
	
	long startTime = 0;
	public void setStartTime(long time) { this.startTime = time; }
	public long getStartTime() { return this.startTime; }
	public boolean timerActive() { return this.startTime != 0 && TimeUtil.compareTime(this.duration, this.startTime); }
	long duration = 0;
	public long getDuration() { return this.duration; }
	public void setDuration(long duration) { this.duration = MathUtil.clamp(duration, 1000, Long.MAX_VALUE); }
	int discount = 10;
	public int getDiscount() { return this.discount; }
	public void setDiscount(int discount) { this.discount = MathUtil.clamp(discount, 1, 100); }
	
	private TimedSale() { super(TYPE); }

	
	@Override
	public IconData getIcon() { return IconUtil.ICON_TIMED_SALE; }

	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.timerActive())
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
	public void tradeCost(TradeCostEvent event)
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
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		//Write start time
		compound.putLong("startTime", this.startTime);
		//Save sale duration
		compound.putLong("duration", this.duration);
		//Save discount
		compound.putInt("discount", this.discount);
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json, HolderLookup.Provider lookup) {
		
		json.addProperty("duration", this.duration);
		json.addProperty("discount", this.discount);
		
		return json;
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
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
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("discount",this.discount);
		node.setLongValue("duration",this.duration);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.discount = Math.max(1,node.getIntValue("discount"));
		this.duration = node.getLongValue("duration");
	}

	@Override
	public void resetToDefaultState() {
		this.discount = 10;
		this.duration = 0;
		this.startTime = 0;
	}

	@Override
	public void loadFromJson(JsonObject json, HolderLookup.Provider lookup) {
		if(json.has("duration"))
			this.duration = json.get("duration").getAsLong();
		if(json.has("discount"))
			this.discount = MathUtil.clamp(this.discount, 0, 100);
	}
	
	@Override
	public void handleUpdateMessage(Player player, LazyPacketData updateInfo) {
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
			if(this.startTime != 0)
				return;
			this.startTime = TimeUtil.getCurrentTime();
		}
		else if(updateInfo.contains("StopSale"))
		{
			if(this.startTime == 0)
				return;
			this.startTime = 0;
		}
	}
	
	@Override
	public CompoundTag savePersistentData(HolderLookup.Provider lookup) {
		CompoundTag compound = new CompoundTag();
		compound.putLong("startTime", this.startTime);
		return compound;
	}
	@Override
	public void loadPersistentData(CompoundTag data, HolderLookup.Provider lookup) {
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

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new TimedSaleTab(parent); }
	
	
}
