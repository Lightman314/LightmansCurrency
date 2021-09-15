package io.github.lightman314.lightmanscurrency.tradedata.rules;

import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.PreTradeEvent;

public interface ITradeRuleHandler {

	public void beforeTrade(PreTradeEvent event);
	public void afterTrade(PostTradeEvent event);
	
}
