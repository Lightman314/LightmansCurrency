package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import java.util.List;

import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;

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
	
}
