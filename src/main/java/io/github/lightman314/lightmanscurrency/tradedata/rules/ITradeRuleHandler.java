package io.github.lightman314.lightmanscurrency.tradedata.rules;

import java.util.List;

import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;

public interface ITradeRuleHandler {

	public void beforeTrade(PreTradeEvent event);
	public void afterTrade(PostTradeEvent event);
	
	public List<TradeRule> getRules();
	public void addRule(TradeRule newRule);
	public void removeRule(TradeRule rule);
	public void clearRules();
	public void setRules(List<TradeRule> rules);
	public void markRulesDirty();
	
}
