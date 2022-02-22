package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public interface ITradeRuleHandler {

	public void beforeTrade(PreTradeEvent event);
	public void tradeCost(TradeCostEvent event);
	public void afterTrade(PostTradeEvent event);
	
	public default boolean allowRule(TradeRule rule) { return true; }
	public List<TradeRule> getRules();
	public void addRule(TradeRule newRule);
	public void removeRule(TradeRule rule);
	public void clearRules();
	public void setRules(List<TradeRule> rules);
	public void markRulesDirty();
	
	public static <T extends ITradeRuleHandler> void savePersistentRuleData(CompoundTag data, @Nullable ITradeRuleHandler trader, @Nullable List<T> trades) {
		if(trader != null && trader.getRules().size() > 0)
			TradeRule.writePersistentData(data, trader.getRules(), "TraderRuleData");
		if(trades != null)
		{
			ListTag tradeData = new ListTag();
			for(int i = 0; i < trades.size(); ++i)
			{
				T trade = trades.get(i);
				if(trade.getRules().size() > 0)
				{
					CompoundTag ruleData = new CompoundTag();
					if(TradeRule.writePersistentData(ruleData, trade.getRules(), "Data"))
					{
						ruleData.putInt("index", i);
						tradeData.add(ruleData);
					}
				}
			}
			if(tradeData.size() > 0)
				data.put("TradeRuleData", tradeData);
		}
	}
	
	public static void readPersistentRuleData(CompoundTag data, @Nullable ITradeRuleHandler trader, @Nullable List<? extends ITradeRuleHandler> trades) {
		
		if(trader != null)
			TradeRule.readPersistentData(data, trader.getRules(), "TraderRuleData");
		if(trades != null && data.contains("TradeRuleData", Tag.TAG_LIST))
		{
			ListTag tradeData = data.getList("TradeRuleData", Tag.TAG_COMPOUND);
			for(int i = 0; i < tradeData.size(); ++i)
			{
				CompoundTag ruleData = tradeData.getCompound(i);
				int index = ruleData.getInt("index");
				if(index >= 0 && index < trades.size())
					TradeRule.readPersistentData(ruleData, trades.get(index).getRules(), "Data");
			}
		}
		
	}
	
}
